/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

import quickfix.Log;

final class NullLog implements Log {

    @Override
    public final void clear() {
    }

    @Override
    public final void onIncoming(String message) {
    }

    @Override
    public final void onOutgoing(String message) {
    }

    @Override
    public final void onEvent(String text) {
    }

    @Override
    public final void onErrorEvent(String text) {
    }
}
