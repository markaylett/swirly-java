/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class FrontRest implements Rest {

    private final Model model;

    private final MnemRbTree getRecTree(RecType recType) throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            switch (recType) {
            case ASSET:
                tree = model.selectAsset();
                break;
            case CONTR:
                tree = model.selectContr();
                break;
            case MARKET:
                tree = model.selectMarket();
                break;
            case TRADER:
                tree = model.selectTrader();
                break;
            }
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final InstructTree getOrderTree(String trader) throws ServiceUnavailableException {
        InstructTree tree = null;
        try {
            tree = model.selectOrder(trader);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final InstructTree getTradeTree(String trader) throws ServiceUnavailableException {
        InstructTree tree = null;
        try {
            tree = model.selectTrade(trader);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final TraderPosnTree getPosnTree(String trader, int busDay)
            throws ServiceUnavailableException {
        TraderPosnTree tree = null;
        try {
            tree = model.selectPosn(trader, busDay);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final void doGetRec(RecType recType, Params params, Appendable out)
            throws ServiceUnavailableException, IOException {
        final MnemRbTree tree = getRecTree(recType);
        toJsonArray(tree.getFirst(), params, out);
    }

    private final void doGetOrder(String trader, Params params, Appendable out)
            throws ServiceUnavailableException, IOException {
        final InstructTree tree = getOrderTree(trader);
        toJsonArray(tree.getFirst(), params, out);
    }

    private final void doGetTrade(String trader, Params params, Appendable out)
            throws ServiceUnavailableException, IOException {
        final InstructTree tree = getTradeTree(trader);
        toJsonArray(tree.getFirst(), params, out);
    }

    private final void doGetPosn(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        final int busDay = getBusDate(now).toJd();
        final TraderPosnTree tree = getPosnTree(trader, busDay);
        toJsonArray(tree.getFirst(), params, out);
    }

    public FrontRest(Model model) {
        this.model = model;
    }

    @Override
    public final @Nullable String findTraderByEmail(String email)
            throws ServiceUnavailableException {
        try {
            return model.selectTraderByEmail(email);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
    }

    @Override
    public final void getRec(boolean withTraders, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        out.append("{\"assets\":");
        doGetRec(RecType.ASSET, params, out);
        out.append(",\"contrs\":");
        doGetRec(RecType.CONTR, params, out);
        out.append(",\"markets\":");
        doGetRec(RecType.MARKET, params, out);
        if (withTraders) {
            out.append(",\"traders\":");
            doGetRec(RecType.TRADER, params, out);
        }
        out.append('}');

    }

    @Override
    public final void getRec(RecType recType, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        doGetRec(recType, params, out);
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final MnemRbTree tree = getRecTree(recType);
        final Rec rec = (Rec) tree.find(mnem);
        if (rec == null) {
            throw new NotFoundException(String.format("record '%s' does not exist", mnem));
        }
        rec.toJson(params, out);
    }

    @Override
    public final void getView(Params params, long now, Appendable out) {
        throw new UnsupportedOperationException("getView");
    }

    @Override
    public final void getView(String market, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getView");
    }

    @Override
    public final void getSess(String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        out.append("{\"orders\":");
        doGetOrder(mnem, params, out);
        out.append(",\"trades\":");
        doGetTrade(mnem, params, out);
        out.append(",\"posns\":");
        doGetPosn(mnem, params, now, out);
        out.append('}');
    }

    @Override
    public final void getOrder(String mnem, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getOrder");
    }

    @Override
    public final void getOrder(String mnem, String market, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getOrder");
    }

    @Override
    public final void getOrder(String mnem, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException {
        throw new UnsupportedOperationException("getOrder");
    }

    @Override
    public final void getTrade(String mnem, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getTrade");
    }

    @Override
    public final void getTrade(String mnem, String market, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getTrade");
    }

    @Override
    public final void getTrade(String mnem, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException {
        throw new UnsupportedOperationException("getTrade");
    }

    @Override
    public final void getPosn(String mnem, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getPosn");
    }

    @Override
    public final void getPosn(String mnem, String contr, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getPosn");
    }

    @Override
    public final void getPosn(String mnem, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException {
        throw new UnsupportedOperationException("getPosn");
    }
}
