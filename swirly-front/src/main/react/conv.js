/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

function fractToReal(numer, denom) {
    return numer / denom;
}

function realToIncs(real, incSize) {
    return roundHalfAway(real / incSize);
}

function incsToReal(incs, incSize) {
    return incs * incSize;
}

function qtyToLots(qty, contr) {
    return realToIncs(qty, contr.qtyInc);
}

function lotsToQty(lots, contr) {
    return incsToReal(lots, contr.qtyInc).toFixed(contr.qtyDp);
}

function priceToTicks(price, contr) {
    return realToIncs(price, contr.priceInc);
}

function ticksToPrice(ticks, contr) {
    return incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp);
}

function realToDp(d) {
    var dp = 0;
    for (; dp < 9; ++dp) {
        var fp = d % 1.0;
        if (fp < 0.000000001) {
            break;
        }
        d *= 10;
    }
    return dp;
}

function toDateInt(s) {
    return parseInt(s.substr(0, 4) + s.substr(5, 2) + s.substr(8, 2));
}

function toDateStr(i) {
    var year = Math.floor(i / 10000);
    var mon = Math.floor(i / 100) % 100;
    var mday = i % 100;
    return ('000' + year).slice(-4)
        + '-' + ('0' + mon).slice(-2)
        + '-' + ('0' + mday).slice(-2);
}

function toTimeStr(ms) {
    var date = new Date(ms);
    return ('0' + date.getHours()).slice(-2)
        + ':' + ('0' + date.getMinutes()).slice(-2)
        + ':' + ('0' + date.getSeconds()).slice(-2);
}
