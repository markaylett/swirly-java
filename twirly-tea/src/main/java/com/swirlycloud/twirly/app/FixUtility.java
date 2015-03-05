/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import quickfix.IncorrectTagValue;
import quickfix.field.ExecType;
import quickfix.field.LastLiquidityInd;
import quickfix.field.OrdStatus;
import quickfix.field.Side;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.State;

public final class FixUtility {
    private FixUtility() {
    }

    // OrdStatus(39)

    public static char stateToOrdStatus(State in, long resd) {
        char out;
        switch (in) {
        case NEW:
            out = OrdStatus.NEW;
            break;
        case REVISE:
            out = OrdStatus.REPLACED;
            break;
        case CANCEL:
            out = OrdStatus.CANCELED;
            break;
        case TRADE:
            out = resd == 0 ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED;
            break;
        default:
            out = '\0';
            assert false;
        }
        return out;
    }

    public static State ordStatusToState(char in) throws IncorrectTagValue {
        State out = null;
        switch (in) {
        case OrdStatus.NEW:
            out = State.NEW;
            break;
        case OrdStatus.REPLACED:
            out = State.REVISE;
            break;
        case OrdStatus.CANCELED:
            out = State.CANCEL;
            break;
        case OrdStatus.FILLED:
        case OrdStatus.PARTIALLY_FILLED:
            out = State.TRADE;
            break;
        default:
            throw new IncorrectTagValue(OrdStatus.FIELD);
        }
        return out;
    }

    // Side(54)

    public static char actionToSide(Action in) {
        char out;
        switch (in) {
        case BUY:
            out = Side.BUY;
            break;
        case SELL:
            out = Side.SELL;
            break;
        default:
            out = '\0';
            assert false;
        }
        return out;
    }

    public static Action sideToAction(char in) throws IncorrectTagValue {
        Action out = null;
        switch (in) {
        case Side.BUY:
            out = Action.BUY;
            break;
        case Side.SELL:
            out = Action.SELL;
            break;
        default:
            throw new IncorrectTagValue(Side.FIELD);
        }
        return out;
    }

    // ExecType(150)

    public static char stateToExecType(State in, long resd) {
        char out;
        switch (in) {
        case NEW:
            out = ExecType.NEW;
            break;
        case REVISE:
            out = ExecType.REPLACE;
            break;
        case CANCEL:
            out = ExecType.CANCELED;
            break;
        case TRADE:
            out = resd == 0 ? ExecType.FILL : ExecType.PARTIAL_FILL;
            break;
        default:
            out = '\0';
            assert false;
        }
        return out;
    }

    public static State execTypeToState(char in) throws IncorrectTagValue {
        State out = null;
        switch (in) {
        case ExecType.NEW:
            out = State.NEW;
            break;
        case ExecType.REPLACE:
            out = State.REVISE;
            break;
        case ExecType.CANCELED:
            out = State.CANCEL;
            break;
        case ExecType.FILL:
        case ExecType.PARTIAL_FILL:
            out = State.TRADE;
            break;
        default:
            throw new IncorrectTagValue(ExecType.FIELD);
        }
        return out;
    }

    // LastLiquidityInd(851)

    public static int roleToLastLiquidityInd(Role in) {
        int out;
        switch (in) {
        case MAKER:
            out = LastLiquidityInd.ADDED_LIQUIDITY;
            break;
        case TAKER:
            out = LastLiquidityInd.REMOVED_LIQUIDITY;
            break;
        default:
            out = '\0';
            assert false;
        }
        return out;
    }

    public static Role lastLiquidityIndToRole(int in) throws IncorrectTagValue {
        Role out = null;
        switch (in) {
        case LastLiquidityInd.ADDED_LIQUIDITY:
            out = Role.MAKER;
            break;
        case LastLiquidityInd.REMOVED_LIQUIDITY:
            out = Role.TAKER;
            break;
        default:
            throw new IncorrectTagValue(LastLiquidityInd.FIELD);
        }
        return out;
    }

}
