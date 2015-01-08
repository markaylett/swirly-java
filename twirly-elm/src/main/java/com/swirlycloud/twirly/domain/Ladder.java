/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import com.swirlycloud.twirly.math.Matrix;

public final class Ladder extends Matrix {
    /**
     * Maximum rungs in ladder.
     */
    public static final int DEPTH_MAX = 5;

    // Columns.
    public static final int BID_TICKS = 0;
    public static final int BID_LOTS = 1;
    public static final int BID_COUNT = 2;
    public static final int OFFER_TICKS = 3;
    public static final int OFFER_LOTS = 4;
    public static final int OFFER_COUNT = 5;

    public Ladder() {
        super(DEPTH_MAX, 6);
    }

    public void vwap() {
        final int rows = getRows();
        // Accumulators.
        double bidLots = 0.0, bidLicks = 0.0, bidCount = 0.0;
        double offerLots = 0.0, offerLicks = 0.0, offerCount = 0.0;
        for (int row = 0; row < rows; ++row) {
            if (isValidBid(row)) {
                final double lots = getBidLots(row);
                bidLots += lots;
                bidLicks += lots * getBidTicks(row);
                bidCount += getBidCount(row);
                setBidRung(row, bidLicks / bidLots, bidLots, bidCount);
            }
            if (isValidOffer(row)) {
                final double lots = getOfferLots(row);
                offerLots += lots;
                offerLicks += lots * getOfferTicks(row);
                offerCount += getOfferCount(row);
                setOfferRung(row, offerLicks / offerLots, offerLots, offerCount);
            }
        }
    }

    public final void setBidRung(int row, double ticks, double lots, double count) {
        setValue(row, BID_TICKS, ticks);
        setValue(row, BID_LOTS, lots);
        setValue(row, BID_COUNT, count);
    }

    public final void setBidTicks(int row, double ticks) {
        setValue(row, BID_TICKS, ticks);
    }

    public final void setBidLots(int row, double lots) {
        setValue(row, BID_LOTS, lots);
    }

    public final void setBidCount(int row, double count) {
        setValue(row, BID_COUNT, count);
    }

    public final void setOfferRung(int row, double ticks, double lots, double count) {
        setValue(row, OFFER_TICKS, ticks);
        setValue(row, OFFER_LOTS, lots);
        setValue(row, OFFER_COUNT, count);
    }

    public final void setOfferTicks(int row, double ticks) {
        setValue(row, OFFER_TICKS, ticks);
    }

    public final void setOfferLots(int row, double lots) {
        setValue(row, OFFER_LOTS, lots);
    }

    public final void setOfferCount(int row, double count) {
        setValue(row, OFFER_COUNT, count);
    }

    public final boolean isValidBid(int row) {
        // Use lots instead of ticks or count, because zero is a valid price, and order counts may
        // be unavailable.
        return getBidLots(row) > 0;
    }

    public final double getBidTicks(int row) {
        return getValue(row, BID_TICKS);
    }

    public final double getBidLots(int row) {
        return getValue(row, BID_LOTS);
    }

    public final double getBidCount(int row) {
        return getValue(row, BID_COUNT);
    }

    public final boolean isValidOffer(int row) {
        // Use lots instead of ticks or count, because zero is a valid price, and order counts may
        // be unavailable.
        return getOfferLots(row) > 0;
    }

    public final double getOfferTicks(int row) {
        return getValue(row, OFFER_TICKS);
    }

    public final double getOfferLots(int row) {
        return getValue(row, OFFER_LOTS);
    }

    public final double getOfferCount(int row) {
        return getValue(row, OFFER_COUNT);
    }

    public final long roundBidTicks(int row) {
        return (long) getBidTicks(row);
    }

    public final long roundBidLots(int row) {
        return (long) getBidLots(row);
    }

    public final int roundBidCount(int row) {
        return (int) getBidCount(row);
    }

    public final long roundOfferTicks(int row) {
        return (long) Math.ceil(getOfferTicks(row));
    }

    public final long roundOfferLots(int row) {
        return (long) getOfferLots(row);
    }

    public final int roundOfferCount(int row) {
        return (int) getOfferCount(row);
    }
}
