/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Quote;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.JslNode;

/**
 * Transaction journal.
 * 
 * @author Mark Aylett
 */
public interface Journ extends AutoCloseable {

    void createMarket(@NonNull String mnem, @Nullable String display, @NonNull String contr,
            int settlDay, int expiryDay, int state);

    void updateMarket(@NonNull String mnem, @Nullable String display, int state)
            throws NotFoundException;

    void createTrader(@NonNull String mnem, @Nullable String display, @NonNull String email);

    void updateTrader(@NonNull String mnem, @Nullable String display) throws NotFoundException;

    void createExec(@NonNull Exec exec) throws NotFoundException;

    /**
     * Archive list of executions. The list may be modified asynchronously by this operation.
     * 
     * @param market
     *            The market.
     * @param first
     *            The first execution.
     * @throws NotFoundException
     */
    void createExecList(@NonNull String market, @NonNull JslNode first) throws NotFoundException;

    /**
     * This overload may be less efficient than the ones that are market-specific. The list may be
     * modified asynchronously by this operation.
     * 
     * @param first
     *            The first execution.
     * @throws NotFoundException
     */
    void createExecList(@NonNull JslNode first) throws NotFoundException;

    void createQuote(@NonNull Quote quote) throws NotFoundException;

    void archiveOrder(@NonNull String market, long id, long modified) throws NotFoundException;

    /**
     * Archive list of orders. The list must not be modified by this operation.
     * 
     * @param market
     *            The market.
     * @param first
     *            The first market-id.
     * @param modified
     *            The modification time.
     * @throws NotFoundException
     */
    void archiveOrderList(@NonNull String market, @NonNull JslNode first, long modified)
            throws NotFoundException;

    /**
     * This overload may be less efficient than the ones that are market-specific. The list must not
     * be modified by this operation.
     * 
     * @param first
     *            The first market-id node.
     * @param modified
     *            The modification time.
     * @throws NotFoundException
     */
    void archiveOrderList(@NonNull JslNode first, long modified) throws NotFoundException;

    void archiveTrade(@NonNull String market, long id, long modified) throws NotFoundException;

    /**
     * Archive list of trades. The list must not be modified by this operation.
     * 
     * @param market
     *            The market.
     * @param first
     *            The first market-id node.
     * @param modified
     *            The modification time.
     * @throws NotFoundException
     */
    void archiveTradeList(@NonNull String market, @NonNull JslNode first, long modified)
            throws NotFoundException;

    /**
     * This overload may be less efficient than the ones that are market-specific. The list must not
     * be modified by this operation.
     * 
     * @param first
     *            The first market-id node.
     * @param modified
     *            The modification time.
     * @throws NotFoundException
     */
    void archiveTradeList(@NonNull JslNode first, long modified) throws NotFoundException;
}
