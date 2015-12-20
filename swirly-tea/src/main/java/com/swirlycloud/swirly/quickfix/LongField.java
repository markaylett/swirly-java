/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.quickfix;

import quickfix.Field;

public class LongField extends Field<Long> {

    private static final long serialVersionUID = 1L;

    public LongField(int field) {
        super(field, 0L);
    }

    public LongField(int field, Long data) {
        super(field, data);
    }

    public LongField(int field, long data) {
        super(field, data);
    }

    public final void setValue(Long value) {
        setObject(value);
    }

    public final void setValue(long value) {
        setObject(value);
    }

    public final long getValue() {
        return getObject();
    }

    public final boolean valueEquals(Long value) {
        return getObject().equals(value);
    }

    public final boolean valueEquals(int value) {
        return getObject().equals(value);
    }
}
