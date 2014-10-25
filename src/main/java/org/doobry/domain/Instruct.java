/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

/**
 * Fields common to both Order and Exec.
 */

public interface Instruct {

    long getTraderId();

    Party getTrader();

    long getGiveupId();

    Party getGiveup();

    long getContrId();

    Contr getContr();

    int getSettlDay();

    String getRef();

    State getState();

    Action getAction();

    long getTicks();

    long getLots();

    long getResd();

    long getExec();

    long getLastTicks();

    long getLastLots();

    long getMinLots();

    boolean isDone();
}