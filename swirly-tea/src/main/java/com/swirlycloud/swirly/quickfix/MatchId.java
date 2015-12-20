/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

public final class MatchId extends LongField {
    private static final long serialVersionUID = 1L;
    public static final int FIELD = 20002;

    public MatchId() {
        super(FIELD);
    }

    public MatchId(Long data) {
        super(FIELD, data);
    }
}
