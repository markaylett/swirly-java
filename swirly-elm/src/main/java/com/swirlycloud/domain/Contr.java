/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static com.swirlycloud.domain.Conv.fractToReal;
import static com.swirlycloud.domain.Conv.realToDp;

import java.io.IOException;

import com.swirlycloud.util.AshUtil;

public final class Contr extends Rec {
    private final AssetType assetType;
    private final String asset;
    private final String ccy;
    private final int tickNumer;
    private final int tickDenom;
    private final transient double priceInc;
    private final int lotNumer;
    private final int lotDenom;
    private final transient double qtyInc;
    private final transient int priceDp;
    private final int pipDp;
    private final transient int qtyDp;
    private final long minLots;
    private final long maxLots;

    public Contr(long id, String mnem, String display, AssetType assetType, String asset,
            String ccy, int tickNumer, int tickDenom, int lotNumer, int lotDenom, int pipDp,
            long minLots, long maxLots) {
        super(RecType.CONTR, id, mnem, display);
        if (id >= (1L << 16)) {
            throw new IllegalArgumentException("contr-id exceeds max-value");
        }
        this.assetType = assetType;
        this.asset = asset;
        this.ccy = ccy;
        this.tickNumer = tickNumer;
        this.tickDenom = tickDenom;
        this.priceInc = fractToReal(tickNumer, tickDenom);
        this.lotNumer = lotNumer;
        this.lotDenom = lotDenom;
        this.qtyInc = fractToReal(lotNumer, lotDenom);
        this.priceDp = realToDp(priceInc);
        this.pipDp = pipDp;
        this.qtyDp = realToDp(qtyInc);
        this.minLots = minLots;
        this.maxLots = maxLots;
    }

    @Override
    public final String toString() {
        return AshUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Appendable out, Object arg) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"assetType\":\"").append(assetType.name());
        out.append("\",\"asset\":\"").append(asset);
        out.append("\",\"ccy\":\"").append(ccy);
        out.append("\",\"tickNumer\":").append(String.valueOf(tickNumer));
        out.append(",\"tickDenom\":").append(String.valueOf(tickDenom));
        out.append(",\"lotNumer\":").append(String.valueOf(lotNumer));
        out.append(",\"lotDenom\":").append(String.valueOf(lotDenom));
        out.append(",\"priceDp\":").append(String.valueOf(priceDp));
        out.append(",\"pipDp\":").append(String.valueOf(pipDp));
        out.append(",\"qtyDp\":").append(String.valueOf(qtyDp));
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        out.append(",\"maxLots\":").append(String.valueOf(maxLots));
        out.append("}");
    }

    public final AssetType getAssetType() {
        return assetType;
    }

    public final String getAsset() {
        return asset;
    }

    public final String getCcy() {
        return ccy;
    }

    public final int getTickNumer() {
        return tickNumer;
    }

    public final int getTickDenom() {
        return tickDenom;
    }

    public final double getPriceInc() {
        return priceInc;
    }

    public final int getLotNumer() {
        return lotNumer;
    }

    public final int getLotDenom() {
        return lotDenom;
    }

    public final double getQtyInc() {
        return qtyInc;
    }

    public final int getPriceDp() {
        return priceDp;
    }

    public final int getPipDp() {
        return pipDp;
    }

    public final int getQtyDp() {
        return qtyDp;
    }

    public final long getMinLots() {
        return minLots;
    }

    public final long getMaxLots() {
        return maxLots;
    }
}
