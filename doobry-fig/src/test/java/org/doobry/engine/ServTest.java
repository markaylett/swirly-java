/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Reg;
import org.doobry.mock.MockBank;
import org.doobry.mock.MockJourn;
import org.doobry.mock.MockModel;
import org.junit.Test;

public final class ServTest {

    @Test
    public final void test() {
        final Serv s = new Serv(new MockBank(Reg.values().length), new MockJourn());
        try {
            s.load(new MockModel());
        } finally {
            s.close();
        }
    }
}
