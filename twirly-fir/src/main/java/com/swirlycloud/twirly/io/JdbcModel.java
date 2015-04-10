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
import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.intrusive.PosnTree;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.io.Model;
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
    private final PreparedStatement updateOrderStmt;
    private final PreparedStatement updateExecStmt;

    private static Asset getAsset(ResultSet rs) throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final AssetType assetType = AssetType.valueOf(rs.getInt("typeId"));
        return new Asset(mnem, display, assetType);
    }

    private static Contr getContr(ResultSet rs) throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final Memorable asset = newMnem(rs.getString("asset"));
        final Memorable ccy = newMnem(rs.getString("ccy"));
        final int tickNumer = rs.getInt("tickNumer");
        final int tickDenom = rs.getInt("tickDenom");
        final int lotNumer = rs.getInt("lotNumer");
        final int lotDenom = rs.getInt("lotDenom");
        final int pipDp = rs.getInt("pipDp");
        final int minLots = rs.getInt("minLots");
        final int maxLots = rs.getInt("maxLots");
        return new Contr(mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom,
                pipDp, minLots, maxLots);
    }

    private static Market getMarket(ResultSet rs) throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final Memorable contr = newMnem(rs.getString("contr"));
        final int settlDay = rs.getInt("settlDay");
        final int expiryDay = rs.getInt("expiryDay");
        final int state = rs.getInt("state");
        final long lastTicks = rs.getLong("lastTicks");
        final long lastLots = rs.getLong("lastLots");
        final long lastTime = rs.getLong("lastTime");
        final long maxOrderId = rs.getLong("maxOrderId");
        final long maxExecId = rs.getLong("maxExecId");
        return new Market(mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots,
                lastTime, maxOrderId, maxExecId);
    }

    private static Trader getTrader(ResultSet rs) throws SQLException {
        final String mnem = rs.getString("mnem");
        final String display = rs.getString("display");
        final String email = rs.getString("email");
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
        return new Order(id, trader, market, contr, settlDay, ref, state, action, ticks, lots,
                resd, exec, cost, lastTicks, lastLots, minLots, created, modified);
    }

    private static Exec getTrade(ResultSet rs) throws SQLException {
        final long id = rs.getLong("id");
        final long orderId = rs.getLong("orderId");
        final String trader = rs.getString("trader");
        final String market = rs.getString("market");
        final String contr = rs.getString("contr");
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
        long matchId;
        Role role;
        String cpty;
        if (state == State.TRADE) {
            matchId = rs.getLong("matchId");
            role = Role.valueOf(rs.getInt("roleId"));
            cpty = rs.getString("cpty");
        } else {
            matchId = 0;
            role = null;
            cpty = null;
        }
        final long created = rs.getLong("created");
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
        PreparedStatement updateOrderStmt = null;
        PreparedStatement updateExecStmt = null;
        boolean success = false;
        try {
            try {
                conn = DriverManager.getConnection(url, user, password);
                selectAssetStmt = conn.prepareStatement("SELECT mnem, display, typeId FROM Asset");
                selectContrStmt = conn
                        .prepareStatement("SELECT mnem, display, asset, ccy, tickNumer, tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots FROM ContrV");
                selectMarketStmt = conn
                        .prepareStatement("SELECT mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots, lastTime, maxOrderId, maxExecId FROM MarketV");
                selectTraderStmt = conn.prepareStatement("SELECT mnem, display, email FROM Trader");
                selectOrderStmt = conn
                        .prepareStatement("SELECT id, trader, market, contr, settlDay, ref, stateId, actionId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, created, modified FROM Order_ WHERE archive = 0 AND resd > 0");
                selectTradeStmt = conn
                        .prepareStatement("SELECT id, orderId, trader, market, contr, settlDay, ref, actionId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, roleId, cpty, created FROM Exec WHERE archive = 0 AND stateId = 4");
                selectPosnStmt = conn
                        .prepareStatement("SELECT trader, contr, settlDay, actionId, cost, lots FROM PosnV");
                insertMarketStmt = conn
                        .prepareStatement("INSERT INTO Market (mnem, display, contr, settlDay, expiryDay, state) VALUES (?, ?, ?, ?, ?, ?)");
                insertTraderStmt = conn
                        .prepareStatement("INSERT INTO Trader (mnem, display, email) VALUES (?, ?, ?)");
                insertExecStmt = conn
                        .prepareStatement("INSERT INTO Exec (id, orderId, trader, market, contr, settlDay, ref, stateId, actionId, ticks, lots, resd, exec, cost, lastTicks, lastLots, minLots, matchId, roleId, cpty, archive, created, modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                updateOrderStmt = conn
                        .prepareStatement("UPDATE Order_ SET archive = 1, modified = ? WHERE market = ? AND id = ?");
                updateExecStmt = conn
                        .prepareStatement("UPDATE Exec SET archive = 1, modified = ? WHERE market = ? AND id = ?");
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
            insertMarketStmt.setString(i++, mnem);
            insertMarketStmt.setString(i++, display);
            insertMarketStmt.setString(i++, contr);
            insertMarketStmt.setInt(i++, settlDay);
            insertMarketStmt.setInt(i++, expiryDay);
            insertMarketStmt.setInt(i++, state);
            insertMarketStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void insertTrader(String mnem, String display, String email) {
        try {
            int i = 1;
            insertTraderStmt.setString(i++, mnem);
            insertTraderStmt.setString(i++, display);
            insertTraderStmt.setString(i++, email);
            insertTraderStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void insertExec(Exec exec) {
        try {
            int i = 1;
            insertExecStmt.setLong(i++, exec.getId());
            insertExecStmt.setLong(i++, exec.getOrderId());
            insertExecStmt.setString(i++, exec.getTrader());
            insertExecStmt.setString(i++, exec.getMarket());
            insertExecStmt.setString(i++, exec.getContr());
            insertExecStmt.setInt(i++, exec.getSettlDay());
            if (!exec.getRef().isEmpty()) {
                insertExecStmt.setString(i++, exec.getRef());
            } else {
                insertExecStmt.setNull(i++, Types.CHAR);
            }
            insertExecStmt.setInt(i++, exec.getState().intValue());
            insertExecStmt.setInt(i++, exec.getAction().intValue());
            insertExecStmt.setLong(i++, exec.getTicks());
            insertExecStmt.setLong(i++, exec.getLots());
            insertExecStmt.setLong(i++, exec.getResd());
            insertExecStmt.setLong(i++, exec.getExec());
            insertExecStmt.setLong(i++, exec.getCost());
            if (exec.getLastLots() > 0) {
                insertExecStmt.setLong(i++, exec.getLastTicks());
                insertExecStmt.setLong(i++, exec.getLastLots());
            } else {
                insertExecStmt.setNull(i++, Types.INTEGER);
                insertExecStmt.setNull(i++, Types.INTEGER);
            }
            insertExecStmt.setLong(i++, exec.getMinLots());
            if (exec.getState() == State.TRADE) {
                insertExecStmt.setLong(i++, exec.getMatchId());
                insertExecStmt.setInt(i++, exec.getRole().intValue());
                insertExecStmt.setString(i++, exec.getCpty());
            } else {
                insertExecStmt.setNull(i++, Types.INTEGER);
                insertExecStmt.setNull(i++, Types.INTEGER);
                insertExecStmt.setNull(i++, Types.CHAR);
            }
            insertExecStmt.setBoolean(i++, false);
            insertExecStmt.setLong(i++, exec.getCreated());
            insertExecStmt.setLong(i++, exec.getCreated());
            insertExecStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void insertExecList(String market, SlNode first) {
        try {
            conn.setAutoCommit(false);
            boolean success = false;
            try {
                for (SlNode node = first; node != null; node = node.slNext()) {
                    insertExec((Exec) node);
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
        }
    }

    @Override
    public final void archiveOrder(String market, long id, long modified) {
        try {
            int i = 1;
            updateOrderStmt.setLong(i++, modified);
            updateOrderStmt.setString(i++, market);
            updateOrderStmt.setLong(i++, id);
            updateOrderStmt.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final void archiveTrade(String market, long id, long modified) {
        try {
            int i = 1;
            updateExecStmt.setLong(i++, modified);
            updateExecStmt.setString(i++, market);
            updateExecStmt.setLong(i++, id);
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
    public final SlNode selectPosn() {
        final PosnTree posns = new PosnTree();
        try (final ResultSet rs = selectPosnStmt.executeQuery()) {
            while (rs.next()) {
                final String trader = rs.getString("trader");
                final String contr = rs.getString("contr");
                final int settlDay = rs.getInt("settlDay");
                // Lazy position.
                Posn posn = (Posn) posns.pfind(trader, contr, settlDay);
                if (posn == null || !posn.getTrader().equals(trader)
                        || !posn.getContr().equals(contr) || posn.getSettlDay() != settlDay) {
                    final RbNode parent = posn;
                    posn = new Posn(trader, contr, settlDay);
                    posns.pinsert(posn, parent);
                }
                final Action action = Action.valueOf(rs.getInt("actionId"));
                final long cost = rs.getLong("cost");
                final long lots = rs.getLong("lots");
                if (action == Action.BUY) {
                    posn.setBuyCost(cost);
                    posn.setBuyLots(lots);
                } else {
                    assert action == Action.SELL;
                    posn.setSellCost(cost);
                    posn.setSellLots(lots);
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
