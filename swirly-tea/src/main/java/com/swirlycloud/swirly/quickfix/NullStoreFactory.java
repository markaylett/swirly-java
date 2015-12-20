/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.SessionID;
import quickfix.SystemTime;

public final class NullStoreFactory implements MessageStoreFactory {
    private static final class NullStore implements MessageStore {
        private int nextSenderMsgSeqNum;
        private int nextTargetMsgSeqNum;
        private Calendar creationTime = SystemTime.getUtcCalendar();

        public NullStore() throws IOException {
            reset();
        }

        @Override
        public final void get(int startSequence, int endSequence, Collection<String> messages)
                throws IOException {
            // No messages.
        }

        @Override
        public final Date getCreationTime() throws IOException {
            return creationTime.getTime();
        }

        @Override
        public final int getNextSenderMsgSeqNum() throws IOException {
            return nextSenderMsgSeqNum;
        }

        @Override
        public final int getNextTargetMsgSeqNum() throws IOException {
            return nextTargetMsgSeqNum;
        }

        @Override
        public final void incrNextSenderMsgSeqNum() throws IOException {
            setNextSenderMsgSeqNum(getNextSenderMsgSeqNum() + 1);
        }

        @Override
        public final void incrNextTargetMsgSeqNum() throws IOException {
            setNextTargetMsgSeqNum(getNextTargetMsgSeqNum() + 1);
        }

        @Override
        public final void reset() throws IOException {
            setNextSenderMsgSeqNum(1);
            setNextTargetMsgSeqNum(1);
            creationTime = SystemTime.getUtcCalendar();
        }

        @Override
        public final boolean set(int sequence, String message) throws IOException {
            return true;
        }

        @Override
        public final void setNextSenderMsgSeqNum(int next) throws IOException {
            nextSenderMsgSeqNum = next;
        }

        @Override
        public final void setNextTargetMsgSeqNum(int next) throws IOException {
            nextTargetMsgSeqNum = next;
        }

        @Override
        public final void refresh() throws IOException {
        }
    }

    @Override
    public final MessageStore create(SessionID sessionID) {
        try {
            return new NullStore();
        } catch (final IOException e) {
            throw new RuntimeError(e);
        }
    }
}
