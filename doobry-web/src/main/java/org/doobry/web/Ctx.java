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

public final class Ctx {
    private static class CtxHolder {
        private static final Ctx INSTANCE = new Ctx();
    }

    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    private Ctx() {
    }

    public static Ctx getInstance() {
        return CtxHolder.INSTANCE;
    }

    public final Future<String> foo() {
        return pool.submit(new Callable<String>() {
            @Override
            public final String call() {
                return "foo";
            }
        });
    }
}
