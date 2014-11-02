/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import static org.doobry.util.Date.jdToIso;

import org.doobry.domain.Contr;
import org.doobry.domain.Level;
import org.doobry.domain.Side;
import org.doobry.util.RbNode;

public final class View {

    /**
     * Maximum price levels in view.
     */
    private static final int LEVEL_MAX = 3;

    private View() {
    }

    public static void print(StringBuilder sb, Book book, long now) {
        final Contr contr = book.getContr();
        final int settlDay = book.getSettlDay();
        sb.append("{contr\":\"").append(contr.getMnem()).append("\",");
        sb.append("\"settl_date\":").append(jdToIso(settlDay)).append(",");

        final Side bidSide = book.getBidSide();
        final Side offerSide = book.getOfferSide();

        final RbNode bidFirst = bidSide.getFirstLevel();
        final RbNode offerFirst = offerSide.getFirstLevel();

        sb.append("\"bid_ticks\":[");
        RbNode node = bidFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getTicks());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"bid_lots\":[");
        node = bidFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getLots());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"bid_count\":[");
        node = bidFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getCount());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"offer_ticks\":[");
        node = offerFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getTicks());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"offer_lots\":[");
        node = offerFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getLots());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"offer_count\":[");
        node = offerFirst;
        for (int i = 0; i < LEVEL_MAX; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                sb.append(level.getCount());
                node = node.rbNext();
            } else {
                sb.append('0');
            }
        }
        sb.append("],\"created\":").append(now).append("}");
    }
}
