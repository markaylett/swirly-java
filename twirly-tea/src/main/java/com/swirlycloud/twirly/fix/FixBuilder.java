/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fix;

import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;
import static com.swirlycloud.twirly.fix.FixUtility.execTypeToState;
import static com.swirlycloud.twirly.fix.FixUtility.fixToSide;
import static com.swirlycloud.twirly.fix.FixUtility.lastLiquidityIndToRole;
import static com.swirlycloud.twirly.fix.FixUtility.ordStatusToState;
import static com.swirlycloud.twirly.fix.FixUtility.roleToLastLiquidityInd;
import static com.swirlycloud.twirly.fix.FixUtility.sideToFix;
import static com.swirlycloud.twirly.fix.FixUtility.stateToExecType;
import static com.swirlycloud.twirly.fix.FixUtility.stateToOrdStatus;

import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Instruct;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.quickfix.Contract;
import com.swirlycloud.twirly.quickfix.Cost;
import com.swirlycloud.twirly.quickfix.MatchId;

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

    // MsgType(35)

    public final void setMsgType(String msgType) {
        final Header header = message.getHeader();
        header.setString(MsgType.FIELD, msgType);
    }

    public final String getMsgType() throws FieldNotFound {
        final Header header = message.getHeader();
        return header.getString(MsgType.FIELD);
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

    public final void setOrdStatus(char ordStatus) {
        message.setChar(OrdStatus.FIELD, ordStatus);
    }

    public final State getOrdStatus() throws FieldNotFound, IncorrectTagValue {
        return ordStatusToState(message.getChar(OrdStatus.FIELD));
    }

    // OrdType(40)

    public final void setOrdType() {
        message.setChar(OrdType.FIELD, OrdType.LIMIT);
    }

    // OrigClOrdId(41)

    public final void setOrigClOrdId(String ref) {
        message.setString(OrigClOrdID.FIELD, ref);
    }

    public final String getOrigClOrdId() throws FieldNotFound {
        return message.getString(OrigClOrdID.FIELD);
    }

    // RefSeqNum(45)

    public final void setRefSeqNum(int refSeqNum) throws FieldNotFound {
        message.setInt(RefSeqNum.FIELD, refSeqNum);
    }

    public final int getRefSeqNum() throws FieldNotFound {
        return message.getInt(RefSeqNum.FIELD);
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

    // Text(58)

    public final void setText(String text) {
        message.setString(Text.FIELD, text);
    }

    public final String getText() throws FieldNotFound {
        return message.getString(Text.FIELD);
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

    // CxlRejReason(102)

    public final void setCxlRejReason(int reason) {
        message.setInt(CxlRejReason.FIELD, reason);
    }

    public final int getCxlRejReason() throws FieldNotFound {
        return message.getInt(CxlRejReason.FIELD);
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

    // RefMsgType(372)

    public final void setRefMsgType(String refMsgType) {
        message.setString(RefMsgType.FIELD, refMsgType);
    }

    public final String getRefMsgType() throws FieldNotFound {
        return message.getString(RefMsgType.FIELD);
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

    // BusinessRejectRefID(379)

    public final void setBusinessRejectRefId(String refId) {
        message.setString(BusinessRejectRefID.FIELD, refId);
    }

    public final String getBusinessRejectRefId() throws FieldNotFound {
        return message.getString(BusinessRejectRefID.FIELD);
    }

    // BusinessRejectReason(380)

    public final void setBusinessRejectReason(int reason) {
        message.setInt(BusinessRejectReason.FIELD, reason);
    }

    public final int getBusinessRejectReason() throws FieldNotFound {
        return message.getInt(BusinessRejectReason.FIELD);
    }

    // CxlRejResponseTo(434)

    public final void setCxlRejResponseTo(char cxlRejResponseTo) {
        message.setChar(CxlRejResponseTo.FIELD, cxlRejResponseTo);
    }

    public final char getCxlRejResponseTo() throws FieldNotFound {
        return message.getChar(CxlRejResponseTo.FIELD);
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
     * @param reason
     *            BusinessRejectReason code.
     * @param text
     *            Optional textual information.
     * @throws FieldNotFound
     */
    public final void setBusinessReject(@NonNull Message refMsg, @Nullable String refId, int reason,
            @Nullable String text) throws FieldNotFound {
        final Header header = refMsg.getHeader();
        setRefMsgType(header.getString(MsgType.FIELD));
        setRefSeqNum(header.getInt(MsgSeqNum.FIELD));
        if (refId != null) {
            setBusinessRejectRefId(refId);
        }
        setBusinessRejectReason(reason);
        if (text != null) {
            setText(text);
        }
    }

    /**
     * @param ref
     *            The cancel-request's reference.
     * @param orderRef
     *            The reference of the order attempting to be cancelled.
     * @param responseTo
     *            The message-type being responded to.
     * @param reason
     *            CxlRejReason code.
     * @param text
     *            Optional textual information.
     */
    public final void setCancelReject(@NonNull String ref, @NonNull String orderRef,
            char responseTo, int reason, @Nullable String text) {
        setClOrdId(ref);
        setOrigClOrdId(orderRef);
        message.setString(OrderID.FIELD, "NONE");
        setOrdStatus(OrdStatus.REJECTED);
        setCxlRejResponseTo(responseTo);
        setCxlRejReason(reason);
        if (text != null) {
            setText(text);
        }
    }

    public final void setCancelReject(@NonNull String ref, @NonNull Order order, char responseTo,
            int reason, @Nullable String text) {
        setClOrdId(ref);
        setOrigClOrdId(order.getRef());
        setOrderId(order.getId());
        setOrdStatus(order.getState(), order.getResd());
        setCxlRejResponseTo(responseTo);
        setCxlRejReason(reason);
        if (text != null) {
            setText(text);
        }
    }

    public final void setInstruct(@NonNull Instruct instruct, @Nullable String ref) {
        setExecId(instruct.getId());
        setOrderId(instruct.getOrderId());
        setAccount(instruct.getTrader());
        setSymbol(instruct.getMarket());
        setContract(instruct.getContr());
        setFutSettDate(instruct.getSettlDay());
        if (ref != null) {
            setClOrdId(ref);
            setOrigClOrdId(instruct.getRef());
        } else {
            setClOrdId(instruct.getRef());
        }
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

    public final void setExec(@NonNull Exec exec, @Nullable String ref) {
        setInstruct(exec, ref);
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

    public final void setNewOrderSingle(@NonNull String market, @NonNull String ref,
            @NonNull Side side, long lots, long ticks, long minLots, long now) {
        setSymbol(market);
        setClOrdId(ref);
        setSide(side);
        setOrdType();
        setOrderQty(lots);
        setPrice(ticks);
        setMinQty(minLots);
        setTransactTime(now);
    }

    public final void setOrderCancelReplaceRequest(@NonNull String market, @NonNull String ref,
            @NonNull String orderRef, long lots, long now) {
        setSymbol(market);
        setClOrdId(ref);
        setOrigClOrdId(orderRef);
        message.setChar(quickfix.field.Side.FIELD, quickfix.field.Side.UNDISCLOSED);
        setOrdType();
        setOrderQty(lots);
        setTransactTime(now);
    }

    public final void setOrderCancelReplaceRequest(@NonNull String market, @NonNull String ref,
            long orderId, long lots, long now) {
        setOrderCancelReplaceRequest(market, ref, "NONE", lots, now);
        setOrderId(orderId);
    }

    public final void setOrderCancelRequest(@NonNull String market, @NonNull String ref,
            @NonNull String orderRef, long now) {
        setSymbol(market);
        setClOrdId(ref);
        setOrigClOrdId(orderRef);
        message.setChar(quickfix.field.Side.FIELD, quickfix.field.Side.UNDISCLOSED);
        setTransactTime(now);
    }

    public final void setOrderCancelRequest(@NonNull String market, @NonNull String ref,
            long orderId, long now) {
        setOrderCancelRequest(market, ref, "NONE", now);
        setOrderId(orderId);
    }
}
