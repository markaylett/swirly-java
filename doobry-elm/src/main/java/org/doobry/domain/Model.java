/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public interface Model {
    Rec readRec(Kind kind);

    Order[] readOrder();

    Exec[] readTrade();

    Posn[] readPosn();
}
