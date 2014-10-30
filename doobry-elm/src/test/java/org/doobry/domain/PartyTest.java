/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.junit.Assert.assertEquals;

import org.doobry.mock.MockParty;
import org.junit.Test;

public final class PartyTest {
    @Test
    public final void test() {
        assertEquals(
                "{\"mnem\":\"WRAMIREZ\",\"display\":\"Wayne Ramirez\",\"email\":\"wayne.ramirez@doobry.org\"}",
                MockParty.newParty("WRAMIREZ").toString());
    }
}
