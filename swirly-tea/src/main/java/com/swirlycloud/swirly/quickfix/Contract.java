/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

import quickfix.StringField;

public final class Contract extends StringField {
    private static final long serialVersionUID = 1L;
    public static final int FIELD = 20000;

    public Contract() {
        super(FIELD);
    }

    public Contract(String data) {
        super(FIELD, data);
    }
}
