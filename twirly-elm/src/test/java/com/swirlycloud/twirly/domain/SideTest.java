/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.intrusive.DlNode;
import com.swirlycloud.twirly.intrusive.RbNode;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockTrader;

public final class SideTest {
    private static final int size(RbNode node) {
        int n = 0;
        for (; node != null; node = node.rbNext()) {
            ++n;
        }
        return n;
    }

    private static final int size(DlNode node) {
        int n = 0;
        for (; !node.isEnd(); node = node.dlNext()) {
            ++n;
        }
        return n;
    }

    @Test
    public final void testOrders() {
        long now = System.currentTimeMillis();
        final Contr contr = MockContr.newContr("EURUSD");
        final Trader trader = MockTrader.newTrader("MARAYL");
        // Two orders at the same price level.
        final Order apple = new Order(1, trader, contr, ymdToJd(2014, 2, 14), "apple", Action.BUY,
                12345, 10, 0, now);
        final Order orange = new Order(2, trader, contr, ymdToJd(2014, 2, 14), "orange",
                Action.BUY, 12345, 20, 0, now);
        final Side side = new Side();

        apple.state = null;
        apple.resd = -1;
        apple.exec = -1;
        apple.lastTicks = -1;
        apple.lastLots = -1;

        orange.state = null;
        orange.resd = -1;
        orange.exec = -1;
        orange.lastTicks = -1;
        orange.lastLots = -1;

        // Place orders.
        ++now;
        side.placeOrder(apple, now);
        side.placeOrder(orange, now);

        assertEquals(State.NEW, apple.getState());
        assertEquals(10, apple.getResd());
        assertEquals(0, apple.getExec());
        assertEquals(-1, apple.getLastTicks());
        assertEquals(-1, apple.getLastLots());
        assertEquals(now - 1, apple.getCreated());
        assertEquals(now, apple.getModified());

        assertEquals(1, size(side.getFirstLevel()));
        assertEquals(2, size(side.getFirstOrder()));

        assertEquals("apple", ((Order) side.getFirstOrder()).getRef());
        assertEquals("orange", ((Order) side.getLastOrder()).getRef());

        Level level = (Level) side.getFirstLevel();
        assertEquals(12345, level.getTicks());
        // Sum of lots.
        assertEquals(30, level.getLots());
        // Two orders at this price level.
        assertEquals(2, level.getCount());

        // Revise first order.
        ++now;
        side.reviseOrder(apple, 5, now);

        assertEquals(State.REVISE, apple.getState());
        assertEquals(5, apple.getResd());
        assertEquals(0, apple.getExec());
        assertEquals(-1, apple.getLastTicks());
        assertEquals(-1, apple.getLastLots());
        assertEquals(now - 2, apple.getCreated());
        assertEquals(now, apple.getModified());

        assertEquals(1, size(side.getFirstLevel()));
        assertEquals(2, size(side.getFirstOrder()));

        assertEquals("apple", ((Order) side.getFirstOrder()).getRef());
        assertEquals("orange", ((Order) side.getLastOrder()).getRef());

        level = (Level) side.getFirstLevel();
        assertEquals(12345, level.getTicks());
        // Sum of lots.
        assertEquals(25, level.getLots());
        // Two orders at this price level.
        assertEquals(2, level.getCount());

        // Cancel second order.
        ++now;
        side.cancelOrder(orange, now);

        assertEquals(State.CANCEL, orange.getState());
        assertEquals(0, orange.getResd());
        assertEquals(0, orange.getExec());
        assertEquals(-1, orange.getLastTicks());
        assertEquals(-1, orange.getLastLots());
        assertEquals(now - 3, orange.getCreated());
        assertEquals(now, orange.getModified());

        assertEquals(1, size(side.getFirstLevel()));
        assertEquals(1, size(side.getFirstOrder()));

        assertEquals("apple", ((Order) side.getFirstOrder()).getRef());
        assertEquals("apple", ((Order) side.getLastOrder()).getRef());

        level = (Level) side.getFirstLevel();
        assertEquals(12345, level.getTicks());
        assertEquals(5, level.getLots());
        assertEquals(1, level.getCount());
    }

    @Test
    public final void testLevels() {
        final long now = System.currentTimeMillis();
        final Contr contr = MockContr.newContr("EURUSD");
        final Trader trader = MockTrader.newTrader("MARAYL");
        // Two orders at the same price level.
        final Order apple = new Order(1, trader, contr, ymdToJd(2014, 2, 14), "apple", Action.BUY,
                12345, 10, 0, now);
        final Order orange = new Order(2, trader, contr, ymdToJd(2014, 2, 14), "orange",
                Action.BUY, 12345, 20, 0, now);
        // Best inserted last.
        final Order pear = new Order(3, trader, contr, ymdToJd(2014, 2, 14), "pear", Action.BUY,
                12346, 25, 0, now);
        final Side side = new Side();

        side.placeOrder(apple, now);
        side.placeOrder(orange, now);
        side.placeOrder(pear, now);

        assertEquals(2, size(side.getFirstLevel()));
        assertEquals(3, size(side.getFirstOrder()));

        assertEquals("pear", ((Order) side.getFirstOrder()).getRef());
        assertEquals("orange", ((Order) side.getLastOrder()).getRef());

        Level level = (Level) side.getFirstLevel();
        assertEquals(12346, level.getTicks());
        assertEquals(25, level.getLots());
        assertEquals(1, level.getCount());

        level = (Level) side.getLastLevel();
        assertEquals(12345, level.getTicks());
        assertEquals(30, level.getLots());
        assertEquals(2, level.getCount());
    }
}
