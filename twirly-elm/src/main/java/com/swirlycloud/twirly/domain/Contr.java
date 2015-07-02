/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.domain.Conv.fractToReal;
import static com.swirlycloud.twirly.domain.Conv.realToDp;
import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class Contr extends Rec {
    private Memorable asset;
    private Memorable ccy;
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

    public Contr(String mnem, @Nullable String display, Memorable asset, Memorable ccy,
            int tickNumer, int tickDenom, int lotNumer, int lotDenom, int pipDp, long minLots,
            long maxLots) {
        super(mnem, display);
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
        String mnem = null;
        String display = null;
        Memorable asset = null;
        Memorable ccy = null;
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
                if (mnem == null) {
                    throw new IOException("mnem is null");
                }
                if (asset == null) {
                    throw new IOException("asset is null");
                }
                if (ccy == null) {
                    throw new IOException("ccy is null");
                }
                return new Contr(mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer,
                        lotDenom, pipDp, minLots, maxLots);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("tickNumer".equals(name)) {
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
                } else if ("asset".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    asset = newMnem(s);
                } else if ("ccy".equals(name)) {
                    final String s = p.getString();
                    assert s != null;
                    ccy = newMnem(s);
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
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"asset\":\"").append(asset.getMnem());
        out.append("\",\"ccy\":\"").append(ccy.getMnem());
        out.append("\",\"tickNumer\":").append(String.valueOf(tickNumer));
        out.append(",\"tickDenom\":").append(String.valueOf(tickDenom));
        out.append(",\"lotNumer\":").append(String.valueOf(lotNumer));
        out.append(",\"lotDenom\":").append(String.valueOf(lotDenom));
        out.append(",\"pipDp\":").append(String.valueOf(pipDp));
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        out.append(",\"maxLots\":").append(String.valueOf(maxLots));
        out.append("}");
    }

    public final void enrich(Asset asset, Asset ccy) {
        assert this.asset.getMnem().equals(asset.getMnem());
        assert this.ccy.getMnem().equals(ccy.getMnem());
        this.asset = asset;
        this.ccy = ccy;
    }

    @Override
    public final RecType getRecType() {
        return RecType.CONTR;
    }

    public final String getAsset() {
        return asset.getMnem();
    }

    public final Asset getAssetRich() {
        return (Asset) asset;
    }

    public final String getCcy() {
        return ccy.getMnem();
    }

    public final Asset getCcyRich() {
        return (Asset) ccy;
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
