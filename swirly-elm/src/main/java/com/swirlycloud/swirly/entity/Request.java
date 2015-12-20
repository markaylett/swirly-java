/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.node.SlNode;

public @NonNullByDefault interface Request extends SlNode, Financial {

    void setRefNext(@Nullable Request next);

    @Nullable
    Request refNext();

    String getTrader();

    @Override
    String getMarket();

    @Override
    String getContr();

    @Override
    int getSettlDay();

    @Override
    boolean isSettlDaySet();

    long getId();

    @Nullable
    String getRef();

    Side getSide();

    long getLots();

    long getCreated();
}
