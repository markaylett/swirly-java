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
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.MnemRbTree;

public final class MockTrader {

    private final Factory factory;
    private final List<NullaryFunction<Trader>> list = new ArrayList<>();
    private final Map<String, NullaryFunction<Trader>> map = new HashMap<>();

    private final void put(final @NonNull String mnem, final String display,
            final @NonNull String email) {
        final NullaryFunction<Trader> fn = new NullaryFunction<Trader>() {
            @Override
            public final Trader call() {
                return factory.newTrader(mnem, display, email);
            }
        };
        list.add(fn);
        map.put(mnem, fn);
    }

    public MockTrader(Factory factory) {
        this.factory = factory;
        put("MARAYL", "Mark Aylett", "mark.aylett@gmail.com");
        put("GOSAYL", "Goska Aylett", "goska.aylett@gmail.com");
        put("TOBAYL", "Toby Aylett", "toby.aylett@gmail.com");
        put("EMIAYL", "Emily Aylett", "emily.aylett@gmail.com");
        put("SWIRLY", "Swirly Cloud", "info@swirlycloud.com");
        put("RAMMAC", "Ram Macharaj", "ram.mac@gmail.com");
    }

    @SuppressWarnings("null")
    public final @NonNull Trader newTrader(String mnem) {
        return map.get(mnem).call();
    }

    public final @NonNull MnemRbTree selectTrader() {
        final MnemRbTree t = new MnemRbTree();
        for (final NullaryFunction<Trader> entry : list) {
            final Trader trader = entry.call();
            assert trader != null;
            t.insert(trader);
        }
        return t;
    }

    public final void selectTrader(UnaryCallback<Trader> cb) {
        for (final NullaryFunction<Trader> entry : list) {
            cb.call(entry.call());
        }
    }
}
