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

ko.bindingHandlers.mnem = {
    update: function(elem, valAccessor) {
        var val = valAccessor();
        $(elem).text(val().mnem);
    }
};

function Book(val, contrs) {

    var contr = contrs[val.contr];

    this.id = ko.observable(val.id);
    this.contr = ko.observable(contr);
    this.settlDate = ko.observable(val.settlDate);
    this.bidTicks = ko.observable(val.bidTicks[0]);
    this.bidLots = ko.observable(val.bidLots[0]);
    this.bidCount = ko.observable(val.bidCount[0]);
    this.offerTicks = ko.observable(val.offerTicks[0]);
    this.offerLots = ko.observable(val.offerLots[0]);
    this.offerCount = ko.observable(val.offerCount[0]);

    this.bidPrice = ko.computed(function() {
        return ticksToPrice(this.bidTicks(), this.contr());
    }, this);

    this.offerPrice = ko.computed(function() {
        return ticksToPrice(this.offerTicks(), this.contr());
    }, this);
}

function Order(val, contrs) {

    var contr = contrs[val.contr];

    this.id = ko.observable(val.id);
    this.user = ko.observable(val.user);
    this.contr = ko.observable(contr);
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

    this.price = ko.computed(function() {
        return ticksToPrice(this.ticks(), this.contr());
    }, this);

    this.lastPrice = ko.computed(function() {
        return ticksToPrice(this.lastTicks(), this.contr());
    }, this);
}

function Trade(val, contrs) {

    var contr = contrs[val.contr];

    this.id = ko.observable(val.id);
    this.orderId = ko.observable(val.orderId);
    this.user = ko.observable(val.user);
    this.contr = ko.observable(contr);
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

    this.matchId = ko.observable(val.matchId);
    this.role = ko.observable(val.role);
    this.cpty = ko.observable(val.cpty);

    this.created = ko.observable(val.created);

    this.price = ko.computed(function() {
        return ticksToPrice(this.ticks(), this.contr());
    }, this);

    this.lastPrice = ko.computed(function() {
        return ticksToPrice(this.lastTicks(), this.contr());
    }, this);
}

function Posn(val, contrs) {
}

function ViewModel(contrs) {
    var self = this;
    self.contrs = contrs;
    self.books = ko.observableArray([]);
    self.orders = ko.observableArray([]);
    self.trades = ko.observableArray([]);
    self.posns = ko.observableArray([]);
}

ViewModel.prototype.refresh = function() {
    var self = this;

    $.getJSON('/api/book', function(raw) {

        var cooked = $.map(raw, function(val) {
            return new Book(val, self.contrs);
        });
        self.books(cooked);
    });

    $.getJSON('/api/accnt', function(raw) {

        var cooked = $.map(raw.orders, function(val) {
            return new Order(val, self.contrs);
        });
        self.orders(cooked);

        cooked = $.map(raw.trades, function(val) {
            return new Trade(val, self.contrs);
        });
        self.trades(cooked);

        cooked = $.map(raw.posns, function(val) {
            return new Posn(val, self.contrs);
        });
        self.posns(cooked);
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
