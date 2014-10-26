/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public interface Model {
    Rec[] readRec(RecType type);
    Order[] readOrder();
    Exec[] readTrade();
    Posn[] readPosn();
}
