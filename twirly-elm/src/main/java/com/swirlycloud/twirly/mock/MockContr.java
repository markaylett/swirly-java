/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;

public final class MockContr {

    private static final List<NullaryFunction<Contr>> LIST = new ArrayList<>();
    private static final Map<String, NullaryFunction<Contr>> MAP = new HashMap<>();

    private static void put(final String mnem, final String display, final String asset,
            final String ccy, final int tickNumer, final int tickDenom, final int lotNumer,
            final int lotDenom, final int pipDp, final long minLots, final long maxLots) {
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
        put("AUDUSD", "AUDUSD", "AUD", "USD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("EURCHF", "EURCHF", "EUR", "CHF", 1, 10000, 1000000, 1, 4, 1, 10);
        put("EURCZK", "EURCZK", "EUR", "CZK", 1, 100, 1000000, 1, 2, 1, 10);
        put("EURDKK", "EURDKK", "EUR", "DKK", 1, 1000, 1000000, 1, 3, 1, 10);
        put("EURGBP", "EURGBP", "EUR", "GBP", 1, 10000, 1000000, 1, 4, 1, 10);
        put("EURHUF", "EURHUF", "EUR", "HUF", 1, 100, 1000000, 1, 2, 1, 10);
        put("EURJPY", "EURJPY", "EUR", "JPY", 1, 100, 1000000, 1, 2, 1, 10);
        put("EURNOK", "EURNOK", "EUR", "NOK", 1, 1000, 1000000, 1, 3, 1, 10);
        put("EURPLN", "EURPLN", "EUR", "PLN", 1, 1000, 1000000, 1, 3, 1, 10);
        put("EURRON", "EURRON", "EUR", "RON", 1, 1000, 1000000, 1, 3, 1, 10);
        put("EURSEK", "EURSEK", "EUR", "SEK", 1, 1000, 1000000, 1, 3, 1, 10);
        put("EURUSD", "EURUSD", "EUR", "USD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("GBPUSD", "GBPUSD", "GBP", "USD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("NZDUSD", "NZDUSD", "NZD", "USD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("USDCAD", "USDCAD", "USD", "CAD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("USDCHF", "USDCHF", "USD", "CHF", 1, 10000, 1000000, 1, 4, 1, 10);
        put("USDHKD", "USDHKD", "USD", "HKD", 1, 1000, 1000000, 1, 3, 1, 10);
        put("USDILS", "USDILS", "USD", "ILS", 1, 1000, 1000000, 1, 3, 1, 10);
        put("USDJPY", "USDJPY", "USD", "JPY", 1, 100, 1000000, 1, 2, 1, 10);
        put("USDMXN", "USDMXN", "USD", "MXN", 1, 1000, 1000000, 1, 3, 1, 10);
        put("USDSGD", "USDSGD", "USD", "SGD", 1, 10000, 1000000, 1, 4, 1, 10);
        put("USDTHB", "USDTHB", "USD", "THB", 1, 100, 1000000, 1, 2, 1, 10);
        put("USDTRY", "USDTRY", "USD", "TRY", 1, 1000, 1000000, 1, 4, 1, 10);
        put("USDZAR", "USDZAR", "USD", "ZAR", 1, 1000, 1000000, 1, 3, 1, 10);
        put("CAP", "Central Appalachia Coal", "CAP", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("NAP", "Northern Appalachia Coal", "NAP", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("ILB", "Illinois Basin Coal", "ILB", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("PRB", "Powder River Basin Coal", "PRB", "USD", 1, 20, 1000, 1, 2, 1, 10);
        put("UIB", "Uinta Basin Coal", "UIB", "USD", 1, 20, 1000, 1, 2, 1, 10);
    }

    private MockContr() {
    }

    public static Contr newContr(String mnem) {
        return MAP.get(mnem).call();
    }

    public static void selectContr(UnaryCallback<Contr> cb) {
        for (final NullaryFunction<Contr> entry : LIST) {
            cb.call(entry.call());
        }
    }
}
