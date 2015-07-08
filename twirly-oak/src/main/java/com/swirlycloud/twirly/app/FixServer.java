/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.app.FixUtility.sideToAction;
import static com.swirlycloud.twirly.util.TimeUtil.now;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.MinQty;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.FixRejectException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.AsyncModel;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.TransNode;

public final class FixServer extends MessageCracker implements Application {

    private final static Logger log = LoggerFactory.getLogger(FixServer.class);
    private static final ThreadLocal<FixBuilder> builderTls = new ThreadLocal<FixBuilder>() {
        @Override
        protected final FixBuilder initialValue() {
            return new FixBuilder();
        }
    };

    private final SessionSettings settings;
    // Guarded by serv lock.
    private final LockableServ serv;
    private final boolean nowFromTransactTime;
    // Guarded by serv lock.
    private final Map<String, SessionID> traderIdx = new HashMap<>();

    private static FixBuilder getBuilder(Message message) {
        final FixBuilder builder = builderTls.get();
        builder.setMessage(message);
        return builder;
    }

    private static void sendToTarget(Message message, SessionID sessionId) {
        try {
            Session.sendToTarget(message, sessionId);
        } catch (SessionNotFound e) {
            // What else can we do?
            log.error(sessionId + ": session not found", e);
        }
    }

    private final long getNow(Message message) throws FieldNotFound {
        return nowFromTransactTime ? message.getUtcTimeStamp(TransactTime.FIELD).getTime() : now();
    }

    @SuppressWarnings("null")
    private final Sess findSessLocked(SessionID sessionId) {
        try {
            return serv.getLazySessByEmail(settings.getString(sessionId, "Email"));
        } catch (ConfigError | FieldConvertError | NotFoundException e) {
            return null;
        }
    }

    private final void sendBusinessReject(Message refMsg, String refId, String text,
            SessionID sessionId) throws FieldNotFound {
        final FixBuilder builder = getBuilder(new BusinessMessageReject());
        builder.setBusinessReject(refMsg, text, refId);
        sendToTarget(builder.getMessage(), sessionId);
    }

    private final void sendCancelRejectLocked(String ref, Order order, String text,
            SessionID sessionId) {
        final FixBuilder builder = getBuilder(new OrderCancelReject());
        builder.setCancelReject(ref, order, text);
        sendToTarget(builder.getMessage(), sessionId);
    }

    private final void sendCancelReject(String ref, String orderRef, Long orderId, String text,
            SessionID sessionId) {
        final FixBuilder builder = getBuilder(new OrderCancelReject());
        builder.setCancelReject(ref, orderRef, orderId, text);
        sendToTarget(builder.getMessage(), sessionId);
    }

    private final void sendTransLocked(Sess sess, Trans trans, SessionID sessionId) {
        final FixBuilder builder = getBuilder(new ExecutionReport());
        String targetTrader = sess.getTrader();
        SessionID targetSessionId = sessionId;
        for (TransNode node = trans.execs.getFirst(); node != null; node = node.transNext()) {
            final Exec exec = (Exec) node;
            if (!exec.getTrader().equals(targetTrader)) {
                targetTrader = exec.getTrader();
                targetSessionId = traderIdx.get(targetTrader);
            }
            builder.setExec(exec);
            sendToTarget(builder.getMessage(), targetSessionId);
        }
    }

    public FixServer(SessionSettings settings, LockableServ serv) throws ConfigError,
            FieldConvertError, NotFoundException {
        this.settings = settings;
        this.serv = serv;
        this.nowFromTransactTime = settings.getBool("NowFromTransactTime");

        serv.acquireRead();
        try {
            final Iterator<SessionID> it = settings.sectionIterator();
            while (it.hasNext()) {
                final SessionID sessionId = it.next();
                final String email = settings.getString(sessionId, "Email");
                assert email != null;
                final Trader trader = serv.findTraderByEmail(email);
                if (trader == null) {
                    throw new NotFoundException(String.format("trader '%s' does not exist", email));
                }
                traderIdx.put(trader.getMnem(), sessionId);
            }
        } finally {
            serv.releaseRead();
        }
    }

    public FixServer(SessionSettings settings, @NonNull AsyncModel model) throws ConfigError,
            FieldConvertError, NotFoundException, InterruptedException, ExecutionException {
        this(settings, new LockableServ(model, now()));
    }

    public FixServer(SessionSettings settings, @NonNull Model model) throws ConfigError,
            FieldConvertError, NotFoundException {
        this(settings, new LockableServ(model, now()));
    }

    @Override
    public final void onMessage(BusinessMessageReject message, SessionID sessionId) {
        log.info(sessionId.getTargetCompID() + ": BusinessMessageReject: " + message);
    }

    @Override
    public final void onMessage(NewOrderSingle message, SessionID sessionId) throws FieldNotFound,
            IncorrectTagValue {
        log.info(sessionId.getTargetCompID() + ": NewOrderSingle: " + message);

        final String marketMnem = message.getString(Symbol.FIELD);
        final String ref = message.getString(ClOrdID.FIELD);
        final Action action = sideToAction(message.getChar(Side.FIELD));
        final long ticks = (long) message.getDouble(Price.FIELD);
        final long lots = (long) message.getDouble(OrderQty.FIELD);
        final long minLots = (long) message.getDouble(MinQty.FIELD);
        final long now = getNow(message);

        assert marketMnem != null;
        assert action != null;

        if ("NONE".equals(ref)) {
            // This is reserved in our FIX specification to mean "not a reference."
            throw new IncorrectTagValue(ClOrdID.FIELD, ref);
        }

        serv.acquireWrite();
        try {
            final Sess sess = findSessLocked(sessionId);
            if (sess == null) {
                throw new FixRejectException("session misconfigured");
            }
            final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
            if (market == null) {
                throw new FixRejectException(
                        String.format("market '%s' does not exist", marketMnem));
            }
            try (final Trans trans = new Trans()) {
                serv.placeOrder(sess, market, ref, action, ticks, lots, minLots, now, trans);
                log.info(sessionId + ": " + trans);
                sendTransLocked(sess, trans, sessionId);
            }
        } catch (FixRejectException e) {
            log.warn(sessionId + ": " + e.getMessage());
            sendBusinessReject(message, ref, e.getMessage(), sessionId);
        } catch (BadRequestException | NotFoundException e) {
            log.warn(sessionId + ": " + e.getMessage());
            sendBusinessReject(message, ref, "invalid request: " + e.getMessage(), sessionId);
        } catch (ServiceUnavailableException e) {
            log.warn(sessionId + ": " + e.getMessage());
            sendBusinessReject(message, ref, "service unavailable: " + e.getMessage(), sessionId);
        } finally {
            serv.releaseWrite();
        }
    }

    @Override
    public final void onMessage(OrderCancelReplaceRequest message, SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue {
        log.info(sessionId.getTargetCompID() + ": OrderCancelReplaceRequest: " + message);

        final String marketMnem = message.getString(Symbol.FIELD);
        final String ref = message.getString(ClOrdID.FIELD);
        final String orderRef = message.getString(OrigClOrdID.FIELD);
        Long orderId = null;
        if (message.isSetField(OrderID.FIELD)) {
            orderId = Long.valueOf(message.getString(OrderID.FIELD));
        }
        final long lots = (long) message.getDouble(OrderQty.FIELD);
        final long now = getNow(message);
        Order order = null;

        assert marketMnem != null;
        assert orderRef != null;

        serv.acquireWrite();
        try {
            final Sess sess = findSessLocked(sessionId);
            if (sess == null) {
                throw new FixRejectException("session misconfigured");
            }
            final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
            if (market == null) {
                throw new FixRejectException(
                        String.format("market '%s' does not exist", marketMnem));
            }
            if (orderId != null) {
                order = sess.findOrder(marketMnem, orderId);
                if (order == null) {
                    throw new FixRejectException(
                            String.format("order '%d' does not exist", orderId));
                }
            } else {
                order = sess.findOrder(orderRef);
                if (order == null) {
                    throw new FixRejectException(String.format("order '%s' does not exist",
                            orderRef));
                }
            }
            try (final Trans trans = new Trans()) {
                serv.reviseOrder(sess, market, order, lots, now, trans);
                log.info(sessionId + ": " + trans);
                sendTransLocked(sess, trans, sessionId);
            }
        } catch (FixRejectException e) {
            log.warn(sessionId + ": " + e.getMessage());
            sendCancelReject(ref, orderRef, orderId, e.getMessage(), sessionId);
        } catch (BadRequestException | NotFoundException e) {
            log.warn(sessionId + ": " + e.getMessage());
            assert order != null;
            sendCancelRejectLocked(ref, order, "invalid request: " + e.getMessage(), sessionId);
        } catch (ServiceUnavailableException e) {
            log.warn(sessionId + ": " + e.getMessage());
            assert order != null;
            sendCancelRejectLocked(ref, order, "service unavailable: " + e.getMessage(), sessionId);
        } finally {
            serv.releaseWrite();
        }
    }

    @Override
    public final void onMessage(OrderCancelRequest message, SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue {
        log.info(sessionId.getTargetCompID() + ": OrderCancelRequest: " + message);

        final String marketMnem = message.getString(Symbol.FIELD);
        final String ref = message.getString(ClOrdID.FIELD);
        final String orderRef = message.getString(OrigClOrdID.FIELD);
        Long orderId = null;
        if (message.isSetField(OrderID.FIELD)) {
            orderId = Long.valueOf(message.getString(OrderID.FIELD));
        }
        final long now = getNow(message);
        Order order = null;

        assert marketMnem != null;
        assert orderRef != null;

        serv.acquireWrite();
        try {
            final Sess sess = findSessLocked(sessionId);
            if (sess == null) {
                throw new FixRejectException("session misconfigured");
            }
            final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
            if (market == null) {
                throw new FixRejectException(
                        String.format("market '%s' does not exist", marketMnem));
            }
            if (orderId != null) {
                order = sess.findOrder(marketMnem, orderId);
                if (order == null) {
                    throw new FixRejectException(
                            String.format("order '%d' does not exist", orderId));
                }
            } else {
                order = sess.findOrder(orderRef);
                if (order == null) {
                    throw new FixRejectException(String.format("order '%s' does not exist",
                            orderRef));
                }
            }
            try (final Trans trans = new Trans()) {
                serv.cancelOrder(sess, market, order, now, trans);
                log.info(sessionId + ": " + trans);
                sendTransLocked(sess, trans, sessionId);
            }
        } catch (FixRejectException e) {
            log.warn(sessionId + ": " + e.getMessage());
            sendCancelReject(ref, orderRef, orderId, e.getMessage(), sessionId);
        } catch (BadRequestException | NotFoundException e) {
            log.warn(sessionId + ": " + e.getMessage());
            assert order != null;
            sendCancelRejectLocked(ref, order, "invalid request: " + e.getMessage(), sessionId);
        } catch (ServiceUnavailableException e) {
            log.warn(sessionId + ": " + e.getMessage());
            assert order != null;
            sendCancelRejectLocked(ref, order, "service unavailable: " + e.getMessage(), sessionId);
        } finally {
            serv.releaseWrite();
        }
    }

    @Override
    public final void onCreate(SessionID sessionId) {
        if (log.isInfoEnabled()) {
            log.info(sessionId + ": onCreate: " + sessionId);
        }
    }

    @Override
    public final void onLogon(SessionID sessionId) {
        if (log.isInfoEnabled()) {
            log.info(sessionId + ": onLogon: " + sessionId);
        }
    }

    @Override
    public final void onLogout(SessionID sessionId) {
        if (log.isInfoEnabled()) {
            log.info(sessionId + ": onLogout: " + sessionId);
        }
    }

    @Override
    public final void toAdmin(Message message, SessionID sessionId) {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": toAdmin: " + message);
        }
    }

    @Override
    public final void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": fromAdmin: " + message);
        }
    }

    @Override
    public final void toApp(Message message, SessionID sessionId) throws DoNotSend {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": toApp: " + message);
        }
    }

    @Override
    public final void fromApp(Message message, SessionID sessionId) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": fromApp: " + message);
        }
        crack(message, sessionId);
    }
}
