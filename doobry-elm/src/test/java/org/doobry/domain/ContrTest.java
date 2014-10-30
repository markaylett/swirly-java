/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.junit.Assert.assertEquals;

import org.doobry.mock.MockContr;
import org.junit.Test;

public final class ContrTest {
    @Test
    public final void test() {
        assertEquals(
                "{\"mnem\":\"EURUSD\",\"display\":\"EURUSD\",\"asset_type\":\"CURRENCY\",\"asset\":\"EUR\",\"ccy\":\"USD\",\"tick_numer\":1,\"tick_denom\":10000,\"lot_numer\":1000000,\"lot_denom\":1,\"price_dp\":4,\"pip_dp\":4,\"qty_dp\":0,\"min_lots\":1,\"max_lots\":10}",
                MockContr.newContr("EURUSD").toString());
    }
}
