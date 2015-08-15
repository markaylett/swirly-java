/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.swirlycloud.twirly.node.RbNode;

public abstract class SerializableTest {

    protected static String toJsonString(RbNode node) throws IOException {
        final StringBuilder sb = new StringBuilder();
        toJsonArray(node, PARAMS_NONE, sb);
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
