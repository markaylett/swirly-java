/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;

public final class MockTrader {
    private static final List<NullaryFunction<Trader>> LIST = new ArrayList<>();
    private static final Map<String, NullaryFunction<Trader>> MAP = new HashMap<>();

    private static void put(final String mnem, final String display, final String email) {
        final NullaryFunction<Trader> fn = new NullaryFunction<Trader>() {
            @Override
            public final Trader call() {
                return new Trader(mnem, display, email);
            }
        };
        LIST.add(fn);
        MAP.put(mnem, fn);
    }

    static {
        put("MARAYL", "Mark Aylett", "mark.aylett@gmail.com");
        put("GOSAYL", "Goska Aylett", "goska.aylett@gmail.com");
        put("TOBAYL", "Toby Aylett", "toby.aylett@gmail.com");
        put("EMIAYL", "Emily Aylett", "emily.aylett@gmail.com");
        put("SWIRLY", "Swirly Cloud", "info@swirlycloud.com");
    }

    private MockTrader() {
    }

    public static Trader newTrader(String mnem) {
        return MAP.get(mnem).call();
    }

    public static void selectTrader(UnaryCallback<Trader> cb) {
        for (final NullaryFunction<Trader> entry : LIST) {
            cb.call(entry.call());
        }
    }
}
