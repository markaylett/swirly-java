/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.domain.Conv.fractToReal;
import static com.swirlycloud.twirly.domain.Conv.realToDp;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Params;

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

    public static Contr parse(JsonParser p) throws IOException {
        long id = 0;
        String mnem = null;
        String display = null;
        AssetType assetType = null;
        String asset = null;
        String ccy = null;
        int tickNumer = 0;
        int tickDenom = 0;
        int lotNumer = 0;
        int lotDenom = 0;
        int pipDp = 0;
        long minLots = 0;
        long maxLots = 0;

        String name = null;
        while (p.hasNext()) {
            final Event event = p.next();
            switch (event) {
            case END_OBJECT:
                return new Contr(id, mnem, display, assetType, asset, ccy, tickNumer, tickDenom,
                        lotNumer, lotDenom, pipDp, minLots, maxLots);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("id".equals(name)) {
                    id = p.getLong();
                } else if ("tickNumer".equals(name)) {
                    tickNumer = p.getInt();
                } else if ("tickDenom".equals(name)) {
                    tickDenom = p.getInt();
                } else if ("lotNumer".equals(name)) {
                    lotNumer = p.getInt();
                } else if ("lotDenom".equals(name)) {
                    lotDenom = p.getInt();
                } else if ("pipDp".equals(name)) {
                    pipDp = p.getInt();
                } else if ("minLots".equals(name)) {
                    minLots = p.getLong();
                } else if ("maxLots".equals(name)) {
                    maxLots = p.getLong();
                } else {
                    throw new IOException(String.format("unexpected number field '%s'", name));
                }
                break;
            case VALUE_STRING:
                if ("mnem".equals(name)) {
                    mnem = p.getString();
                } else if ("display".equals(name)) {
                    display = p.getString();
                } else if ("assetType".equals(name)) {
                    assetType = AssetType.valueOf(p.getString());
                } else if ("asset".equals(name)) {
                    asset = p.getString();
                } else if ("ccy".equals(name)) {
                    ccy = p.getString();
                } else {
                    throw new IOException(String.format("unexpected string field '%s'", name));
                }
                break;
            default:
                throw new IOException(String.format("unexpected json token '%s'", event));
            }
        }
        throw new IOException("end-of object not found");
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out)
            throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"assetType\":\"").append(assetType.name());
        out.append("\",\"asset\":\"").append(asset);
        out.append("\",\"ccy\":\"").append(ccy);
        out.append("\",\"tickNumer\":").append(String.valueOf(tickNumer));
        out.append(",\"tickDenom\":").append(String.valueOf(tickDenom));
        out.append(",\"lotNumer\":").append(String.valueOf(lotNumer));
        out.append(",\"lotDenom\":").append(String.valueOf(lotDenom));
        out.append(",\"pipDp\":").append(String.valueOf(pipDp));
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
