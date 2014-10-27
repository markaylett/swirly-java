/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.doobry.domain.Party;
import org.doobry.function.NullaryFunction;
import org.doobry.util.Queue;

public final class MockParty {
    private static final Map<String, NullaryFunction<Party>> FACTORIES = new HashMap<String, NullaryFunction<Party>>();

    private static void put(final long id, final String mnem, final String display,
            final String email) {
        FACTORIES.put(mnem, new NullaryFunction<Party>() {
            @Override
            public final Party call() {
                return new Party(id, mnem, display, email);
            }
        });
    }

    static {
        int i = 1;
        put(i++, "WRAMIREZ", "Wayne Ramirez", "wayne.ramirez@doobry.org");
        put(i++, "SFLORES", "Steven Flores", "steven.flores@doobry.org");
        put(i++, "JWRIGHT", "Juan Wright", "juan.wright@doobry.org");
        put(i++, "VCAMPBEL", "Virginia Campbell", "virginia.campbell@doobry.org");
        put(i++, "GWILSON", "George Wilson", "george.wilson@doobry.org");
        put(i++, "BJONES", "Bobby Jones", "bobby.jones@doobry.org");
        put(i++, "TLEE", "Todd Lee", "todd.lee@doobry.org");
        put(i++, "EEDWARDS", "Emily Edwards", "emily.edwards@doobry.org");
        put(i++, "RALEXAND", "Raymond Alexander", "raymond.alexander@doobry.org");
        put(i++, "JTHOMAS", "Joseph Thomas", "joseph.thomas@doobry.org");
        put(i++, "DBRA", "Account A", "dbra@doobry.org");
        put(i++, "DBRB", "Account B", "dbrb@doobry.org");
    }

    private MockParty() {
    }

    public static Party newParty(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static Party[] newPartyArray() {
        int i = 0;
        final Party[] arr = new Party[FACTORIES.size()];
        for (final Entry<String, NullaryFunction<Party>> entry : FACTORIES.entrySet()) {
            arr[i++] = entry.getValue().call();
        }
        return arr;
    }

    public static Party newPartyList() {
        final Queue q = new Queue();
        for (final Entry<String, NullaryFunction<Party>> entry : FACTORIES.entrySet()) {
            q.insertBack(entry.getValue().call());
        }
        return (Party) q.getFirst();
    }
}
