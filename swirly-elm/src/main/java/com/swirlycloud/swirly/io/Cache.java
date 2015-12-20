/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.io;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault interface Cache extends AutoCloseable {

    /**
     * Read the specified cache entry.
     * 
     * @param key
     *            The cache entry key.
     * @return The cache entry value or null if it does not exist.
     */
    Future<?> read(String key);

    Future<Map<String, Object>> read(Collection<String> keys);

    /**
     * Create cache entry if it does not already exist.
     * 
     * @param key
     *            The cache entry key.
     * @param val
     *            The cache entry value.
     */
    void create(String key, Object val);

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
