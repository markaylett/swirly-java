/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.function.UnaryFunction;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.rec.Contr;

public final class MockContr {

    private static List<UnaryFunction<Contr, Factory>> LIST = new ArrayList<>();
    private static Map<String, UnaryFunction<Contr, Factory>> MAP = new HashMap<>();

    private static void put(final @NonNull String mnem, final String display,
            final @NonNull String asset, final @NonNull String ccy, final int tickNumer,
            final int tickDenom, final int lotNumer, final int lotDenom, final int pipDp,
            final long minLots, final long maxLots) {
        final UnaryFunction<Contr, Factory> fn = new UnaryFunction<Contr, Factory>() {
            @Override
            public final Contr call(Factory factory) {
                return factory.newContr(mnem, display, MockAsset.newAsset(asset, factory),
                        MockAsset.newAsset(ccy, factory), tickNumer, tickDenom, lotNumer, lotDenom,
                        pipDp, minLots, maxLots);
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
        // US Corporates.
        put("CSCO", "Cisco Systems Inc", "CSCO", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("DIS", "Walt Disney", "DIS", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("IBM", "Ibm Corp", "IBM", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("INTC", "Intel Corp", "INTC", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("MSFT", "Microsoft Corp", "MSFT", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("VIA", "Viacom Inc", "VIA", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("VOD", "Vodafone Group Plc", "VOD", "USD", 1, 1000, 1, 1, 3, 1, 10);
        put("VZ", "Verizon Com", "VZ", "USD", 1, 1000, 1, 1, 3, 1, 10);
    }

    private MockContr() {
    }

    @SuppressWarnings("null")
    public static @NonNull Contr newContr(String mnem, Factory factory) {
        return MAP.get(mnem).call(factory);
    }

    public static @NonNull MnemRbTree readContr(Factory factory) {
        final MnemRbTree t = new MnemRbTree();
        for (final UnaryFunction<Contr, Factory> entry : LIST) {
            final Contr contr = entry.call(factory);
            assert contr != null;
            t.insert(contr);
        }
        return t;
    }

    public static void readContr(Factory factory, UnaryCallback<Contr> cb) {
        for (final UnaryFunction<Contr, Factory> entry : LIST) {
            cb.call(entry.call(factory));
        }
    }
}
