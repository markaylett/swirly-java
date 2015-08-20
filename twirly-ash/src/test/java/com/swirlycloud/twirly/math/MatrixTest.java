/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MatrixTest {

    private static final double DELTA = 0.000001;

    @Test
    public final void testZero() {
        final Matrix m = Matrix.zero(2);
        assertEquals(2, m.getCols());
        assertEquals(2, m.getRows());
        for (int i = 0; i < 2; ++i) {
            for (final int j = 0; i < 2; ++i) {
                assertEquals(0.0, m.getValue(i, j), 0.0);
            }
        }
    }

    @Test
    public final void testIdentity() {
        final Matrix m = Matrix.identity(5);
        assertEquals(5, m.getCols());
        assertEquals(5, m.getRows());
        for (int i = 0; i < 5; ++i) {
            for (final int j = 0; i < 5; ++i) {
                if (i == j) {
                    assertEquals(1.0, m.getValue(i, j), 0.0);
                } else {
                    assertEquals(0.0, m.getValue(i, j), 0.0);
                }
            }
        }
    }

    @Test(expected = Throwable.class)
    public final void testInvalidRow() {
        final Matrix m = Matrix.zero(2);
        m.getValue(2, 0);
    }

    @Test
    public final void testMultiply() {
        final Matrix d = Matrix.diagonal(0.2, 0.1, 0.15);
        final Matrix c = Matrix.identity(3);
        c.setValue(0, 1, 0.8);
        c.setValue(0, 2, 0.5);
        c.setValue(1, 0, 0.8);
        c.setValue(1, 2, 0.3);
        c.setValue(2, 0, 0.5);
        c.setValue(2, 1, 0.3);
        final Matrix v = d.multiply(c).multiply(d);
        assertEquals(0.04, v.getValue(0, 0), DELTA);
        assertEquals(0.016, v.getValue(0, 1), DELTA);
        assertEquals(0.015, v.getValue(0, 2), DELTA);
        assertEquals(0.016, v.getValue(1, 0), DELTA);
        assertEquals(0.01, v.getValue(1, 1), DELTA);
        assertEquals(0.0045, v.getValue(1, 2), DELTA);
        assertEquals(0.015, v.getValue(2, 0), DELTA);
        assertEquals(0.0045, v.getValue(2, 1), DELTA);
        assertEquals(0.0225, v.getValue(2, 2), DELTA);
    }
}