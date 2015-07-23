/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.node.SlUtil.popNext;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class FrontRest implements Rest {

    private final Model model;
    @SuppressWarnings("unused")
    private final Cache cache;

    private final MnemRbTree makeTree(@Nullable SlNode first) {
        final MnemRbTree tree = new MnemRbTree();
        for (SlNode slNode = first; slNode != null;) {
            final RbNode rbNode = (RbNode) slNode;
            slNode = popNext(slNode);

            final RbNode unused = tree.insert(rbNode);
            assert unused == null;
        }
        return tree;
    }

    private final MnemRbTree getRecTree(RecType recType) {
        MnemRbTree tree;
        switch (recType) {
        case ASSET:
            tree = makeTree(model.selectAsset());
            break;
        case CONTR:
            tree = makeTree(model.selectContr());
            break;
        case MARKET:
            tree = makeTree(model.selectMarket());
            break;
        case TRADER:
            tree = makeTree(model.selectTrader());
            break;
        default:
            assert false;
            tree = new MnemRbTree();
            break;
        }
        return tree;
    }

    private final void doGetRec(RecType recType, Params params, Appendable out) throws IOException {
        final MnemRbTree tree = getRecTree(recType);
        toJsonArray(tree.getFirst(), params, out);
    }

    public FrontRest(Model model, Cache cache) {
        this.model = model;
        this.cache = cache;
    }

    @Override
    public final void getRec(boolean withTraders, Params params, long now, Appendable out)
            throws IOException {
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
            throws IOException {
        doGetRec(recType, params, out);
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
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
    public final void getView(String marketMnem, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getView");
    }

    @Override
    public final void getSess(String email, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getSess");
    }

    @Override
    public final void getOrder(String email, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getOrder");
    }

    @Override
    public final void getOrder(String email, String market, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getOrder");
    }

    @Override
    public final void getOrder(String email, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException {
        throw new UnsupportedOperationException("getOrder");
    }

    @Override
    public final void getTrade(String email, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getTrade");
    }

    @Override
    public final void getTrade(String email, String market, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getTrade");
    }

    @Override
    public final void getTrade(String email, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException {
        throw new UnsupportedOperationException("getTrade");
    }

    @Override
    public final void getPosn(String email, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getPosn");
    }

    @Override
    public final void getPosn(String email, String contr, Params params, long now, Appendable out)
            throws NotFoundException {
        throw new UnsupportedOperationException("getPosn");
    }

    @Override
    public final void getPosn(String email, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException {
        throw new UnsupportedOperationException("getPosn");
    }
}
