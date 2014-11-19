/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

function fractToReal(numer, denom) {
    return numer / denom;
}

function realToIncs(real, incSize) {
    return Math.round(real / incSize);
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

function qtyInc(contr) {
    return fractToReal(contr.lotNumer, contr.lotDenom).toFixed(contr.qtyDp);
}

function priceInc(contr) {
    return fractToReal(contr.tickNumer, contr.tickDenom).toFixed(contr.priceDp);
}

function Order(val, contrs) {

    this.id = ko.observable(val.id);
    this.user = ko.observable(val.user);
    this.contr = ko.observable(val.contr);
    this.settlDate = ko.observable(val.settlDate);
    this.ref = ko.observable(val.ref);
    this.state = ko.observable(val.state);
    this.action = ko.observable(val.action);
    this.ticks = ko.observable(val.ticks);
    this.lots = ko.observable(val.lots);
    this.resd = ko.observable(val.resd);
    this.exec = ko.observable(val.exec);
    this.lastTicks = ko.observable(val.lastTicks);
    this.lastLots = ko.observable(val.lastLots);
    this.minLots = ko.observable(val.minLots);
    this.created = ko.observable(val.created);
    this.modified = ko.observable(val.modified);

    var contr = contrs[val.contr];
    this.price = ticksToPrice(val.ticks, contr);
    this.lastPrice = ticksToPrice(val.lastTicks, contr);
}

function ViewModel(contrs) {
    var self = this;
    self.contrs = contrs;
    self.orders = ko.observableArray([]);
}

ViewModel.prototype.refresh = function() {
    var self = this;
    $.getJSON('/api/accnt/order', function(raw) {
        var cooked = $.map(raw, function(val) {
            return new Order(val, self.contrs);
        });
        self.orders(cooked);
    });
};

function documentReady() {

    $('#tabs').tab();

    $.getJSON('/api/rec/contr', function(raw) {
        var contrs = [];
        $.each(raw, function(key, val) {
            val.priceInc = priceInc(val);
            val.qtyInc = qtyInc(val);
            contrs[val.mnem] = val;
        });
        var model = new ViewModel(contrs);
        ko.applyBindings(model);
        model.refresh();
    });
}
