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
        put(i++, "BJONES", "Bobby Jones", "bobby.jones@doobry.org");
        put(i++, "EEDWARDS", "Emily Edwards", "emily.edwards@doobry.org");
        put(i++, "GWILSON", "George Wilson", "george.wilson@doobry.org");
        put(i++, "JTHOMAS", "Joseph Thomas", "joseph.thomas@doobry.org");
        put(i++, "JWRIGHT", "Juan Wright", "juan.wright@doobry.org");
        put(i++, "RALEXAND", "Raymond Alexander", "raymond.alexander@doobry.org");
        put(i++, "SFLORES", "Steven Flores", "steven.flores@doobry.org");
        put(i++, "TLEE", "Todd Lee", "todd.lee@doobry.org");
        put(i++, "VCAMPBEL", "Virginia Campbell", "virginia.campbell@doobry.org");
        put(i++, "WRAMIREZ", "Wayne Ramirez", "wayne.ramirez@doobry.org");
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
