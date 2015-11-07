/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fix;

import static com.swirlycloud.twirly.fix.FixUtility.readSettings;

import java.io.IOException;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.exception.AlreadyExistsException;
import com.swirlycloud.twirly.quickfix.NullStoreFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.field.BusinessRejectRefID;
import quickfix.field.ClOrdID;
import quickfix.field.RefSeqNum;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.Reject;

public final class FixClnt extends MessageCracker implements AutoCloseable, Application {

    private final static Logger log = LoggerFactory.getLogger(FixClnt.class);
    private static final ThreadLocal<FixBuilder> builder = new ThreadLocal<FixBuilder>() {
        @Override
        protected final FixBuilder initialValue() {
            return new FixBuilder();
        }
    };

    @SuppressWarnings("unused")
    private final SessionSettings settings;
    private Initiator initiator;
    private final FixClntCache cache = new FixClntCache();
    private boolean logon = false;

    private FixClnt(final SessionSettings settings) throws ConfigError, FieldConvertError {
        this.settings = settings;
    }

    public static FixClnt create(final SessionSettings settings, final LogFactory logFactory)
            throws ConfigError, FieldConvertError {
        final FixClnt client = new FixClnt(settings);
        final Initiator initiator = new SocketInitiator(client, new NullStoreFactory(), settings,
                logFactory, new DefaultMessageFactory());
        // Transfer owndership.
        client.initiator = initiator;
        initiator.start();
        return client;
    }

    public static FixClnt create(final String path, final LogFactory logFactory)
            throws ConfigError, FieldConvertError, IOException {
        final SessionSettings settings = readSettings(path);
        return create(settings, logFactory);
    }

    @Override
    public final void close() throws Exception {
        initiator.stop();
    }

    @Override
    public final void onMessage(Reject message, SessionID sessionId) throws FieldNotFound {
        log.info(sessionId.getSenderCompID() + ": Reject: " + message);
        final int seqNum = message.getInt(RefSeqNum.FIELD);
        cache.setResponse(seqNum, message, sessionId);
    }

    @Override
    public final void onMessage(BusinessMessageReject message, SessionID sessionId)
            throws FieldNotFound {
        log.info(sessionId.getSenderCompID() + ": BusinessMessageReject: " + message);
        if (message.isSetField(BusinessRejectRefID.FIELD)) {
            final String ref = message.getString(BusinessRejectRefID.FIELD);
            assert ref != null;
            cache.setResponse(ref, message, sessionId);
        } else {
            final int seqNum = message.getInt(RefSeqNum.FIELD);
            cache.setResponse(seqNum, message, sessionId);
        }
    }

    @Override
    public final void onMessage(OrderCancelReject message, SessionID sessionId)
            throws FieldNotFound {
        log.info(sessionId.getSenderCompID() + ": OrderCancelReject: " + message);
        final String ref = message.getString(ClOrdID.FIELD);
        assert ref != null;
        cache.setResponse(ref, message, sessionId);
    }

    @Override
    public final void onMessage(ExecutionReport message, SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue {
        log.info(sessionId.getSenderCompID() + ": ExecutionReport: " + message);
        final String ref = message.getString(ClOrdID.FIELD);
        assert ref != null;
        cache.setResponse(ref, message, sessionId);
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
        synchronized (this) {
            logon = true;
            this.notifyAll();
        }
    }

    @Override
    public final void onLogout(SessionID sessionId) {
        if (log.isInfoEnabled()) {
            log.info(sessionId + ": onLogout: " + sessionId);
        }
        synchronized (this) {
            logon = false;
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
        try {
            crack(message, sessionId);
        } catch (final UnsupportedMessageType e) {
            log.error(sessionId + ": fromAdmin: " + e.getMessage(), e);
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

    public final Future<Message> sendNewOrderSingle(@NonNull String market, @NonNull String ref,
            @NonNull Side side, long lots, long ticks, long minLots, long now,
            @NonNull SessionID sessionId) throws AlreadyExistsException, SessionNotFound {
        final FixBuilder builder = FixClnt.builder.get();
        builder.setMessage(new NewOrderSingle());
        builder.setNewOrderSingle(market, ref, side, lots, ticks, minLots, now);
        final Message message = builder.getMessage();
        assert message != null;
        return cache.sendRequest(ref, message, sessionId);
    }

    public final Future<Message> sendOrderCancelReplaceRequest(@NonNull String market,
            @NonNull String ref, @NonNull String orderRef, long lots, long now,
            @NonNull SessionID sessionId) throws AlreadyExistsException, SessionNotFound {
        final FixBuilder builder = FixClnt.builder.get();
        builder.setMessage(new OrderCancelReplaceRequest());
        builder.setOrderCancelReplaceRequest(market, ref, orderRef, lots, now);
        final Message message = builder.getMessage();
        assert message != null;
        return cache.sendRequest(ref, message, sessionId);
    }

    public final Future<Message> sendOrderCancelReplaceRequest(@NonNull String market,
            @NonNull String ref, long orderId, long lots, long now, @NonNull SessionID sessionId)
                    throws AlreadyExistsException, SessionNotFound {
        final FixBuilder builder = FixClnt.builder.get();
        builder.setMessage(new OrderCancelReplaceRequest());
        builder.setOrderCancelReplaceRequest(market, ref, orderId, lots, now);
        final Message message = builder.getMessage();
        assert message != null;
        return cache.sendRequest(ref, message, sessionId);
    }

    public final Future<Message> sendOrderCancelRequest(@NonNull String market, @NonNull String ref,
            @NonNull String orderRef, long now, @NonNull SessionID sessionId)
                    throws AlreadyExistsException, SessionNotFound {
        final FixBuilder builder = FixClnt.builder.get();
        builder.setMessage(new OrderCancelRequest());
        builder.setOrderCancelRequest(market, ref, orderRef, now);
        final Message message = builder.getMessage();
        assert message != null;
        return cache.sendRequest(ref, message, sessionId);
    }

    public final Future<Message> sendOrderCancelRequest(@NonNull String market, @NonNull String ref,
            long orderId, long now, @NonNull SessionID sessionId)
                    throws AlreadyExistsException, SessionNotFound {
        final FixBuilder builder = FixClnt.builder.get();
        builder.setMessage(new OrderCancelRequest());
        builder.setOrderCancelRequest(market, ref, orderId, now);
        final Message message = builder.getMessage();
        assert message != null;
        return cache.sendRequest(ref, message, sessionId);
    }

    public final void waitForLogon() throws InterruptedException {
        synchronized (this) {
            while (!logon) {
                wait();
            }
        }
    }
}
