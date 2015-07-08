/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.node.SlNode;

public final class MockContr {

    private static final List<NullaryFunction<Contr>> LIST = new ArrayList<>();
    private static final Map<String, NullaryFunction<Contr>> MAP = new HashMap<>();

    private static void put(final @NonNull String mnem, final String display,
            final @NonNull String asset, final @NonNull String ccy, final int tickNumer,
            final int tickDenom, final int lotNumer, final int lotDenom, final int pipDp,
            final long minLots, final long maxLots) {
        final NullaryFunction<Contr> fn = new NullaryFunction<Contr>() {
            @Override
            public final Contr call() {
                return new Contr(mnem, display, MockAsset.newAsset(asset), MockAsset.newAsset(ccy),
                        tickNumer, tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots);
            }
        };
        LIST.add(fn);
        MAP.put(mnem, fn);
    }

    static {
        // Forex.
        put("EURUSD", "EURUSD", "EUR", "USD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("GBPUSD", "GBPUSD", "GBP", "USD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("USDCHF", "USDCHF", "USD", "CHF", 1, 10000, 1000000, 1, 4, 1, 10);
        put("USDJPY", "USDJPY", "USD", "JPY", 1, 100, 1000000, 1, 2, 1, 10);
        // Coal.
        put("CAP", "Central Appalachia Coal", "CAP", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("NAP", "Northern Appalachia Coal", "NAP", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("ILB", "Illinois Basin Coal", "ILB", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("PRB", "Powder River Basin Coal", "PRB", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("UIB", "Uinta Basin Coal", "UIB", "USD", 1, 20, 1000, 1, 2, 1, 10);
        // Coffee.
        put("WYCA", "Yirgachefe A", "WYCA", "ETB", 1, 1, 1, 1, 0, 1, 10);
        put("WWNA", "Wenago A", "WWNA", "ETB", 1, 1, 1, 1, 0, 1, 10);
        put("WKCA", "Kochere A", "WKCA", "ETB", 1, 1, 1, 1, 0, 1, 10);
        put("WGAA", "Gelena Abaya A", "WGAA", "ETB", 1, 1, 1, 1, 0, 1, 10);
    }

    private MockContr() {
    }

    @SuppressWarnings("null")
    public static @NonNull Contr newContr(String mnem) {
        return MAP.get(mnem).call();
    }

    public static SlNode selectContr() {
        final SlQueue q = new SlQueue();
        for (final NullaryFunction<Contr> entry : LIST) {
            q.insertBack(entry.call());
        }
        return q.getFirst();
    }

    public static void selectContr(UnaryCallback<Contr> cb) {
        for (final NullaryFunction<Contr> entry : LIST) {
            cb.call(entry.call());
        }
    }
}
