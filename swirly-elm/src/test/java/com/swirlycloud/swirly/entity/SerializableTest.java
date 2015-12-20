/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import static com.swirlycloud.swirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.swirly.util.JsonUtil.toJsonArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.swirlycloud.swirly.entity.Entity;

public abstract class SerializableTest {

    protected static String toJsonString(Entity entity) throws IOException {
        final StringBuilder sb = new StringBuilder();
        toJsonArray(entity, PARAMS_NONE, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected static <T> T writeAndRead(T obj) throws ClassNotFoundException, IOException {

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(bout);

        out.writeObject(obj);

        final byte[] arr = bout.toByteArray();
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(arr));
        return (T) in.readObject();
    }
}
