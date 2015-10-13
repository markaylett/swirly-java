/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.rest.RestUtil.getViewsParam;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.OrderNotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.rec.Rec;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class FrontRest implements Rest {

    private final Model model;
    private final Factory factory;

    private final MnemRbTree selectRec(RecType recType) throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            switch (recType) {
            case ASSET:
                tree = model.selectAsset(factory);
                break;
            case CONTR:
                tree = model.selectContr(factory);
                break;
            case MARKET:
                tree = model.selectMarket(factory);
                break;
            case TRADER:
                tree = model.selectTrader(factory);
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

    private final MnemRbTree selectAsset() throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            tree = model.selectAsset(factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final MnemRbTree selectContr() throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            tree = model.selectContr(factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final MnemRbTree selectMarket() throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            tree = model.selectMarket(factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final MnemRbTree selectTrader() throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            tree = model.selectTrader(factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final MnemRbTree selectView() throws ServiceUnavailableException {
        MnemRbTree tree = null;
        try {
            tree = model.selectView(factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final InstructTree selectOrder(String trader) throws ServiceUnavailableException {
        InstructTree tree = null;
        try {
            tree = model.selectOrder(trader, factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final InstructTree selectTrade(String trader) throws ServiceUnavailableException {
        InstructTree tree = null;
        try {
            tree = model.selectTrade(trader, factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    private final TraderPosnTree selectPosn(String trader, int busDay)
            throws ServiceUnavailableException {
        TraderPosnTree tree = null;
        try {
            tree = model.selectPosn(trader, busDay, factory);
        } catch (final InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("service interrupted", e);
        }
        assert tree != null;
        return tree;
    }

    public FrontRest(Model model, Factory factory) {
        this.model = model;
        this.factory = factory;
    }

    @Override
    public final @Nullable String findTraderByEmail(String email)
            throws ServiceUnavailableException {
        try {
            return model.selectTraderByEmail(email, factory);
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
        toJsonArray(selectAsset().getFirst(), params, out);
        out.append(",\"contrs\":");
        toJsonArray(selectContr().getFirst(), params, out);
        out.append(",\"markets\":");
        toJsonArray(selectMarket().getFirst(), params, out);
        if (withTraders) {
            out.append(",\"traders\":");
            toJsonArray(selectTrader().getFirst(), params, out);
        }
        out.append('}');
    }

    @Override
    public final void getRec(RecType recType, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        final MnemRbTree tree = selectRec(recType);
        toJsonArray(tree.getFirst(), params, out);
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final MnemRbTree tree = selectRec(recType);
        final Rec rec = (Rec) tree.find(mnem);
        if (rec == null) {
            throw new NotFoundException(String.format("record '%s' does not exist", mnem));
        }
        rec.toJson(params, out);
    }

    @Override
    public final void getView(Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        toJsonArray(selectView().getFirst(), params, out);
    }

    @Override
    public final void getView(String market, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        RestUtil.getView(selectView().getFirst(), market, params, out);
    }

    @Override
    public final void getSess(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        out.append("{\"orders\":");
        toJsonArray(selectOrder(trader).getFirst(), params, out);
        out.append(",\"trades\":");
        toJsonArray(selectTrade(trader).getFirst(), params, out);
        out.append(",\"posns\":");
        final int busDay = getBusDate(now).toJd();
        toJsonArray(selectPosn(trader, busDay).getFirst(), params, out);
        if (getViewsParam(params)) {
            out.append(",\"views\":");
            toJsonArray(selectView().getFirst(), params, out);
        }
        out.append('}');
    }

    @Override
    public final void getOrder(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        toJsonArray(selectOrder(trader).getFirst(), params, out);
    }

    @Override
    public final void getOrder(String trader, String market, Params params, long now,
            Appendable out) throws ServiceUnavailableException, IOException {
        RestUtil.getOrder(selectOrder(trader).getFirst(), market, params, out);
    }

    @Override
    public final void getOrder(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final InstructTree tree = selectOrder(trader);
        final Order order = (Order) tree.find(market, id);
        if (order == null) {
            throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
        }
        order.toJson(params, out);
    }

    @Override
    public final void getTrade(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        toJsonArray(selectTrade(trader).getFirst(), params, out);
    }

    @Override
    public final void getTrade(String trader, String market, Params params, long now,
            Appendable out) throws ServiceUnavailableException, IOException {
        RestUtil.getTrade(selectTrade(trader).getFirst(), market, params, out);
    }

    @Override
    public final void getTrade(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final InstructTree tree = selectTrade(trader);
        final Exec trade = (Exec) tree.find(market, id);
        if (trade == null) {
            throw new NotFoundException(String.format("trade '%d' does not exist", id));
        }
        trade.toJson(params, out);
    }

    @Override
    public final void getPosn(String trader, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        final int busDay = getBusDate(now).toJd();
        toJsonArray(selectPosn(trader, busDay).getFirst(), params, out);
    }

    @Override
    public final void getPosn(String trader, String contr, Params params, long now, Appendable out)
            throws ServiceUnavailableException, IOException {
        RestUtil.getPosn(selectTrade(trader).getFirst(), contr, params, out);
    }

    @Override
    public final void getPosn(String trader, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final int busDay = getBusDate(now).toJd();
        final TraderPosnTree tree = selectPosn(trader, busDay);
        final Posn posn = (Posn) tree.find(contr, maybeIsoToJd(settlDate));
        if (posn == null) {
            throw new NotFoundException(
                    String.format("posn for '%s' on '%d' does not exist", contr, settlDate));
        }
        posn.toJson(params, out);
    }
}
