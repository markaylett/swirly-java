/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.node.SlUtil.popNext;
import static com.swirlycloud.twirly.util.MnemUtil.newMnem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.intrusive.PosnTree;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.Memorable;

public final class JdbcModel implements Model {
    private final Connection conn;
    private final PreparedStatement selectAssetStmt;
    private final PreparedStatement selectContrStmt;
    private final PreparedStatement selectMarketStmt;
    private final PreparedStatement selectTraderStmt;
    private final PreparedStatement selectOrderStmt;
    private final PreparedStatement selectTradeStmt;
    private final PreparedStatement selectPosnStmt;
    private final PreparedStatement insertMarketStmt;
    private final PreparedStatement insertTraderStmt;
    private final PreparedStatement insertExecStmt;
    private final PreparedStatement updateMarketStmt;
    private final PreparedStatement updateTraderStmt;
    private final PreparedStatement updateOrderStmt;
    private final PreparedStatement updateExecStmt;

    private static void setParam(PreparedStatement stmt, int i, int val) throws SQLException {
        stmt.setInt(i, val);
    }

    private static void setNullIfZero(PreparedStatement stmt, int i, int val) throws SQLException {
        if (val != 0) {
            stmt.setInt(i, val);
        } else {
            stmt.setNull(i, Types.INTEGER);
        }
    }

    private static void setParam(PreparedStatement stmt, int i, long val) throws SQLException {
        stmt.setLong(i, val);
    }

    private static void setNullIfZero(PreparedStatement stmt, int i, long val) throws SQLException {
        if (val != 0) {
            stmt.setLong(i, val);
        } else {
            stmt.setNull(i, Types.INTEGER);
        }
    }

    private static void setParam(PreparedStatement stmt, int i, String val) throws SQLException {
        stmt.setString(i, val);
    }

    private static void setNullIfEmpty(PreparedStatement stmt, int i, String val)
            throws SQLException {
        if (val != null && !val.isEmpty()) {
            stmt.setString(i, val);
        } else {
            stmt.setNull(i, Types.CHAR);
        }
    }

    private static void setParam(PreparedStatement stmt, int i, boolean val) throws SQLException {
        stmt.setBoolean(i, val);
    }

    private static Asset getAsset(ResultSet rs) throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final AssetType assetType = AssetType.valueOf(rs.getInt("typeId"));
        assert mnem != null;
        assert assetType != null;
        return new Asset(mnem, display, assetType);
    }

    private static Contr getContr(ResultSet rs) throws SQLException {
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
        return new Contr(mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom,
                pipDp, minLots, maxLots);
    }

    private static Market getMarket(ResultSet rs) throws SQLException {
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
        return new Market(mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots,
                lastTime, maxOrderId, maxExecId);
    }

    private static Trader getTrader(ResultSet rs) throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final String email = rs.getString("email");

        assert mnem != null;
        assert email != null;
        return new Trader(mnem, display, email);
    }

    private static Order getOrder(ResultSet rs) throws SQLException {
        final long id = rs.getLong("id");
        final String trader = rs.getString("trader");
        final String market = rs.getString("market");
        final String contr = rs.getString("contr");
        final int settlDay = rs.getInt("settlDay");
        final String ref = rs.getString("ref");
        final State state = State.valueOf(rs.getInt("stateId"));
        final Action action = Action.valueOf(rs.getInt("actionId"));
        final long ticks = rs.getLong("ticks");
        final long lots = rs.getLong("lots");
        final long resd = rs.getLong("resd");
        final long exec = rs.getLong("exec");
        final long cost = rs.getLong("cost");
        final long lastTicks = rs.getLong("lastTicks");
        final long lastLots = rs.getLong("lastLots");
        final long minLots = rs.getLong("minLots");
        final long created = rs.getLong("created");
        final long modified = rs.getLong("modified");
        assert trader != null;
        assert market != null;
        assert contr != null;
        return new Order(id, trader, market, contr, settlDay, ref, state, action, ticks, lots,
                resd, exec, cost, lastTicks, lastLots, minLots, created, modified);
    }

    private static Exec getTrade(ResultSet rs) throws SQLException {
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
        final Action action = Action.valueOf(rs.getInt("actionId"));
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
        return new Exec(id, orderId, trader, market, contr, settlDay, ref, state, action, ticks,
                lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, role, cpty, created);
    }

    public JdbcModel(String url, String user, String password) {
        Connection conn = null;
        PreparedStatement selectAssetStmt = null;
        PreparedStatement selectContrStmt = null;
        PreparedStatement selectMarketStmt = null;
        PreparedStatement selectTraderStmt = null;
        PreparedStatement selectOrderStmt = null;
        PreparedStatement selectTradeStmt = null;
        PreparedStatement selectPosnStmt = null;
        PreparedStatement insertMarketStmt = null;
        PreparedStatement insertTraderStmt = null;
        PreparedStatement insertExecStmt = null;
        PreparedStatement updateMarketStmt = null;
        PreparedStatement updateTraderStmt = null;
        PreparedStatement updateOrderStmt = null;
        PreparedStatement updateExecStmt = null;
        boolean success = false;
        try {
            try {
                conn = DriverManager.getConnection(url, user, password);
                selectAssetStmt = conn
                        .prepareStatement("SELECT mnem, display, typeId FROM Asset_t");
                selectContrStmt = conn
                        .prepareStatement("SELECT mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots FROM Contr_v");
                selectMarketStmt = conn
                        .prepareStatement("SELECT mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots, lastTime, maxOrderId, maxExecId FROM Market_v");
                selectTraderStmt = conn
                        .prepareStatement("SELECT mnem, display, email FROM Trader_t");
                selectOrderStmt = conn
                        .prepareStatement("SELECT id, trader, market, contr, settlDay, ref, stateId, actionId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, created, modified FROM Order_t WHERE archive = 0 AND resd > 0");
                selectTradeStmt = conn
                        .prepareStatement("SELECT id, orderId, trader, market, contr, settlDay, ref, actionId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, roleId, cpty, created FROM Exec_t WHERE archive = 0 AND stateId = 4");
                selectPosnStmt = conn
                        .prepareStatement("SELECT trader, contr, settlDay, actionId, cost, lots FROM Posn_v");
                insertMarketStmt = conn
                        .prepareStatement("INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state) VALUES (?, ?, ?, ?, ?, ?)");
                insertTraderStmt = conn
                        .prepareStatement("INSERT INTO Trader_t (mnem, display, email) VALUES (?, ?, ?)");
                insertExecStmt = conn
                        .prepareStatement("INSERT INTO Exec_t (id, orderId, trader, market, contr, settlDay, ref, stateId, actionId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, roleId, cpty, archive, created, modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                updateMarketStmt = conn
                        .prepareStatement("UPDATE Market_t SET display = ?, state = ? WHERE mnem = ?");
                updateTraderStmt = conn
                        .prepareStatement("UPDATE Trader_t SET display = ? WHERE mnem = ?");
                updateOrderStmt = conn
                        .prepareStatement("UPDATE Order_t SET archive = 1, modified = ? WHERE market = ? AND id = ?");
                updateExecStmt = conn
                        .prepareStatement("UPDATE Exec_t SET archive = 1, modified = ? WHERE market = ? AND id = ?");
                // Success.
                this.conn = conn;
                this.selectAssetStmt = selectAssetStmt;
                this.selectContrStmt = selectContrStmt;
                this.selectMarketStmt = selectMarketStmt;
                this.selectTraderStmt = selectTraderStmt;
                this.selectOrderStmt = selectOrderStmt;
                this.selectTradeStmt = selectTradeStmt;
                this.selectPosnStmt = selectPosnStmt;
                this.insertMarketStmt = insertMarketStmt;
                this.insertTraderStmt = insertTraderStmt;
                this.insertExecStmt = insertExecStmt;
                this.updateMarketStmt = updateMarketStmt;
                this.updateTraderStmt = updateTraderStmt;
                this.updateOrderStmt = updateOrderStmt;
                this.updateExecStmt = updateExecStmt;
                success = true;
            } finally {
                if (!success) {
                    if (updateExecStmt != null) {
                        updateExecStmt.close();
                    }
                    if (updateOrderStmt != null) {
                        updateOrderStmt.close();
                    }
                    if (updateTraderStmt != null) {
                        updateTraderStmt.close();
                    }
                    if (updateMarketStmt != null) {
                        updateMarketStmt.close();
                    }
                    if (insertExecStmt != null) {
                        insertExecStmt.close();
                    }
                    if (insertTraderStmt != null) {
                        insertTraderStmt.close();
                    }
                    if (insertMarketStmt != null) {
                        insertMarketStmt.close();
                    }
                    if (selectPosnStmt != null) {
                        selectPosnStmt.close();
                    }
                    if (selectTradeStmt != null) {
                        selectTradeStmt.close();
                    }
                    if (selectOrderStmt != null) {
                        selectOrderStmt.close();
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
    public final void close() throws SQLException {
        updateExecStmt.close();
        updateOrderStmt.close();
        updateTraderStmt.close();
        updateMarketStmt.close();
        insertExecStmt.close();
        insertTraderStmt.close();
        insertMarketStmt.close();
        selectPosnStmt.close();
        selectTradeStmt.close();
        selectOrderStmt.close();
        selectTraderStmt.close();
        selectMarketStmt.close();
        selectContrStmt.close();
        selectAssetStmt.close();
        conn.close();
    }

    @Override
    public final void insertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state) {
        try {
            int i = 1;
            setParam(insertMarketStmt, i++, mnem);
            setParam(insertMarketStmt, i++, display);
            setParam(insertMarketStmt, i++, contr);
            setNullIfZero(insertMarketStmt, i++, settlDay);
            setNullIfZero(insertMarketStmt, i++, expiryDay);
            setParam(insertMarketStmt, i++, state);
            insertMarketStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void updateMarket(String mnem, String display, int state) {
        try {
            int i = 1;
            setParam(updateMarketStmt, i++, display);
            setParam(updateMarketStmt, i++, state);
            setParam(updateMarketStmt, i++, mnem);
            updateMarketStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void insertTrader(String mnem, String display, String email) {
        try {
            int i = 1;
            setParam(insertTraderStmt, i++, mnem);
            setParam(insertTraderStmt, i++, display);
            setParam(insertTraderStmt, i++, email);
            insertTraderStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void updateTrader(String mnem, String display) throws NotFoundException {
        try {
            int i = 1;
            setParam(updateTraderStmt, i++, display);
            setParam(updateTraderStmt, i++, mnem);
            updateTraderStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void insertExec(Exec exec) {
        try {
            int i = 1;
            setParam(insertExecStmt, i++, exec.getId());
            setNullIfZero(insertExecStmt, i++, exec.getOrderId());
            setParam(insertExecStmt, i++, exec.getTrader());
            setParam(insertExecStmt, i++, exec.getMarket());
            setParam(insertExecStmt, i++, exec.getContr());
            setNullIfZero(insertExecStmt, i++, exec.getSettlDay());
            setParam(insertExecStmt, i++, exec.getRef());
            setParam(insertExecStmt, i++, exec.getState().intValue());
            setParam(insertExecStmt, i++, exec.getAction().intValue());
            setParam(insertExecStmt, i++, exec.getTicks());
            setParam(insertExecStmt, i++, exec.getLots());
            setParam(insertExecStmt, i++, exec.getResd());
            setParam(insertExecStmt, i++, exec.getExec());
            setParam(insertExecStmt, i++, exec.getCost());
            if (exec.getLastLots() > 0) {
                setParam(insertExecStmt, i++, exec.getLastTicks());
                setParam(insertExecStmt, i++, exec.getLastLots());
            } else {
                insertExecStmt.setNull(i++, Types.INTEGER);
                insertExecStmt.setNull(i++, Types.INTEGER);
            }
            setParam(insertExecStmt, i++, exec.getMinLots());
            setNullIfZero(insertExecStmt, i++, exec.getMatchId());
            final Role role = exec.getRole();
            if (role != null) {
                setParam(insertExecStmt, i++, role.intValue());
            } else {
                insertExecStmt.setNull(i++, Types.INTEGER);
            }
            setNullIfEmpty(insertExecStmt, i++, exec.getCpty());
            setParam(insertExecStmt, i++, false);
            setParam(insertExecStmt, i++, exec.getCreated());
            setParam(insertExecStmt, i++, exec.getCreated());
            insertExecStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void insertExecList(String market, SlNode first) {
        SlNode node = first;
        try {
            conn.setAutoCommit(false);
            boolean success = false;
            try {
                while (node != null) {
                    final Exec exec = (Exec) node;
                    node = popNext(node);

                    insertExec(exec);
                }
                conn.commit();
                success = true;
            } finally {
                if (!success) {
                    conn.rollback();
                }
                conn.setAutoCommit(true);
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        } finally {
            // Clear nodes to ensure no unwanted retention.
            while (node != null) {
                node = popNext(node);
            }
        }
    }

    @Override
    public final void archiveOrder(String market, long id, long modified) {
        try {
            int i = 1;
            setParam(updateOrderStmt, i++, modified);
            setParam(updateOrderStmt, i++, market);
            setParam(updateOrderStmt, i++, id);
            updateOrderStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void archiveTrade(String market, long id, long modified) {
        try {
            int i = 1;
            setParam(updateExecStmt, i++, modified);
            setParam(updateExecStmt, i++, market);
            setParam(updateExecStmt, i++, id);
            updateExecStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final SlNode selectAsset() {
        final SlQueue q = new SlQueue();
        try (final ResultSet rs = selectAssetStmt.executeQuery()) {
            while (rs.next()) {
                q.insertBack(getAsset(rs));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final SlNode selectContr() {
        final SlQueue q = new SlQueue();
        try (final ResultSet rs = selectContrStmt.executeQuery()) {
            while (rs.next()) {
                q.insertBack(getContr(rs));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final SlNode selectMarket() {
        final SlQueue q = new SlQueue();
        try (final ResultSet rs = selectMarketStmt.executeQuery()) {
            while (rs.next()) {
                q.insertBack(getMarket(rs));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final SlNode selectTrader() {
        final SlQueue q = new SlQueue();
        try (final ResultSet rs = selectTraderStmt.executeQuery()) {
            while (rs.next()) {
                q.insertBack(getTrader(rs));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final SlNode selectOrder() {
        final SlQueue q = new SlQueue();
        try (final ResultSet rs = selectOrderStmt.executeQuery()) {
            while (rs.next()) {
                q.insertBack(getOrder(rs));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final SlNode selectTrade() {
        final SlQueue q = new SlQueue();
        try (final ResultSet rs = selectTradeStmt.executeQuery()) {
            while (rs.next()) {
                q.insertBack(getTrade(rs));
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        return q.getFirst();
    }

    @Override
    public final SlNode selectPosn(int busDay) {
        final PosnTree posns = new PosnTree();
        try (final ResultSet rs = selectPosnStmt.executeQuery()) {
            while (rs.next()) {
                final String trader = rs.getString("trader");
                final String contr = rs.getString("contr");
                final int settlDay = rs.getInt("settlDay");
                assert trader != null;
                assert contr != null;
                // FIXME: handle settled contracts.
                // Lazy position.
                Posn posn = (Posn) posns.pfind(trader, contr, settlDay);
                if (posn == null || !posn.getTrader().equals(trader)
                        || !posn.getContr().equals(contr) || posn.getSettlDay() != settlDay) {
                    final RbNode parent = posn;
                    assert trader != null;
                    assert contr != null;
                    posn = new Posn(trader, contr, settlDay);
                    posns.pinsert(posn, parent);
                }
                final Action action = Action.valueOf(rs.getInt("actionId"));
                final long cost = rs.getLong("cost");
                final long lots = rs.getLong("lots");
                if (action == Action.BUY) {
                    posn.addBuy(cost, lots);
                } else {
                    assert action == Action.SELL;
                    posn.addSell(cost, lots);
                }
            }
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
        final SlQueue q = new SlQueue();
        for (;;) {
            final Posn posn = (Posn) posns.getRoot();
            if (posn == null) {
                break;
            }
            posns.remove(posn);
            q.insertBack(posn);
        }
        return q.getFirst();
    }
}
