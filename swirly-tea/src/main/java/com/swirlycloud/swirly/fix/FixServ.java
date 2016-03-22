/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.fix;

import static com.swirlycloud.swirly.fix.FixUtility.fixToSide;
import static com.swirlycloud.swirly.fix.FixUtility.readSettings;
import static com.swirlycloud.swirly.util.TimeUtil.getTimeOfDay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swirlycloud.swirly.app.LockableServ;
import com.swirlycloud.swirly.app.Response;
import com.swirlycloud.swirly.book.MarketBook;
import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.entity.TraderSess;
import com.swirlycloud.swirly.exception.InternalException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.OrderNotFoundException;
import com.swirlycloud.swirly.exception.ServException;
import com.swirlycloud.swirly.node.SlNode;
import com.swirlycloud.swirly.quickfix.NullStoreFactory;

import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.CxlRejResponseTo;
import quickfix.field.MinQty;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public final class FixServ extends MessageCracker implements AutoCloseable, Application {

    private final static Logger log = LoggerFactory.getLogger(FixServ.class);
    private static final ThreadLocal<FixBuilder> builderTls = new ThreadLocal<FixBuilder>() {
        @Override
        protected final FixBuilder initialValue() {
            return new FixBuilder();
        }
    };

    private final SessionSettings settings;
    // Guarded by serv lock.
    private final LockableServ serv;
    private Acceptor acceptor;
    private final boolean nowFromTransactTime;
    // Guarded by serv lock.
    private final Map<String, SessionID> traderIdx = new HashMap<>();

    private static FixBuilder getBuilder(Message message) {
        final FixBuilder builder = builderTls.get();
        builder.setMessage(message);
        return builder;
    }

    private static void sendToTarget(@NonNull Message message, @NonNull SessionID sessionId) {
        try {
            Session.sendToTarget(message, sessionId);
        } catch (final SessionNotFound e) {
            // What else can we do?
            log.error(sessionId + ": session not found", e);
        }
    }

    private final long getNow(@NonNull Message message) throws FieldNotFound {
        return nowFromTransactTime ? message.getUtcTimeStamp(TransactTime.FIELD).getTime() : getTimeOfDay();
    }

    @SuppressWarnings("null")
    private final TraderSess getTraderLocked(@NonNull SessionID sessionId)
            throws NotFoundException {
        try {
            return serv.getTrader(settings.getString(sessionId, "Trader"));
        } catch (ConfigError | FieldConvertError e) {
            return null;
        }
    }

    private final void sendBusinessReject(@NonNull Message refMsg, @Nullable String refId,
            @NonNull ServException e, @NonNull SessionID sessionId) throws FieldNotFound {
        final FixBuilder builder = getBuilder(new BusinessMessageReject());
        builder.setBusinessReject(refMsg, refId, e.getBusinessRejectReason(), e.getMessage());
        final Message message = builder.getMessage();
        assert message != null;
        sendToTarget(message, sessionId);
    }

    private final void sendCancelReject(@NonNull String ref, @NonNull String orderRef,
            char responseTo, @NonNull ServException e, @NonNull SessionID sessionId) {
        final FixBuilder builder = getBuilder(new OrderCancelReject());
        builder.setCancelReject(ref, orderRef, responseTo, e.getCancelRejectReason(),
                e.getMessage());
        final Message message = builder.getMessage();
        assert message != null;
        sendToTarget(message, sessionId);
    }

    private final void sendCancelRejectLocked(@NonNull String ref, @NonNull Order order,
            char responseTo, @NonNull ServException e, @NonNull SessionID sessionId) {
        final FixBuilder builder = getBuilder(new OrderCancelReject());
        builder.setCancelReject(ref, order, responseTo, e.getCancelRejectReason(), e.getMessage());
        final Message message = builder.getMessage();
        assert message != null;
        sendToTarget(message, sessionId);
    }

    private final void sendResponseLocked(TraderSess sess, String ref, Response resp,
            @NonNull SessionID sessionId) {
        final FixBuilder builder = getBuilder(new ExecutionReport());
        String targetTrader = sess.getMnem();
        SessionID targetSessionId = sessionId;
        for (SlNode node = resp.getFirstExec(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (!exec.getTrader().equals(targetTrader)) {
                targetTrader = exec.getTrader();
                targetSessionId = traderIdx.get(targetTrader);
            }
            if (targetSessionId != null) {
                builder.setExec(exec, ref);
                final Message message = builder.getMessage();
                assert message != null;
                sendToTarget(message, targetSessionId);
            }
        }
    }

    private FixServ(SessionSettings settings, LockableServ serv)
            throws ConfigError, FieldConvertError, NotFoundException {
        this.settings = settings;
        this.serv = serv;
        this.nowFromTransactTime = settings.getBool("NowFromTransactTime");

        final int lock = serv.readLock();
        try {
            final Iterator<SessionID> it = settings.sectionIterator();
            while (it.hasNext()) {
                final SessionID sessionId = it.next();
                final String trader = settings.getString(sessionId, "Trader");
                assert trader != null;
                traderIdx.put(trader, sessionId);
            }
        } finally {
            serv.unlock(lock);
        }
    }

    public static FixServ create(final SessionSettings settings, LockableServ serv,
            final LogFactory logFactory) throws ConfigError, FieldConvertError, NotFoundException {
        final FixServ server = new FixServ(settings, serv);
        final Acceptor acceptor = new SocketAcceptor(server, new NullStoreFactory(), settings,
                logFactory, new DefaultMessageFactory());
        // Transfer owndership.
        server.acceptor = acceptor;
        acceptor.start();
        return server;
    }

    public static FixServ create(final String path, LockableServ serv, final LogFactory logFactory)
            throws ConfigError, FieldConvertError, NotFoundException, IOException {
        final SessionSettings settings = readSettings(path);
        return create(settings, serv, logFactory);
    }

    @Override
    public final void close() throws Exception {
        acceptor.stop();
    }

    @Override
    public final void onMessage(BusinessMessageReject message, SessionID sessionId) {
        log.info(sessionId.getTargetCompID() + ": BusinessMessageReject: " + message);
    }

    @Override
    public final void onMessage(NewOrderSingle message, SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue {
        log.info(sessionId.getTargetCompID() + ": NewOrderSingle: " + message);

        final String market = message.getString(Symbol.FIELD);
        final String ref = message.getString(ClOrdID.FIELD);
        final long quoteId = 0;
        final Side side = fixToSide(message.getChar(quickfix.field.Side.FIELD));
        final long lots = (long) message.getDouble(OrderQty.FIELD);
        final long ticks = (long) message.getDouble(Price.FIELD);
        final long minLots = (long) message.getDouble(MinQty.FIELD);
        final long now = getNow(message);

        // FIXME.
        assert market != null;
        assert side != null;

        if ("NONE".equals(ref)) {
            // This is reserved in our FIX specification to mean "not a reference."
            throw new IncorrectTagValue(ClOrdID.FIELD, ref);
        }

        final int lock = serv.writeLock();
        try {
            final TraderSess sess = getTraderLocked(sessionId);
            if (sess == null) {
                throw new InternalException("session misconfigured");
            }
            final MarketBook book = serv.getMarket(market);
            try (final Response resp = new Response()) {
                serv.createOrder(sess, book, ref, quoteId, side, lots, ticks, minLots, now, resp);
                log.info(sessionId + ": " + resp);
                sendResponseLocked(sess, null, resp, sessionId);
            }
        } catch (final ServException e) {
            log.warn(sessionId + ": " + e.getMessage());
            sendBusinessReject(message, ref, e, sessionId);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void onMessage(OrderCancelReplaceRequest message, SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue {
        log.info(sessionId.getTargetCompID() + ": OrderCancelReplaceRequest: " + message);

        final String market = message.getString(Symbol.FIELD);
        final String ref = message.getString(ClOrdID.FIELD);
        final String orderRef = message.getString(OrigClOrdID.FIELD);
        Long orderId = null;
        if (message.isSetField(OrderID.FIELD)) {
            orderId = Long.valueOf(message.getString(OrderID.FIELD));
        }
        final long lots = (long) message.getDouble(OrderQty.FIELD);
        final long now = getNow(message);
        Order order = null;

        // FIXME.
        assert market != null;
        assert ref != null;
        assert orderRef != null;

        final int lock = serv.writeLock();
        try {
            final TraderSess sess = getTraderLocked(sessionId);
            if (sess == null) {
                throw new InternalException("session misconfigured");
            }
            final MarketBook book = serv.getMarket(market);
            if (orderId != null) {
                order = sess.findOrder(market, orderId);
                if (order == null) {
                    throw new OrderNotFoundException(
                            String.format("order '%d' does not exist", orderId));
                }
            } else {
                order = sess.findOrder(orderRef);
                if (order == null) {
                    throw new OrderNotFoundException(
                            String.format("order '%s' does not exist", orderRef));
                }
            }
            try (final Response resp = new Response()) {
                serv.reviseOrder(sess, book, order, lots, now, resp);
                log.info(sessionId + ": " + resp);
                sendResponseLocked(sess, ref, resp, sessionId);
            }
        } catch (final ServException e) {
            log.warn(sessionId + ": " + e.getMessage());
            if (order == null) {
                sendCancelReject(ref, orderRef, CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST, e,
                        sessionId);
            } else {
                sendCancelRejectLocked(ref, order, CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST, e,
                        sessionId);
            }
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void onMessage(OrderCancelRequest message, SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue {
        log.info(sessionId.getTargetCompID() + ": OrderCancelRequest: " + message);

        final String market = message.getString(Symbol.FIELD);
        final String ref = message.getString(ClOrdID.FIELD);
        final String orderRef = message.getString(OrigClOrdID.FIELD);
        Long orderId = null;
        if (message.isSetField(OrderID.FIELD)) {
            orderId = Long.valueOf(message.getString(OrderID.FIELD));
        }
        final long now = getNow(message);
        Order order = null;

        // FIXME.
        assert market != null;
        assert ref != null;
        assert orderRef != null;

        final int lock = serv.writeLock();
        try {
            final TraderSess sess = getTraderLocked(sessionId);
            if (sess == null) {
                throw new InternalException("session misconfigured");
            }
            final MarketBook book = serv.getMarket(market);
            if (orderId != null) {
                order = sess.findOrder(market, orderId);
                if (order == null) {
                    throw new OrderNotFoundException(
                            String.format("order '%d' does not exist", orderId));
                }
            } else {
                order = sess.findOrder(orderRef);
                if (order == null) {
                    throw new OrderNotFoundException(
                            String.format("order '%s' does not exist", orderRef));
                }
            }
            try (final Response resp = new Response()) {
                serv.cancelOrder(sess, book, order, now, resp);
                log.info(sessionId + ": " + resp);
                sendResponseLocked(sess, ref, resp, sessionId);
            }
        } catch (final ServException e) {
            log.warn(sessionId + ": " + e.getMessage());
            if (order == null) {
                sendCancelReject(ref, orderRef, CxlRejResponseTo.ORDER_CANCEL_REQUEST, e,
                        sessionId);
            } else {
                sendCancelRejectLocked(ref, order, CxlRejResponseTo.ORDER_CANCEL_REQUEST, e,
                        sessionId);
            }
        } finally {
            serv.unlock(lock);
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
    public final void fromAdmin(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
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
    public final void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (log.isDebugEnabled()) {
            log.debug(sessionId + ": fromApp: " + message);
        }
        crack(message, sessionId);
    }
}
