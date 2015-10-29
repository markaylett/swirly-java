/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.function.UnaryFunction;
import com.swirlycloud.twirly.intrusive.RecTree;
import com.swirlycloud.twirly.rec.Trader;

public final class MockTrader {

    private static List<UnaryFunction<Trader, Factory>> LIST = new ArrayList<>();
    private static Map<String, UnaryFunction<Trader, Factory>> MAP = new HashMap<>();
    private static Map<String, String> EMAIL_IDX = new HashMap<>();

    private static void put(final @NonNull String mnem, final String display,
            final @NonNull String email) {
        final UnaryFunction<Trader, Factory> fn = new UnaryFunction<Trader, Factory>() {
            @Override
            public final Trader call(Factory factory) {
                return factory.newTrader(mnem, display, email);
            }
        };
        LIST.add(fn);
        MAP.put(mnem, fn);
        EMAIL_IDX.put(email, mnem);
    }

    static {
        put("MARAYL", "Mark Aylett", "mark.aylett@gmail.com");
        put("GOSAYL", "Goska Aylett", "goska.aylett@gmail.com");
        put("TOBAYL", "Toby Aylett", "toby.aylett@gmail.com");
        put("EMIAYL", "Emily Aylett", "emily.aylett@gmail.com");
        put("SWIRLY", "Swirly Cloud", "info@swirlycloud.com");
        put("RAMMAC", "Ram Macharaj", "ram.mac@gmail.com");
    }

    private MockTrader() {
    }

    @SuppressWarnings("null")
    public static @NonNull Trader newTrader(String mnem, Factory factory) {
        return MAP.get(mnem).call(factory);
    }

    public static @NonNull RecTree readTrader(Factory factory) {
        final RecTree t = new RecTree();
        for (final UnaryFunction<Trader, Factory> entry : LIST) {
            final Trader trader = entry.call(factory);
            assert trader != null;
            t.insert(trader);
        }
        return t;
    }

    public static @Nullable String readTraderByEmail(String email, Factory factory) {
        return EMAIL_IDX.get(email);
    }

    public static void readTrader(Factory factory, UnaryCallback<Trader> cb) {
        for (final UnaryFunction<Trader, Factory> entry : LIST) {
            cb.call(entry.call(factory));
        }
    }
}
