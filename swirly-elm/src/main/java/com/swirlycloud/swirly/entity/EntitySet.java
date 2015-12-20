/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;

public final @NonNullByDefault class EntitySet {
    public static final int ASSET = 1 << 0;
    public static final int CONTR = 1 << 1;
    public static final int MARKET = 1 << 2;
    public static final int TRADER = 1 << 3;
    public static final int ORDER = 1 << 4;
    public static final int TRADE = 1 << 5;
    public static final int POSN = 1 << 6;
    public static final int QUOTE = 1 << 7;
    public static final int VIEW = 1 << 8;

    private static final int REC_MASK = ASSET | CONTR | MARKET | TRADER;
    private static final int SESS_MASK = ORDER | TRADE | POSN | QUOTE | VIEW;

    private int bs;

    private static int bit(String name) {
        int n = 0;
        switch (name.charAt(0)) {
        case 'a':
            if ("asset".equals(name)) {
                n = ASSET;
            }
            break;
        case 'c':
            if ("contr".equals(name)) {
                n = CONTR;
            }
            break;
        case 'm':
            if ("market".equals(name)) {
                n = MARKET;
            }
            break;
        case 't':
            if ("trade".equals(name)) {
                n = TRADE;
            } else if ("trader".equals(name)) {
                n = TRADER;
            }
            break;
        case 'o':
            if ("order".equals(name)) {
                n = ORDER;
            }
            break;
        case 'p':
            if ("posn".equals(name)) {
                n = POSN;
            }
            break;
        case 'q':
            if ("quote".equals(name)) {
                n = QUOTE;
            }
            break;
        case 'v':
            if ("view".equals(name)) {
                n = VIEW;
            }
            break;
        }
        return n;
    }

    public EntitySet(int bs) {
        this.bs = bs;
    }

    public static EntitySet parse(String ls) {
        int bs = 0;
        int i = 0, j = 0;
        for (; j < ls.length(); ++j) {
            if (ls.charAt(j) == ',') {
                final String s = ls.substring(i, j);
                assert s != null;
                bs |= bit(s);
                i = j + 1;
            }
        }
        if (i != j) {
            final String s = ls.substring(i, j);
            assert s != null;
            bs |= bit(s);
        }
        return new EntitySet(bs);
    }

    public final boolean hasMany() {
        return (bs & (bs - 1)) != 0;
    }

    public final boolean hasNext() {
        return bs != 0;
    }

    public final int next() {
        final int n = Integer.lowestOneBit(bs);
        bs &= ~n;
        return n;
    }

    public final int getFirst() {
        return Integer.lowestOneBit(bs);
    }

    public final boolean isEmpty() {
        return bs == 0;
    }

    public final boolean isAssetSet() {
        return (bs & ASSET) != 0;
    }

    public final boolean isContrSet() {
        return (bs & CONTR) != 0;
    }

    public final boolean isMarketSet() {
        return (bs & MARKET) != 0;
    }

    public final boolean isTraderSet() {
        return (bs & TRADER) != 0;
    }

    public final boolean isOrderSet() {
        return (bs & ORDER) != 0;
    }

    public final boolean isTradeSet() {
        return (bs & TRADE) != 0;
    }

    public final boolean isPosnSet() {
        return (bs & POSN) != 0;
    }

    public final boolean isQuoteSet() {
        return (bs & QUOTE) != 0;
    }

    public final boolean isViewSet() {
        return (bs & VIEW) != 0;
    }

    public final boolean isRecSet() {
        return (bs & REC_MASK) != 0;
    }

    public final boolean isSessSet() {
        return (bs & SESS_MASK) != 0;
    }
}
