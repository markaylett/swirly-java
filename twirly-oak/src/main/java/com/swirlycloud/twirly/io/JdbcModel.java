/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.intrusive.Container;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.intrusive.PosnTree;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.intrusive.TraderPosnTree;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.rec.Asset;
import com.swirlycloud.twirly.rec.AssetType;
import com.swirlycloud.twirly.rec.Contr;
import com.swirlycloud.twirly.rec.Market;
import com.swirlycloud.twirly.rec.Trader;
import com.swirlycloud.twirly.util.Memorable;

public class JdbcModel implements Model {
    @NonNull
    protected final Connection conn;
    @NonNull
    private final PreparedStatement selectAssetStmt;
    @NonNull
    private final PreparedStatement selectContrStmt;
    @NonNull
    private final PreparedStatement selectMarketStmt;
    @NonNull
    private final PreparedStatement selectTraderStmt;
    @NonNull
    private final PreparedStatement selectTraderByEmailStmt;
    @NonNull
    private final PreparedStatement selectOrderStmt;
    @NonNull
    private final PreparedStatement selectOrderByTraderStmt;
    @NonNull
    private final PreparedStatement selectTradeStmt;
    @NonNull
    private final PreparedStatement selectTradeByTraderStmt;
    @NonNull
    private final PreparedStatement selectPosnStmt;
    @NonNull
    private final PreparedStatement selectPosnByTraderStmt;

    private static @NonNull Asset getAsset(@NonNull ResultSet rs, @NonNull Factory factory)
            throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final AssetType assetType = AssetType.valueOf(rs.getInt("typeId"));
        assert mnem != null;
        assert assetType != null;
        return factory.newAsset(mnem, display, assetType);
    }

    private static @NonNull Contr getContr(@NonNull ResultSet rs, @NonNull Factory factory)
            throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        @SuppressWarnings("null")
        final Memorable asset = newMnem(rs.getString("asset"));
        @SuppressWarnings("null")
        final Memorable ccy = newMnem(rs.getString("ccy"));
        final int tickNumer = rs.getInt("tickNumer");
        final int tickDenom = rs.getInt("tickDenom");
        final int lotNumer = rs.getInt("lotNumer");
        final int lotDenom = rs.getInt("lotDenom");
        final int pipDp = rs.getInt("pipDp");
        final int minLots = rs.getInt("minLots");
        final int maxLots = rs.getInt("maxLots");

        assert mnem != null;
        return factory.newContr(mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom,
                pipDp, minLots, maxLots);
    }

    private static @NonNull Market getMarket(@NonNull ResultSet rs, @NonNull Factory factory)
            throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        @SuppressWarnings("null")
        final Memorable contr = newMnem(rs.getString("contr"));
        // getInt() returns zero if value is null.
        final int settlDay = rs.getInt("settlDay");
        // getInt() returns zero if value is null.
        final int expiryDay = rs.getInt("expiryDay");
        final int state = rs.getInt("state");
        final long lastTicks = rs.getLong("lastTicks");
        final long lastLots = rs.getLong("lastLots");
        final long lastTime = rs.getLong("lastTime");
        final long maxOrderId = rs.getLong("maxOrderId");
        final long maxExecId = rs.getLong("maxExecId");

        assert mnem != null;
        return factory.newMarket(mnem, display, contr, settlDay, expiryDay, state, lastTicks,
                lastLots, lastTime, maxOrderId, maxExecId);
    }

    private static @NonNull Trader getTrader(@NonNull ResultSet rs, @NonNull Factory factory)
            throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final String email = rs.getString("email");

        assert mnem != null;
        assert email != null;
        return factory.newTrader(mnem, display, email);
    }

    private static @NonNull Order getOrder(@NonNull ResultSet rs, @NonNull Factory factory)
            throws SQLException {
        final long id = rs.getLong("id");
        final String trader = rs.getString("trader");
        final String market = rs.getString("market");
        final String contr = rs.getString("contr");
        final int settlDay = rs.getInt("settlDay");
        final String ref = rs.getString("ref");
        final State state = State.valueOf(rs.getInt("stateId"));
        final Side side = Side.valueOf(rs.getInt("sideId"));
        final long ticks = rs.getLong("ticks");
        final long lots = rs.getLong("lots");
        final long resd = rs.getLong("resd");
        final long exec = rs.getLong("exec");
        final long cost = rs.getLong("cost");
        final long lastTicks = rs.getLong("lastTicks");
        final long lastLots = rs.getLong("lastLots");
        final long minLots = rs.getLong("minLots");
        final boolean pecan = rs.getBoolean("pecan");
        final long created = rs.getLong("created");
        final long modified = rs.getLong("modified");
        assert trader != null;
        assert market != null;
        assert contr != null;
        return factory.newOrder(id, trader, market, contr, settlDay, ref, state, side, ticks, lots,
                resd, exec, cost, lastTicks, lastLots, minLots, pecan, created, modified);
    }

    private static @NonNull Exec getTrade(@NonNull ResultSet rs, @NonNull Factory factory)
            throws SQLException {
        final long id = rs.getLong("id");
        // getLong() returns zero if value is null.
        final long orderId = rs.getLong("orderId");
        final String trader = rs.getString("trader");
        final String market = rs.getString("market");
        final String contr = rs.getString("contr");
        // getInt() returns zero if value is null.
        final int settlDay = rs.getInt("settlDay");
        final String ref = rs.getString("ref");
        final State state = State.TRADE;
        final Side side = Side.valueOf(rs.getInt("sideId"));
        final long ticks = rs.getLong("ticks");
        final long lots = rs.getLong("lots");
        final long resd = rs.getLong("resd");
        final long exec = rs.getLong("exec");
        final long cost = rs.getLong("cost");
        final long lastTicks = rs.getLong("lastTicks");
        final long lastLots = rs.getLong("lastLots");
        final long minLots = rs.getLong("minLots");
        final long matchId = rs.getLong("matchId");
        // If roleId is null in the database, then getInt() will return zero, and valueOf() will
        // return null.
        final Role role = Role.valueOf(rs.getInt("roleId"));
        final String cpty = rs.getString("cpty");
        final long created = rs.getLong("created");

        assert trader != null;
        assert market != null;
        assert contr != null;
        return factory.newExec(id, orderId, trader, market, contr, settlDay, ref, state, side,
                ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, role, cpty,
                created);
    }

    private static void readOrder(@NonNull PreparedStatement stmt, @NonNull Factory factory,
            @NonNull final Container<? super Order> c) throws SQLException {
        try (final ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                c.add(getOrder(rs, factory));
            }
        }
    }

    private final void readTrade(@NonNull PreparedStatement stmt, @NonNull Factory factory,
            @NonNull final Container<? super Exec> c) throws SQLException {
        try (final ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                c.add(getTrade(rs, factory));
            }
        }
    }

    private final void readPosn(@NonNull PreparedStatement stmt, int busDay,
            @NonNull Factory factory, @NonNull final Container<? super Posn> c)
                    throws SQLException {
        final PosnTree posns = new PosnTree();
        try (final ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                final String trader = rs.getString("trader");
                final String contr = rs.getString("contr");
                int settlDay = rs.getInt("settlDay");
                assert trader != null;
                assert contr != null;
                // FIXME: Consider time-of-day.
                if (settlDay != 0 && settlDay <= busDay) {
                    settlDay = 0;
                }
                // Lazy position.
                Posn posn = (Posn) posns.pfind(trader, contr, settlDay);
                if (posn == null || !posn.getTrader().equals(trader)
                        || !posn.getContr().equals(contr) || posn.getSettlDay() != settlDay) {
                    final RbNode parent = posn;
                    assert trader != null;
                    assert contr != null;
                    posn = factory.newPosn(trader, contr, settlDay);
                    posns.pinsert(posn, parent);
                }
                final Side side = Side.valueOf(rs.getInt("sideId"));
                final long cost = rs.getLong("cost");
                final long lots = rs.getLong("lots");
                if (side == Side.BUY) {
                    posn.addBuy(cost, lots);
                } else {
                    assert side == Side.SELL;
                    posn.addSell(cost, lots);
                }
            }
        }
        for (;;) {
            final Posn posn = (Posn) posns.getRoot();
            if (posn == null) {
                break;
            }
            posns.remove(posn);
            c.add(posn);
        }
    }

    protected static void setParam(PreparedStatement stmt, int i, int val) throws SQLException {
        stmt.setInt(i, val);
    }

    protected static void setNullIfZero(PreparedStatement stmt, int i, int val)
            throws SQLException {
        if (val != 0) {
            stmt.setInt(i, val);
        } else {
            stmt.setNull(i, Types.INTEGER);
        }
    }

    protected static void setParam(PreparedStatement stmt, int i, long val) throws SQLException {
        stmt.setLong(i, val);
    }

    protected static void setNullIfZero(PreparedStatement stmt, int i, long val)
            throws SQLException {
        if (val != 0) {
            stmt.setLong(i, val);
        } else {
            stmt.setNull(i, Types.INTEGER);
        }
    }

    protected static void setParam(PreparedStatement stmt, int i, String val) throws SQLException {
        stmt.setString(i, val);
    }

    protected static void setNullIfEmpty(PreparedStatement stmt, int i, String val)
            throws SQLException {
        if (val != null && !val.isEmpty()) {
            stmt.setString(i, val);
        } else {
            stmt.setNull(i, Types.CHAR);
        }
    }

    protected static void setParam(PreparedStatement stmt, int i, boolean val) throws SQLException {
        stmt.setBoolean(i, val);
    }

    public JdbcModel(String url, String user, String password) {
        Connection conn = null;
        PreparedStatement selectAssetStmt = null;
        PreparedStatement selectContrStmt = null;
        PreparedStatement selectMarketStmt = null;
        PreparedStatement selectTraderStmt = null;
        PreparedStatement selectTraderByEmailStmt = null;
        PreparedStatement selectOrderStmt = null;
        PreparedStatement selectOrderByTraderStmt = null;
        PreparedStatement selectTradeStmt = null;
        PreparedStatement selectTradeByTraderStmt = null;
        PreparedStatement selectPosnStmt = null;
        PreparedStatement selectPosnByTraderStmt = null;
        boolean success = false;
        try {
            try {
                conn = DriverManager.getConnection(url, user, password);
                selectAssetStmt = conn
                        .prepareStatement("SELECT mnem, display, typeId FROM Asset_t");
                selectContrStmt = conn.prepareStatement(
                        "SELECT mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots FROM Contr_v");
                selectMarketStmt = conn.prepareStatement(
                        "SELECT mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots, lastTime, maxOrderId, maxExecId FROM Market_v");
                selectTraderStmt = conn
                        .prepareStatement("SELECT mnem, display, email FROM Trader_t");
                selectTraderByEmailStmt = conn
                        .prepareStatement("SELECT mnem FROM Trader_t WHERE email = ?");
                selectOrderStmt = conn.prepareStatement(
                        "SELECT id, trader, market, contr, settlDay, ref, stateId, sideId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, pecan, created, modified FROM Order_t WHERE archive = 0 AND resd > 0");
                selectOrderByTraderStmt = conn.prepareStatement(
                        "SELECT id, trader, market, contr, settlDay, ref, stateId, sideId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, pecan, created, modified FROM Order_t WHERE trader = ? AND archive = 0 AND resd > 0");
                selectTradeStmt = conn.prepareStatement(
                        "SELECT id, orderId, trader, market, contr, settlDay, ref, sideId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, roleId, cpty, created FROM Exec_t WHERE archive = 0 AND stateId = 4");
                selectTradeByTraderStmt = conn.prepareStatement(
                        "SELECT id, orderId, trader, market, contr, settlDay, ref, sideId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, roleId, cpty, created FROM Exec_t WHERE trader = ? AND archive = 0 AND stateId = 4");
                selectPosnStmt = conn.prepareStatement(
                        "SELECT trader, contr, settlDay, sideId, cost, lots FROM Posn_v");
                selectPosnByTraderStmt = conn.prepareStatement(
                        "SELECT trader, contr, settlDay, sideId, cost, lots FROM Posn_v WHERE trader = ?");
                // Success.
                this.conn = conn;
                assert selectAssetStmt != null;
                this.selectAssetStmt = selectAssetStmt;
                assert selectContrStmt != null;
                this.selectContrStmt = selectContrStmt;
                assert selectMarketStmt != null;
                this.selectMarketStmt = selectMarketStmt;
                assert selectTraderStmt != null;
                this.selectTraderStmt = selectTraderStmt;
                assert selectTraderByEmailStmt != null;
                this.selectTraderByEmailStmt = selectTraderByEmailStmt;
                assert selectOrderStmt != null;
                this.selectOrderStmt = selectOrderStmt;
                assert selectOrderByTraderStmt != null;
                this.selectOrderByTraderStmt = selectOrderByTraderStmt;
                assert selectTradeStmt != null;
                this.selectTradeStmt = selectTradeStmt;
                assert selectTradeByTraderStmt != null;
                this.selectTradeByTraderStmt = selectTradeByTraderStmt;
                assert selectPosnStmt != null;
                this.selectPosnStmt = selectPosnStmt;
                assert selectPosnByTraderStmt != null;
                this.selectPosnByTraderStmt = selectPosnByTraderStmt;
                success = true;
            } finally {
                if (!success) {
                    if (selectPosnByTraderStmt != null) {
                        selectPosnByTraderStmt.close();
                    }
                    if (selectPosnStmt != null) {
                        selectPosnStmt.close();
                    }
                    if (selectTradeByTraderStmt != null) {
                        selectTradeByTraderStmt.close();
                    }
                    if (selectTradeStmt != null) {
                        selectTradeStmt.close();
                    }
                    if (selectOrderByTraderStmt != null) {
                        selectOrderByTraderStmt.close();
                    }
                    if (selectOrderStmt != null) {
                        selectOrderStmt.close();
                    }
                    if (selectTraderByEmailStmt != null) {
                        selectTraderByEmailStmt.close();
                    }
                    if (selectTraderStmt != null) {
                        selectTraderStmt.close();
                    }
                    if (selectMarketStmt != null) {
                        selectMarketStmt.close();
                    }
                    if (selectContrStmt != null) {
                        selectContrStmt.close();
                    }
                    if (selectAssetStmt != null) {
                        selectAssetStmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                }
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws SQLException {
        selectPosnByTraderStmt.close();
        selectPosnStmt.close();
        selectTradeByTraderStmt.close();
        selectTradeStmt.close();
        selectOrderByTraderStmt.close();
        selectOrderStmt.close();
        selectTraderByEmailStmt.close();
        selectTraderStmt.close();
        selectMarketStmt.close();
        selectContrStmt.close();
        selectAssetStmt.close();
        conn.close();
    }

    @Override
    public final @NonNull MnemRbTree readAsset(@NonNull Factory factory) {
        final MnemRbTree t = new MnemRbTree();
        try (final ResultSet rs = selectAssetStmt.executeQuery()) {
            while (rs.next()) {
                t.insert(getAsset(rs, factory));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    @Override
    public final @NonNull MnemRbTree readContr(@NonNull Factory factory) {
        final MnemRbTree t = new MnemRbTree();
        try (final ResultSet rs = selectContrStmt.executeQuery()) {
            while (rs.next()) {
                t.insert(getContr(rs, factory));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    @Override
    public final @NonNull MnemRbTree readMarket(@NonNull Factory factory) {
        final MnemRbTree t = new MnemRbTree();
        try (final ResultSet rs = selectMarketStmt.executeQuery()) {
            while (rs.next()) {
                t.insert(getMarket(rs, factory));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    @Override
    public final @NonNull MnemRbTree readTrader(@NonNull Factory factory) {
        final MnemRbTree t = new MnemRbTree();
        try (final ResultSet rs = selectTraderStmt.executeQuery()) {
            while (rs.next()) {
                t.insert(getTrader(rs, factory));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    @Override
    public final @Nullable String readTraderByEmail(@NonNull String email,
            @NonNull Factory factory) {
        try {
            setParam(selectTraderByEmailStmt, 1, email);
            try (final ResultSet rs = selectTraderByEmailStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("mnem");
                }
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }

    @Override
    public final @NonNull MnemRbTree readView(@NonNull Factory factory)
            throws InterruptedException {
        return ModelUtil.readView(this, factory);
    }

    @Override
    public final SlNode readOrder(@NonNull Factory factory) {
        final SlQueue q = new SlQueue();
        try {
            readOrder(selectOrderStmt, factory, q);
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final @NonNull InstructTree readOrder(@NonNull String trader,
            @NonNull Factory factory) {
        final InstructTree t = new InstructTree();
        try {
            setParam(selectOrderByTraderStmt, 1, trader);
            readOrder(selectOrderByTraderStmt, factory, t);
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    @Override
    public final SlNode readTrade(@NonNull Factory factory) {
        final SlQueue q = new SlQueue();
        try {
            readTrade(selectTradeStmt, factory, q);
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final @NonNull InstructTree readTrade(@NonNull String trader,
            @NonNull Factory factory) {
        final InstructTree t = new InstructTree();
        try {
            setParam(selectTradeByTraderStmt, 1, trader);
            readTrade(selectTradeByTraderStmt, factory, t);
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    @Override
    public final SlNode readPosn(int busDay, @NonNull Factory factory) {
        final SlQueue q = new SlQueue();
        try {
            readPosn(selectPosnStmt, busDay, factory, q);
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final @NonNull TraderPosnTree readPosn(@NonNull String trader, int busDay,
            @NonNull Factory factory) {
        final TraderPosnTree t = new TraderPosnTree();
        try {
            setParam(selectPosnByTraderStmt, 1, trader);
            readPosn(selectPosnByTraderStmt, busDay, factory, t);
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }
}
