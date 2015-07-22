/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.io;

import static com.swirlycloud.twirly.node.SlUtil.popNext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.UncheckedIOException;
import com.swirlycloud.twirly.node.SlNode;

public final class JdbcDatastore extends JdbcModel implements Datastore {
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

    public JdbcDatastore(String url, String user, String password) {
        super(url, user, password);
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
                    super.close();
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
        super.close();
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
}
