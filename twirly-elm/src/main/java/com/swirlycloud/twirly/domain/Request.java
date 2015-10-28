/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Identifiable;

public @NonNullByDefault interface Request extends SlNode, Identifiable, Financial {

    void setRefNext(@Nullable Request next);

    @Nullable
    Request refNext();

    @Override
    long getId();

    String getTrader();

    @Override
    String getMarket();

    @Override
    String getContr();

    @Override
    int getSettlDay();

    @Override
    boolean isSettlDaySet();

    @Nullable
    String getRef();

    Side getSide();

    long getLots();

    long getCreated();
}
