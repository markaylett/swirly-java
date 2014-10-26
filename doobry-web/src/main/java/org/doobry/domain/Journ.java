/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public interface Journ {

    void insertExecList(Exec first, boolean enriched);

    void insertExec(Exec exec, boolean enriched);

    void updateExec(long id, long modified);
}
