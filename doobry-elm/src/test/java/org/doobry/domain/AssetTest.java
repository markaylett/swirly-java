/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.junit.Assert.assertEquals;

import org.doobry.mock.MockAsset;
import org.junit.Test;

public final class AssetTest {
    @Test
    public final void test() {
        assertEquals(
                "{\"mnem\":\"GBP\",\"display\":\"United Kingdom, Pounds\",\"type\":\"CURRENCY\"}",
                MockAsset.newAsset("GBP").toString());
    }
}
