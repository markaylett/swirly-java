/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.book;

import static com.swirlycloud.twirly.util.TimeUtil.now;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.swirlycloud.twirly.book.BookSide;
import com.swirlycloud.twirly.book.Level;
import com.swirlycloud.twirly.date.JulianDay;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.entity.BasicFactory;
import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.Order;
import com.swirlycloud.twirly.node.DlNode;
import com.swirlycloud.twirly.node.RbNode;

public final class BookSideTest {
    private static final Factory FACTORY = new BasicFactory();

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
        long now = now();
        // Two orders at the same price level.
        final Order apple = FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", JulianDay.isoToJd(20140314),
                1, "apple", Side.BUY, 10, 12345, 0, now);
        final Order orange = FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", JulianDay.isoToJd(20140314),
                2, "orange", Side.BUY, 20, 12345, 0, now);
        final BookSide side = new BookSide();

        apple.invalidate();
        orange.invalidate();

        // Place orders.
        ++now;
        side.createOrder(apple, now);
        side.createOrder(orange, now);

        assertEquals(State.NEW, apple.getState());
        assertEquals(10, apple.getResd());
        assertEquals(-1, apple.getQuotd());
        assertEquals(0, apple.getExec());
        assertEquals(0, apple.getCost());
        assertEquals(-1, apple.getLastTicks());
        assertEquals(-1, apple.getLastLots());
        assertEquals(now - 1, apple.getCreated());
        assertEquals(now, apple.getModified());

        assertEquals(1, size(side.getFirstLevel()));
        assertEquals(2, size(side.getFirstOrder()));

        assertEquals("apple", ((Order) side.getFirstOrder()).getRef());
        assertEquals("orange", ((Order) side.getLastOrder()).getRef());

        Level level = (Level) side.getFirstLevel();
        assert level != null;
        assertEquals(12345, level.getTicks());
        // Sum of lots.
        assertEquals(30, level.getResd());
        // Two orders at this price level.
        assertEquals(2, level.getCount());

        // Revise first order.
        ++now;
        side.reviseOrder(apple, 5, now);

        assertEquals(State.REVISE, apple.getState());
        assertEquals(5, apple.getResd());
        assertEquals(-1, apple.getQuotd());
        assertEquals(0, apple.getExec());
        assertEquals(0, apple.getCost());
        assertEquals(-1, apple.getLastTicks());
        assertEquals(-1, apple.getLastLots());
        assertEquals(now - 2, apple.getCreated());
        assertEquals(now, apple.getModified());

        assertEquals(1, size(side.getFirstLevel()));
        assertEquals(2, size(side.getFirstOrder()));

        assertEquals("apple", ((Order) side.getFirstOrder()).getRef());
        assertEquals("orange", ((Order) side.getLastOrder()).getRef());

        level = (Level) side.getFirstLevel();
        assert level != null;
        assertEquals(12345, level.getTicks());
        // Sum of lots.
        assertEquals(25, level.getResd());
        // Two orders at this price level.
        assertEquals(2, level.getCount());

        // Cancel second order.
        ++now;
        side.cancelOrder(orange, now);

        assertEquals(State.CANCEL, orange.getState());
        assertEquals(0, orange.getResd());
        assertEquals(0, orange.getExec());
        assertEquals(0, orange.getCost());
        assertEquals(-1, orange.getLastTicks());
        assertEquals(-1, orange.getLastLots());
        assertEquals(now - 3, orange.getCreated());
        assertEquals(now, orange.getModified());

        assertEquals(1, size(side.getFirstLevel()));
        assertEquals(1, size(side.getFirstOrder()));

        assertEquals("apple", ((Order) side.getFirstOrder()).getRef());
        assertEquals("apple", ((Order) side.getLastOrder()).getRef());

        level = (Level) side.getFirstLevel();
        assert level != null;
        assertEquals(12345, level.getTicks());
        assertEquals(5, level.getResd());
        assertEquals(1, level.getCount());
    }

    @Test
    public final void testLevels() {
        final long now = now();
        // Two orders at the same price level.
        final Order apple = FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", JulianDay.isoToJd(20140314),
                1, "apple", Side.BUY, 10, 12345, 0, now);
        final Order orange = FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", JulianDay.isoToJd(20140314),
                2, "orange", Side.BUY, 20, 12345, 0, now);
        // Best inserted last.
        final Order pear = FACTORY.newOrder("MARAYL", "EURUSD.MAR14", "EURUSD", JulianDay.isoToJd(20140314),
                3, "pear", Side.BUY, 25, 12346, 0, now);
        final BookSide side = new BookSide();

        side.createOrder(apple, now);
        side.createOrder(orange, now);
        side.createOrder(pear, now);

        assertEquals(2, size(side.getFirstLevel()));
        assertEquals(3, size(side.getFirstOrder()));

        assertEquals("pear", ((Order) side.getFirstOrder()).getRef());
        assertEquals("orange", ((Order) side.getLastOrder()).getRef());

        Level level = (Level) side.getFirstLevel();
        assert level != null;
        assertEquals(12346, level.getTicks());
        assertEquals(25, level.getResd());
        assertEquals(1, level.getCount());

        level = (Level) side.getLastLevel();
        assert level != null;
        assertEquals(12345, level.getTicks());
        assertEquals(30, level.getResd());
        assertEquals(2, level.getCount());
    }
}
