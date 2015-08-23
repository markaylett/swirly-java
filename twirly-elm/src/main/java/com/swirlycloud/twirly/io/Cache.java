/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault interface Cache extends AutoCloseable {

    /**
     * Select the specified cache entry.
     * 
     * @param key
     *            The cache entry key.
     * @return The cache entry value or null if it does not exist.
     */
    @Nullable
    Object select(String key);

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
