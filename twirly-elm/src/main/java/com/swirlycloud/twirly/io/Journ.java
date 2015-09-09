/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.SlNode;

/**
 * Transaction journal.
 * 
 * @author Mark Aylett
 */
public interface Journ extends AutoCloseable {

    void insertMarket(@NonNull String mnem, @Nullable String display, @NonNull String contr,
            int settlDay, int expiryDay, int state);

    void updateMarket(@NonNull String mnem, @Nullable String display, int state)
            throws NotFoundException;

    void insertTrader(@NonNull String mnem, @Nullable String display, @NonNull String email);

    void updateTrader(@NonNull String mnem, @Nullable String display) throws NotFoundException;

    void insertExec(@NonNull Exec exec) throws NotFoundException;

    void insertExecList(@NonNull String market, @NonNull SlNode first) throws NotFoundException;

    /**
     * This overload may be less efficient than the ones that are market-specific.
     * 
     * @param first
     *            The first exec.
     * @throws NotFoundException
     */
    void insertExecList(@NonNull SlNode first) throws NotFoundException;

    void archiveOrder(@NonNull String market, long id, long modified) throws NotFoundException;

    void archiveTrade(@NonNull String market, long id, long modified) throws NotFoundException;
}
