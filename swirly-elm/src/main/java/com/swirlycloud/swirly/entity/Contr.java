/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import static com.swirlycloud.swirly.domain.Conv.fractToReal;
import static com.swirlycloud.swirly.domain.Conv.realToDp;

import java.io.IOException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.domain.RecType;
import com.swirlycloud.swirly.util.Params;

/**
 * A specification that stipulates the terms and conditions of sale.
 *
 * @author Mark Aylett
 */
public final @NonNullByDefault class Contr extends AbstractRec {

    private static final long serialVersionUID = 1L;
    private final String asset;
    private final String ccy;
    private final int lotNumer;
    private final int lotDenom;
    private final transient double qtyInc;
    private final int tickNumer;
    private final int tickDenom;
    private final transient double priceInc;
    private final int pipDp;
    private final transient int qtyDp;
    private final transient int priceDp;
    private final long minLots;
    private final long maxLots;

    protected Contr(String mnem, @Nullable String display, String asset, String ccy, int lotNumer,
            int lotDenom, int tickNumer, int tickDenom, int pipDp, long minLots, long maxLots) {
        super(mnem, display);
        this.asset = asset;
        this.ccy = ccy;
        this.lotNumer = lotNumer;
        this.lotDenom = lotDenom;
        this.qtyInc = fractToReal(lotNumer, lotDenom);
        this.tickNumer = tickNumer;
        this.tickDenom = tickDenom;
        this.priceInc = fractToReal(tickNumer, tickDenom);
        this.pipDp = pipDp;
        this.qtyDp = realToDp(qtyInc);
        this.priceDp = realToDp(priceInc);
        this.minLots = minLots;
        this.maxLots = maxLots;
    }

    public static Contr parse(JsonParser p) throws IOException {
        String mnem = null;
        String display = null;
        String asset = null;
        String ccy = null;
        int lotNumer = 0;
        int lotDenom = 0;
        int tickNumer = 0;
        int tickDenom = 0;
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
                return new Contr(mnem, display, asset, ccy, lotNumer, lotDenom, tickNumer,
                        tickDenom, pipDp, minLots, maxLots);
            case KEY_NAME:
                name = p.getString();
                break;
            case VALUE_NUMBER:
                if ("lotNumer".equals(name)) {
                    lotNumer = p.getInt();
                } else if ("lotDenom".equals(name)) {
                    lotDenom = p.getInt();
                } else if ("tickNumer".equals(name)) {
                    tickNumer = p.getInt();
                } else if ("tickDenom".equals(name)) {
                    tickDenom = p.getInt();
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
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        out.append("{\"mnem\":\"").append(mnem);
        out.append("\",\"display\":\"").append(display);
        out.append("\",\"asset\":\"").append(asset);
        out.append("\",\"ccy\":\"").append(ccy);
        out.append("\",\"lotNumer\":").append(String.valueOf(lotNumer));
        out.append(",\"lotDenom\":").append(String.valueOf(lotDenom));
        out.append(",\"tickNumer\":").append(String.valueOf(tickNumer));
        out.append(",\"tickDenom\":").append(String.valueOf(tickDenom));
        out.append(",\"pipDp\":").append(String.valueOf(pipDp));
        out.append(",\"minLots\":").append(String.valueOf(minLots));
        out.append(",\"maxLots\":").append(String.valueOf(maxLots));
        out.append("}");
    }

    @Override
    public final RecType getRecType() {
        return RecType.CONTR;
    }

    public final String getAsset() {
        return asset;
    }

    public final String getCcy() {
        return ccy;
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

    public final int getTickNumer() {
        return tickNumer;
    }

    public final int getTickDenom() {
        return tickDenom;
    }

    public final double getPriceInc() {
        return priceInc;
    }

    public final int getPriceDp() {
        return priceDp;
    }

    public final int getQtyDp() {
        return qtyDp;
    }

    public final int getPipDp() {
        return pipDp;
    }

    public final long getMinLots() {
        return minLots;
    }

    public final long getMaxLots() {
        return maxLots;
    }
}
