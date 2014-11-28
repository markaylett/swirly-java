/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.back.WebUtil.alternateEmail;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class WebUtilTest {

    @Test
    public final void testEmail() {
        assertEquals("emily.aylett@gmail.com", alternateEmail("emily.aylett@googlemail.com"));
    }
}
