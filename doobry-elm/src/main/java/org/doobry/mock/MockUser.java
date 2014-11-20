/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.doobry.domain.User;
import org.doobry.function.NullaryFunction;
import org.doobry.util.Queue;

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
    }

    private MockUser() {
    }

    public static User newUser(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static User newUserList() {
        final Queue q = new Queue();
        for (final Entry<String, NullaryFunction<User>> entry : FACTORIES.entrySet()) {
            q.insertBack(entry.getValue().call());
        }
        return (User) q.getFirst();
    }
}
