package org.doobry.web;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class CtxTest {
    @Test
    public final void test() throws InterruptedException, ExecutionException {
        System.out.println(Ctx.getInstance().foo().get());
    }
}
