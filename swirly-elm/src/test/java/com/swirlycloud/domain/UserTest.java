/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.mock.MockUser;

public final class UserTest {
    @Test
    public final void test() {
        assertEquals(
                "{\"mnem\":\"MARAYL\",\"display\":\"Mark Aylett\",\"email\":\"mark.aylett@gmail.com\"}",
                MockUser.newUser("MARAYL").toString());
    }
}
