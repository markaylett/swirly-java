/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Action;
import org.doobry.domain.Contr;
import org.doobry.domain.Direct;
import org.doobry.domain.Exec;
import org.doobry.domain.OrdIdx;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Reg;
import org.doobry.domain.Role;
import org.doobry.domain.Side;
import org.doobry.util.Bank;
import org.doobry.util.DlNode;

public final class Broker {
    private Broker() {
    }

    private static long spread(Order takerOrder, Order makerOrder, Direct direct) {
        return direct == Direct.PAID
        // Paid when the taker lifts the offer.
        ? makerOrder.getTicks() - takerOrder.getTicks()
                // Given when the taker hits the bid.
                : takerOrder.getTicks() - makerOrder.getTicks();
    }

    private static void matchOrders(Book book, Order takerOrder, Side side, Direct direct,
            Bank bank, OrdIdx ordIdx, Trans trans) {

        final long now = takerOrder.getCreated();

        long taken = 0;
        long lastTicks = 0;
        long lastLots = 0;

        final Contr contr = book.getContr();
        final int settlDay = book.getSettlDay();

        DlNode node = side.getFirstOrder();
        for (; taken < takerOrder.getResd() && !node.isEnd(); node = node.dlNext()) {
            final Order makerOrder = (Order) node;

            // Only consider orders while prices cross.
            if (spread(takerOrder, makerOrder, direct) > 0)
                break;

            final long matchId = bank.addFetch(Reg.MATCH_ID.intValue(), 1L);
            final long takerId = bank.addFetch(Reg.EXEC_ID.intValue(), 1L);
            final long makerId = bank.addFetch(Reg.EXEC_ID.intValue(), 1L);

            final Accnt makerAccnt = Accnt.getLazyAccnt(makerOrder.getGiveup(), ordIdx);
            final Posn makerPosn = makerAccnt.getLazyPosn(contr, settlDay);

            final Match match = new Match(matchId);
            match.makerOrder = makerOrder;
            match.makerPosn = makerPosn;
            match.ticks = makerOrder.getTicks();
            match.lots = Math.min(takerOrder.getResd() - taken, makerOrder.getResd());

            taken += match.lots;
            lastTicks = match.ticks;
            lastLots = match.lots;

            final Exec makerTrade = new Exec(makerId, makerOrder.getId(), makerOrder, now);
            makerTrade.trade(match.lots, match.ticks, match.lots, match.id, Role.MAKER,
                    takerOrder.getGiveup());
            match.makerTrade = makerTrade;

            final Exec takerTrade = new Exec(takerId, takerOrder.getId(), takerOrder, now);
            takerTrade.trade(taken, match.ticks, match.lots, match.id, Role.TAKER,
                    makerOrder.getGiveup());
            match.takerTrade = takerTrade;

            trans.matches.insertBack(match);

            // Maker updated first because this is consistent with last-look semantics.
            // N.B. the reference count is not incremented here.
            trans.execs.insertBack(makerTrade);
            trans.execs.insertBack(takerTrade);
        }

        if (!trans.matches.isEmpty()) {
            // Avoid allocating position when there are no matches.
            final Accnt takerAccnt = Accnt.getLazyAccnt(takerOrder.getGiveup(), ordIdx);
            trans.takerPosn = takerAccnt.getLazyPosn(contr, settlDay);
            takerOrder.trade(taken, lastTicks, lastLots, now);
        }
    }

    public static void matchOrders(Book book, Order taker, Bank bank, OrdIdx ordIdx, Trans trans) {
        Side side;
        Direct direct;
        if (taker.getAction() == Action.BUY) {
            // Paid when the taker lifts the offer.
            side = book.getOfferSide();
            direct = Direct.PAID;
        } else {
            assert taker.getAction() == Action.SELL;
            // Given when the taker hits the bid.
            side = book.getBidSide();
            direct = Direct.GIVEN;
        }
        matchOrders(book, taker, side, direct, bank, ordIdx, trans);
    }
}
