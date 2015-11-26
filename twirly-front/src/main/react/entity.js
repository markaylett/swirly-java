/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

function enrichAsset(asset) {
    asset.key = asset.mnem;
}

function enrichContr(contr) {

    contr.key = contr.mnem;

    var lotNumer = contr.lotNumer;
    var lotDenom = contr.lotDenom;
    var qtyInc = fractToReal(lotNumer, lotDenom);

    var tickNumer = contr.tickNumer;
    var tickDenom = contr.tickDenom;
    var priceInc = fractToReal(tickNumer, tickDenom);

    contr.qtyDp = realToDp(qtyInc);
    contr.qtyInc = qtyInc.toFixed(contr.qtyDp);

    contr.priceDp = realToDp(priceInc);
    contr.priceInc = priceInc.toFixed(contr.priceDp);
}

function enrichMarket(contrMap, market) {

    market.key = market.mnem;

    market.contr = contrMap[market.contr];
    market.settlDate = isSpecified(market.settlDate) ? toDateStr(market.settlDate) : null;
    market.expiryDate = isSpecified(market.expiryDate) ? toDateStr(market.expiryDate) : null;
}

function enrichTrader(trader) {
    trader.key = trader.mnem;
}

function enrichView(contrMap, view) {

    view.key = view.market;

    var contr = contrMap[view.contr];
    view.contr = contr;
    view.settlDate = isSpecified(view.settlDate) ? toDateStr(view.settlDate) : null;
    view.bidPrice = view.bidTicks.map(function(val) {
        return val !== null ? ticksToPrice(val, contr) : null;
    });
    view.offerPrice = view.offerTicks.map(function(val) {
        return val !== null ? ticksToPrice(val, contr) : null;
    });
    view.lastPrice = isSpecified(view.lastTicks) ? ticksToPrice(view.lastTicks, contr) : null;
}

function enrichOrder(contrMap, order) {

    order.key = order.market + '/' + zeroPad(order.id);

    var contr = contrMap[order.contr];
    order.contr = contr;
    order.settlDate = isSpecified(order.settlDate) ? toDateStr(order.settlDate) : null;
    order.price = ticksToPrice(order.ticks, contr);
    order.lastPrice = isSpecified(order.lastTicks) ? ticksToPrice(order.lastTicks, contr) : null;
    order.isDone = order.resd === 0;
}

function enrichTrade(contrMap, trade) {

    trade.key = trade.market + '/' + zeroPad(trade.id);

    var contr = contrMap[trade.contr];
    trade.contr = contr;
    trade.settlDate = isSpecified(trade.settlDate) ? toDateStr(trade.settlDate) : null;
    trade.price = ticksToPrice(trade.ticks, contr);
    trade.lastPrice = isSpecified(trade.lastTicks) ? ticksToPrice(trade.lastTicks, contr) : null;
}

function posnPrice(lots, cost, contr) {
    var ticks = 0;
    if (lots !== 0) {
        ticks = fractToReal(cost, lots);
    }
    // Extra decimal place.
    return incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp + 1);
}

function calcNetPrice(posn) {
    var lots = posn.buyLots - posn.sellLots;
    var cost = posn.buyCost - posn.sellCost;
    var ticks = 0;
    if (lots !== 0) {
        ticks = fractToReal(cost, lots);
    }
    var contr = posn.contr;
    // Extra decimal place.
    return incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp + 1);
}

function enrichPosn(contrMap, posn) {

    posn.key = posn.contr + '/' + posn.settlDate;

    var contr = contrMap[posn.contr];
    posn.contr = contr;
    posn.settlDate = isSpecified(posn.settlDate) ? toDateStr(posn.settlDate) : null;
    posn.buyPrice = posnPrice(posn.buyLots, posn.buyCost, contr);
    posn.sellPrice = posnPrice(posn.sellLots, posn.sellCost, contr);
    posn.netLots = posn.buyLots - posn.sellLots;
    posn.netPrice = calcNetPrice(posn);
}

function enrichQuote(contrMap, quote) {

    quote.key = quote.market + '/' + zeroPad(quote.id);

    var contr = contrMap[quote.contr];
    quote.contr = contr;
    quote.settlDate = isSpecified(quote.settlDate) ? toDateStr(quote.settlDate) : null;
    quote.price = ticksToPrice(quote.ticks, contr);
}
