/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import org.doobry.domain.Exec;
import org.doobry.domain.Journ;

public final class MockJourn implements Journ {
    @Override
    public final void insertExecList(Exec first, boolean enriched) {
    }
    @Override
    public final void insertExec(Exec exec, boolean enriched) {
    }
    @Override
    public final void updateExec(long id, long modified) {
    }
}
