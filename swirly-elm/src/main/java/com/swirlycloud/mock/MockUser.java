/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.mock;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.User;
import com.swirlycloud.function.NullaryFunction;
import com.swirlycloud.function.UnaryCallback;

public final class MockUser {
    private static final Map<String, NullaryFunction<User>> FACTORIES = new TreeMap<>();

    private static void put(final long id, final String mnem, final String display,
            final String email) {
        FACTORIES.put(mnem, new NullaryFunction<User>() {
            @Override
            public final User call() {
                return new User(id, mnem, display, email);
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

    private MockUser() {
    }

    public static User newUser(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static void selectUser(UnaryCallback<Rec> cb) {
        for (final Entry<String, NullaryFunction<User>> entry : FACTORIES.entrySet()) {
            cb.call(entry.getValue().call());
        }
    }
}
