/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import com.swirlycloud.twirly.date.GregDate;

public final class IrPoint {
    private final String tenor;
    private final GregDate maturityDate;
    private final double parRate;

    public IrPoint(String tenor, GregDate maturityDate, double parRate) {
        this.tenor = tenor;
        this.maturityDate = maturityDate;
        this.parRate = parRate;
    }

    public final String getTenor() {
        return tenor;
    }

    public final GregDate getMaturityDate() {
        return maturityDate;
    }

    public final double getParRate() {
        return parRate;
    }
}
