/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;

public final class MockTrader {
    private static final Map<String, NullaryFunction<Trader>> FACTORIES = new TreeMap<>();

    private static void put(final long id, final String mnem, final String display,
            final String email) {
        FACTORIES.put(mnem, new NullaryFunction<Trader>() {
            @Override
            public final Trader call() {
                return new Trader(id, mnem, display, email);
            }
        });
    }

    static {
        int i = 1;
        put(i++, "MARAYL", "Mark Aylett", "mark.aylett@gmail.com");
        put(i++, "GOSAYL", "Goska Aylett", "goska.aylett@gmail.com");
        put(i++, "TOBAYL", "Toby Aylett", "toby.aylett@gmail.com");
        put(i++, "EMIAYL", "Emily Aylett", "emily.aylett@gmail.com");
        put(i++, "SWIRLY", "Swirly Cloud", "mark.aylett@swirlycloud.com");
    }

    private MockTrader() {
    }

    public static Trader newTrader(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static void selectTrader(UnaryCallback<Trader> cb) {
        for (final Entry<String, NullaryFunction<Trader>> entry : FACTORIES.entrySet()) {
            cb.call(entry.getValue().call());
        }
    }
}
