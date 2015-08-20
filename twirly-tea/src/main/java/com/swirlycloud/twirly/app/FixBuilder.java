/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.app.FixUtility.execTypeToState;
import static com.swirlycloud.twirly.app.FixUtility.fixToSide;
import static com.swirlycloud.twirly.app.FixUtility.lastLiquidityIndToRole;
import static com.swirlycloud.twirly.app.FixUtility.ordStatusToState;
import static com.swirlycloud.twirly.app.FixUtility.roleToLastLiquidityInd;
import static com.swirlycloud.twirly.app.FixUtility.sideToFix;
import static com.swirlycloud.twirly.app.FixUtility.stateToExecType;
import static com.swirlycloud.twirly.app.FixUtility.stateToOrdStatus;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;

import java.util.Date;
import java.util.List;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.BusinessRejectReason;
import quickfix.field.BusinessRejectRefID;
import quickfix.field.ClOrdID;
import quickfix.field.ContraBroker;
import quickfix.field.CumQty;
import quickfix.field.CxlRejReason;
import quickfix.field.CxlRejResponseTo;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.FutSettDate;
import quickfix.field.LastLiquidityInd;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.MinQty;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.NoContraBrokers;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TransactTime;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Instruct;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.quickfix.Contract;
import com.swirlycloud.twirly.quickfix.Cost;
import com.swirlycloud.twirly.quickfix.MatchId;

/**
 * @author Mark Aylett
 */
public final class FixBuilder {
    private Message message;

    private final void setLong(int field, long value) {
        message.setString(field, String.valueOf(value));
    }

    private final long getLong(int field) throws FieldNotFound, NumberFormatException {
        return Long.parseLong(message.getString(field));
    }

    public final void setMessage(Message message) {
        this.message = message;
    }

    public final Message getMessage() {
        return this.message;
    }

    // Account(1)

    public final void setAccount(String trader) {
        message.setString(Account.FIELD, trader);
    }

    public final String getAccount() throws FieldNotFound {
        return message.getString(Account.FIELD);
    }

    // AvgPx(6)

    public final void setAvgPx(double avgTicks) {
        message.setDouble(AvgPx.FIELD, avgTicks);
    }

    public final double getAvgPx() throws FieldNotFound {
        return message.getDouble(AvgPx.FIELD);
    }

    // ClOrdId(11)

    public final void setClOrdId(String ref) {
        message.setString(ClOrdID.FIELD, ref);
    }

    public final String getClOrdId() throws FieldNotFound {
        return message.getString(ClOrdID.FIELD);
    }

    // CumQty(14)

    public final void setCumQty(long exec) {
        setLong(CumQty.FIELD, exec);
    }

    public final long getCumQty() throws FieldNotFound {
        return getLong(CumQty.FIELD);
    }

    // ExecId(17)

    public final void setExecId(long id) {
        setLong(ExecID.FIELD, id);
    }

    public final long getExecId() throws FieldNotFound {
        return getLong(ExecID.FIELD);
    }

    // LastPx(31)

    public final void setLastPx(long lastTicks) {
        setLong(LastPx.FIELD, lastTicks);
    }

    public final long getLastPx() throws FieldNotFound {
        return getLong(LastPx.FIELD);
    }

    // LastQty(32)

    public final void setLastQty(long lastLots) {
        setLong(LastQty.FIELD, lastLots);
    }

    public final long getLastQty() throws FieldNotFound {
        return getLong(LastQty.FIELD);
    }

    // OrderId(37)

    public final void setOrderId(long id) {
        setLong(OrderID.FIELD, id);
    }

    public final long getOrderId() throws FieldNotFound {
        return getLong(OrderID.FIELD);
    }

    // OrderQty(38)

    public final void setOrderQty(long lots) {
        setLong(OrderQty.FIELD, lots);
    }

    public final long getOrderQty() throws FieldNotFound {
        return getLong(OrderQty.FIELD);
    }

    // OrdStatus(39)

    public final void setOrdStatus(State state, long resd) {
        message.setChar(OrdStatus.FIELD, stateToOrdStatus(state, resd));
    }

    public final State getOrdStatus() throws FieldNotFound, IncorrectTagValue {
        return ordStatusToState(message.getChar(OrdStatus.FIELD));
    }

    // OrdType(40)

    public final void setOrdType() {
        message.setChar(OrdType.FIELD, OrdType.LIMIT);
    }

    // ClOrdId(41)

    public final void setOrigClOrdId(String ref) {
        message.setString(OrigClOrdID.FIELD, ref);
    }

    public final String getOrigClOrdId() throws FieldNotFound {
        return message.getString(OrigClOrdID.FIELD);
    }

    // Price(44)

    public final void setPrice(long ticks) {
        setLong(Price.FIELD, ticks);
    }

    public final long getPrice() throws FieldNotFound {
        return getLong(Price.FIELD);
    }

    // Side(54)

    public final void setSide(Side side) {
        message.setChar(quickfix.field.Side.FIELD, sideToFix(side));
    }

    public final Side getSide() throws FieldNotFound, IncorrectTagValue {
        return fixToSide(message.getChar(quickfix.field.Side.FIELD));
    }

    // Symbol(55)

    public final void setSymbol(String symbol) {
        message.setString(Symbol.FIELD, symbol);
    }

    public final String getSymbol() throws FieldNotFound {
        return message.getString(Symbol.FIELD);
    }

    // TransactTime(60)

    public final void setTransactTime(long millis) {
        message.setUtcTimeStamp(TransactTime.FIELD, new Date(millis), true);
    }

    public final long getTransactTime() throws FieldNotFound {
        return message.getUtcTimeStamp(TransactTime.FIELD).getTime();
    }

    // FutSettDate(64)

    public final void setFutSettDate(int settlDay) {
        message.setInt(FutSettDate.FIELD, maybeJdToIso(settlDay));
    }

    public final int getFutSettDate() throws FieldNotFound {
        return maybeIsoToJd(message.getInt(FutSettDate.FIELD));
    }

    // MinQty(110)

    public final void setMinQty(long minLots) {
        setLong(MinQty.FIELD, minLots);
    }

    public final long getMinQty(long minLots) throws FieldNotFound, NumberFormatException {
        return getLong(MinQty.FIELD);
    }

    // ExecType(150)

    public final void setExecType(State state, long resd) {
        message.setChar(ExecType.FIELD, stateToExecType(state, resd));
    }

    public final State getExecType(State state) throws FieldNotFound, IncorrectTagValue {
        return execTypeToState(message.getChar(ExecType.FIELD));
    }

    // LeavesQty(151)

    public final void setLeavesQty(long resd) {
        setLong(LeavesQty.FIELD, resd);
    }

    public final long getLeavesQty() throws FieldNotFound, NumberFormatException {
        return getLong(LeavesQty.FIELD);
    }

    // ContraBroker(375)

    public final void setContraBroker(String cpty) {
        final Group group = new Group(NoContraBrokers.FIELD, ContraBroker.FIELD);
        group.setString(ContraBroker.FIELD, cpty);
        message.addGroup(group);
    }

    public final String getContraBroker() throws FieldNotFound {
        final List<Group> groups = message.getGroups(NoContraBrokers.FIELD);
        if (groups == null || groups.isEmpty()) {
            throw new FieldNotFound(NoContraBrokers.FIELD);
        }
        return groups.get(0).getString(ContraBroker.FIELD);
    }

    // LastLiquidityInd(851)

    public final void setLastLiquidityInd(Role role) {
        message.setInt(LastLiquidityInd.FIELD, roleToLastLiquidityInd(role));
    }

    public final Role getLastLiquidityInd() throws FieldNotFound, IncorrectTagValue {
        return lastLiquidityIndToRole(message.getInt(LastLiquidityInd.FIELD));
    }

    // Contract(20000)

    public final void setContract(String contr) {
        message.setString(Contract.FIELD, contr);
    }

    public final String getContract() throws FieldNotFound {
        return message.getString(Contract.FIELD);
    }

    // Cost(20001)

    public final void setCost(long cost) {
        setLong(Cost.FIELD, cost);
    }

    public final long getCost() throws FieldNotFound {
        return getLong(Cost.FIELD);
    }

    // MatchId(20002)

    public final void setMatchId(long matchId) {
        setLong(MatchId.FIELD, matchId);
    }

    public final long getMatchId() throws FieldNotFound {
        return getLong(MatchId.FIELD);
    }

    /**
     * @param refMsg
     *            The message being rejected.
     * @param refId
     *            Optional id or reference from the message being rejected.
     * @param text
     *            Optional textual information.
     * @throws FieldNotFound
     */
    public final void setBusinessReject(Message refMsg, String refId, String text)
            throws FieldNotFound {
        final Header header = refMsg.getHeader();
        message.setString(RefMsgType.FIELD, header.getString(MsgType.FIELD));
        message.setInt(RefSeqNum.FIELD, header.getInt(MsgSeqNum.FIELD));
        if (refId != null) {
            message.setString(BusinessRejectRefID.FIELD, refId);
        }
        message.setInt(BusinessRejectReason.FIELD, BusinessRejectReason.OTHER);
        if (text != null) {
            message.setString(Text.FIELD, text);
        }
    }

    public final void setCancelReject(String ref, Order order, String text) {
        setClOrdId(ref);
        setOrigClOrdId(order.getRef());
        setOrderId(order.getId());
        setOrdStatus(order.getState(), order.getResd());
        message.setChar(CxlRejResponseTo.FIELD, CxlRejResponseTo.ORDER_CANCEL_REQUEST);
        message.setInt(CxlRejReason.FIELD, CxlRejReason.OTHER);
        if (text != null) {
            message.setString(Text.FIELD, text);
        }
    }

    /**
     * @param ref
     *            The cancel-request's reference.
     * @param orderRef
     *            The reference of the order attempting to be cancelled.
     * @param orderId
     *            Optional id of the order attempting to be cancelled.
     * @param text
     *            Optional textual information.
     */
    public final void setCancelReject(String ref, String orderRef, Long orderId, String text) {
        setClOrdId(ref);
        setOrigClOrdId(orderRef);
        // If CxlRejReason="Unknown order", specify "NONE".
        message.setString(OrderID.FIELD, orderId != null ? orderId.toString() : "NONE");
        message.setChar(OrdStatus.FIELD, OrdStatus.REJECTED);
        message.setChar(CxlRejResponseTo.FIELD, CxlRejResponseTo.ORDER_CANCEL_REQUEST);
        message.setInt(CxlRejReason.FIELD, CxlRejReason.UNKNOWN_ORDER);
        if (text != null) {
            message.setString(Text.FIELD, text);
        }
    }

    public final void setInstruct(Instruct instruct) {
        setExecId(instruct.getId());
        setOrderId(instruct.getOrderId());
        setAccount(instruct.getTrader());
        setSymbol(instruct.getMarket());
        setContract(instruct.getContr());
        setFutSettDate(instruct.getSettlDay());
        setClOrdId(instruct.getRef());
        setExecType(instruct.getState(), instruct.getResd());
        setOrdStatus(instruct.getState(), instruct.getResd());
        setSide(instruct.getSide());
        setOrdType();
        setPrice(instruct.getTicks());
        setOrderQty(instruct.getLots());
        setLeavesQty(instruct.getResd());
        setCumQty(instruct.getExec());
        setCost(instruct.getCost());
        setAvgPx(instruct.getAvgTicks());
        if (instruct.getLastLots() != 0) {
            setLastPx(instruct.getLastTicks());
            setLastQty(instruct.getLastLots());
        }
        setMinQty(instruct.getMinLots());
    }

    public final void setExec(Exec exec) {
        setInstruct(exec);
        if (exec.getMatchId() != 0) {
            setMatchId(exec.getMatchId());
        }
        if (exec.getRole() != null) {
            setLastLiquidityInd(exec.getRole());
        }
        if (exec.getCpty() != null) {
            setContraBroker(exec.getCpty());
        }
        setTransactTime(exec.getCreated());
    }

    public final void setNewOrderSingle(String market, String ref, Side side, long ticks,
            long lots, long minLots, long now) {
        setSymbol(market);
        setClOrdId(ref);
        setSide(side);
        setOrdType();
        setPrice(ticks);
        setOrderQty(lots);
        setMinQty(minLots);
        setTransactTime(now);
    }

    public final void setOrderCancelReplaceRequest(String market, String ref, String orderRef,
            long lots, long now) {
        setSymbol(market);
        setClOrdId(ref);
        setOrigClOrdId(orderRef);
        message.setChar(quickfix.field.Side.FIELD, quickfix.field.Side.UNDISCLOSED);
        setOrdType();
        setOrderQty(lots);
        setTransactTime(now);
    }

    public final void setOrderCancelReplaceRequest(String market, String ref, long orderId,
            long lots, long now) {
        setOrderCancelReplaceRequest(market, ref, "NONE", lots, now);
        setOrderId(orderId);
    }

    public final void setOrderCancelRequest(String market, String ref, String orderRef, long now) {
        setOrderCancelReplaceRequest(market, ref, orderRef, 0, now);
    }

    public final void setOrderCancelRequest(String market, String ref, long orderId, long now) {
        setOrderCancelRequest(market, ref, "NONE", now);
        setOrderId(orderId);
    }
}
