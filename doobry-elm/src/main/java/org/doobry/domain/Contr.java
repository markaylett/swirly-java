/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

import static org.doobry.domain.Conv.fractToReal;
import static org.doobry.domain.Conv.realToDp;

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
        final StringBuilder sb = new StringBuilder();
        print(sb, null);
        return sb.toString();
    }

    @Override
    public final void print(StringBuilder sb, Object arg) {
        sb.append("{\"mnem\":\"").append(mnem);
        sb.append("\",\"display\":\"").append(display);
        sb.append("\",\"assetType\":\"").append(assetType);
        sb.append("\",\"asset\":\"").append(asset);
        sb.append("\",\"ccy\":\"").append(ccy);
        sb.append("\",\"tickNumer\":").append(tickNumer);
        sb.append(",\"tickDenom\":").append(tickDenom);
        sb.append(",\"lotNumer\":").append(lotNumer);
        sb.append(",\"lotDenom\":").append(lotDenom);
        sb.append(",\"priceDp\":").append(priceDp);
        sb.append(",\"pipDp\":").append(pipDp);
        sb.append(",\"qtyDp\":").append(qtyDp);
        sb.append(",\"minLots\":").append(minLots);
        sb.append(",\"maxLots\":").append(maxLots);
        sb.append("}");
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
