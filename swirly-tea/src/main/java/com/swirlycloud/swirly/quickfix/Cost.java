/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

public final class Cost extends LongField {
    private static final long serialVersionUID = 1L;
    public static final int FIELD = 20001;

    public Cost() {
        super(FIELD);
    }

    public Cost(Long data) {
        super(FIELD, data);
    }
}
