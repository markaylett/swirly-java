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
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.MnemRbTree;

public final class MockContr {

    private final Factory factory;
    private final MockAsset mockAsset;
    private final List<NullaryFunction<Contr>> list = new ArrayList<>();
    private final Map<String, NullaryFunction<Contr>> map = new HashMap<>();

    private final void put(final @NonNull String mnem, final String display,
            final @NonNull String asset, final @NonNull String ccy, final int tickNumer,
            final int tickDenom, final int lotNumer, final int lotDenom, final int pipDp,
            final long minLots, final long maxLots) {
        final NullaryFunction<Contr> fn = new NullaryFunction<Contr>() {
            @Override
            public final Contr call() {
                return factory.newContr(mnem, display, mockAsset.newAsset(asset),
                        mockAsset.newAsset(ccy), tickNumer, tickDenom, lotNumer, lotDenom, pipDp,
                        minLots, maxLots);
            }
        };
        list.add(fn);
        map.put(mnem, fn);
    }

    public MockContr(Factory factory) {
        this.factory = factory;
        this.mockAsset = new MockAsset(factory);

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

    @SuppressWarnings("null")
    public final @NonNull Contr newContr(String mnem) {
        return map.get(mnem).call();
    }

    public final @NonNull MnemRbTree selectContr() {
        final MnemRbTree t = new MnemRbTree();
        for (final NullaryFunction<Contr> entry : list) {
            final Contr contr = entry.call();
            assert contr != null;
            t.insert(contr);
        }
        return t;
    }

    public final void selectContr(UnaryCallback<Contr> cb) {
        for (final NullaryFunction<Contr> entry : list) {
            cb.call(entry.call());
        }
    }
}
