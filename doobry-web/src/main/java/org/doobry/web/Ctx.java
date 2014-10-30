/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.doobry.domain.Rec;
import org.doobry.domain.RecType;
import org.doobry.domain.Reg;
import org.doobry.engine.Serv;
import org.doobry.mock.MockBank;
import org.doobry.mock.MockJourn;
import org.doobry.mock.MockModel;
import org.doobry.util.SlNode;

public final class Ctx {
    private static class CtxHolder {
        private static final Ctx INSTANCE = new Ctx();
    }

    private final ExecutorService pool;
    private final Serv serv;

    private Ctx() {
        pool = Executors.newFixedThreadPool(1);
        serv = new Serv(new MockBank(Reg.values().length), new MockJourn());
        serv.load(new MockModel());
    }

    public static Ctx getInstance() {
        return CtxHolder.INSTANCE;
    }

    public final Future<String> getRec(final RecType type) {
        return pool.submit(new Callable<String>() {
            @Override
            public final String call() {
                final StringBuilder sb = new StringBuilder();
                sb.append('[');
                SlNode node = serv.getFirstRec(type);
                for (int i = 0; node != null; node = node.slNext()) {
                    final Rec rec = (Rec) node;
                    if (i++ > 0) {
                        sb.append(',');
                    }
                    rec.print(sb);
                }
                sb.append(']');
                return sb.toString();
            }
        });
    }
}
