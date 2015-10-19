/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault interface Cache extends AutoCloseable {

    /**
     * Select the specified cache entry.
     * 
     * @param key
     *            The cache entry key.
     * @return The cache entry value or null if it does not exist.
     */
    Future<?> select(String key);

    Future<Map<String, Object>> select(Collection<String> keys);

    /**
     * Insert cache entry if it does not already exist.
     * 
     * @param key
     *            The cache entry key.
     * @param val
     *            The cache entry value.
     */
    void insert(String key, Object val);

    /**
     * Update existing cache entry regardless of existing value.
     * 
     * @param key
     *            The cache entry key.
     * @param val
     *            The cache entry value.
     */
    void update(String key, Object val);

    /**
     * Delete cache entry.
     * 
     * @param key
     *            The cache entry key.
     */
    void delete(String key);
}
