/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.util.concurrent.ExecutionException;

import org.doobry.domain.RecType;
import org.junit.Test;

public class CtxTest {
    @Test
    public final void test() throws InterruptedException, ExecutionException {
        System.out.println(Ctx.getInstance().getRec(RecType.CONTR).get());
    }
}
