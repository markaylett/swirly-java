/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.domain;

import com.swirlycloud.swirly.math.Matrix;

public final class Ladder extends Matrix {

    private static final long serialVersionUID = 1L;

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

    public void setVwap(boolean on) {
        final int rows = getRows();
        if (on) {
            // Accumulators.
            double bidLots = 0.0, bidCost = 0.0, bidCount = 0.0;
            double offerLots = 0.0, offerCost = 0.0, offerCount = 0.0;
            for (int row = 0; row < rows; ++row) {
                if (isValidBid(row)) {
                    final double lots = getBidLots(row);
                    bidLots += lots;
                    bidCost += lots * getBidTicks(row);
                    bidCount += getBidCount(row);
                    setBidLevel(row, bidCost / bidLots, bidLots, bidCount);
                }
                if (isValidOffer(row)) {
                    final double lots = getOfferLots(row);
                    offerLots += lots;
                    offerCost += lots * getOfferTicks(row);
                    offerCount += getOfferCount(row);
                    setOfferLevel(row, offerCost / offerLots, offerLots, offerCount);
                }
            }
        } else {
            for (int row = rows - 1; row > 0; --row) {
                if (isValidBid(row)) {
                    double bidLots = getBidLots(row);
                    double bidCost = bidLots * getBidTicks(row);
                    double bidCount = getBidCount(row);
                    final double prevLots = getBidLots(row - 1);
                    bidLots -= prevLots;
                    bidCost -= prevLots * getBidTicks(row - 1);
                    bidCount -= getBidCount(row - 1);
                    setBidLevel(row, bidCost / bidLots, bidLots, bidCount);
                }
                if (isValidOffer(row)) {
                    double offerLots = getOfferLots(row);
                    double offerCost = offerLots * getOfferTicks(row);
                    double offerCount = getOfferCount(row);
                    final double prevLots = getOfferLots(row - 1);
                    offerLots -= prevLots;
                    offerCost -= prevLots * getOfferTicks(row - 1);
                    offerCount -= getOfferCount(row - 1);
                    setOfferLevel(row, offerCost / offerLots, offerLots, offerCount);
                }
            }
        }
    }

    public final void setBidLevel(int row, double ticks, double lots, double count) {
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

    public final void setOfferLevel(int row, double ticks, double lots, double count) {
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
