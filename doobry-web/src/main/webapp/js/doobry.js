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

function toDateInt(s) {
    return parseInt(s.substr(0, 4) + s.substr(5, 2) + s.substr(8, 2));
}

function toDateStr(i) {
    var year = Math.floor(i / 10000);
    var mon = Math.floor(i / 100) % 100;
    var mday = i % 100;
    return '' + year + '-' + mon + '-' + mday;
}

ko.bindingHandlers.mnem = {
    update: function(elem, valAccessor) {
        var val = valAccessor();
        $(elem).text(val().mnem);
    }
};

function Book(val, contrs) {
    var self = this;

    var contr = contrs[val.contr];

    self.id = ko.observable(val.id);
    self.contr = ko.observable(contr);
    self.settlDate = ko.observable(toDateStr(val.settlDate));
    self.bidTicks = ko.observable(val.bidTicks[0]);
    self.bidLots = ko.observable(val.bidLots[0]);
    self.bidCount = ko.observable(val.bidCount[0]);
    self.offerTicks = ko.observable(val.offerTicks[0]);
    self.offerLots = ko.observable(val.offerLots[0]);
    self.offerCount = ko.observable(val.offerCount[0]);

    self.bidPrice = ko.computed(function() {
        return ticksToPrice(self.bidTicks(), self.contr());
    }, this);

    self.offerPrice = ko.computed(function() {
        return ticksToPrice(self.offerTicks(), self.contr());
    }, this);
}

function Order(val, contrs) {
    var self = this;

    var contr = contrs[val.contr];

    self.isSelected = ko.observable(val.isSelected);
    self.id = ko.observable(val.id);
    self.user = ko.observable(val.user);
    self.contr = ko.observable(contr);
    self.settlDate = ko.observable(toDateStr(val.settlDate));
    self.ref = ko.observable(val.ref);
    self.state = ko.observable(val.state);
    self.action = ko.observable(val.action);
    self.ticks = ko.observable(val.ticks);
    self.lots = ko.observable(val.lots);
    self.resd = ko.observable(val.resd);
    self.exec = ko.observable(val.exec);
    self.lastTicks = ko.observable(val.lastTicks);
    self.lastLots = ko.observable(val.lastLots);
    self.minLots = ko.observable(val.minLots);
    self.created = ko.observable(val.created);
    self.modified = ko.observable(val.modified);

    self.price = ko.computed(function() {
        return ticksToPrice(self.ticks(), self.contr());
    }, this);

    self.lastPrice = ko.computed(function() {
        return ticksToPrice(self.lastTicks(), self.contr());
    }, this);
}

function Trade(val, contrs) {
    var self = this;

    var contr = contrs[val.contr];

    self.isSelected = ko.observable(val.isSelected);
    self.id = ko.observable(val.id);
    self.orderId = ko.observable(val.orderId);
    self.user = ko.observable(val.user);
    self.contr = ko.observable(contr);
    self.settlDate = ko.observable(toDateStr(val.settlDate));
    self.ref = ko.observable(val.ref);
    self.state = ko.observable(val.state);
    self.action = ko.observable(val.action);
    self.ticks = ko.observable(val.ticks);
    self.lots = ko.observable(val.lots);
    self.resd = ko.observable(val.resd);
    self.exec = ko.observable(val.exec);
    self.lastTicks = ko.observable(val.lastTicks);
    self.lastLots = ko.observable(val.lastLots);
    self.minLots = ko.observable(val.minLots);

    self.matchId = ko.observable(val.matchId);
    self.role = ko.observable(val.role);
    self.cpty = ko.observable(val.cpty);

    self.created = ko.observable(val.created);

    self.price = ko.computed(function() {
        return ticksToPrice(self.ticks(), self.contr());
    }, this);

    self.lastPrice = ko.computed(function() {
        return ticksToPrice(self.lastTicks(), self.contr());
    }, this);
}

function Posn(val, contrs) {
    var self = this;

    var contr = contrs[val.contr];

    self.id = ko.observable(val.id);
    self.user = ko.observable(val.user);
    self.contr = ko.observable(contr);
    self.settlDate = ko.observable(toDateStr(val.settlDate));
    self.buyLicks = ko.observable(val.buyLicks);
    self.buyLots = ko.observable(val.buyLots);
    self.sellLicks = ko.observable(val.sellLicks);
    self.sellLots = ko.observable(val.sellLots);

    self.buyPrice = ko.computed(function() {
        var ticks = 0;
        var lots = self.buyLots();
        if (lots > 0) {
            ticks = fractToReal(self.buyLicks(), lots);
        }
        return ticksToPrice(ticks, self.contr());
    }, this);

    self.sellPrice = ko.computed(function() {
        var ticks = 0;
        var lots = self.sellLots();
        if (lots > 0) {
            ticks = fractToReal(self.sellLicks(), lots);
        }
        return ticksToPrice(ticks, self.contr());
    }, this);
}

function ViewModel(contrs) {
    var self = this;

    self.contrs = contrs;

    self.books = ko.observableArray([]);
    self.orders = ko.observableArray([]);
    self.trades = ko.observableArray([]);
    self.posns = ko.observableArray([]);

    self.contrMnem = ko.observable();
    self.settlDate = ko.observable();
    self.price = ko.observable();
    self.lots = ko.observable();

    self.refresh = function() {

        $.getJSON('/api/book', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new Book(val, self.contrs);
            });
            self.books(cooked);
        });

        $.getJSON('/api/accnt', function(raw) {

            var cooked = $.map(raw.orders, function(val) {
                match = ko.utils.arrayFirst(self.orders(), function(item) {
                    return item.id() === val.id;
                });
                val.isSelected = (match !== null && match.isSelected());
                return new Order(val, self.contrs);
            });
            self.orders(cooked);

            cooked = $.map(raw.trades, function(val) {
                match = ko.utils.arrayFirst(self.trades(), function(item) {
                    return item.id() === val.id;
                });
                val.isSelected = (match !== null && match.isSelected());
                return new Trade(val, self.contrs);
            });
            self.trades(cooked);

            cooked = $.map(raw.posns, function(val) {
                return new Posn(val, self.contrs);
            });
            self.posns(cooked);
        });
    };

    self.selectOrder = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.price());
        self.lots(val.lots());
        return true;
    };

    self.submitOrder = function(action) {
        var contr = self.contrs[self.contrMnem()];
        var settlDate = toDateInt(self.settlDate());
        var ticks = priceToTicks(self.price(), contr);
        var lots = parseInt(self.lots());
        $.ajax({
            type: 'post',
            url: '/api/accnt/order/',
            data: JSON.stringify({
                contr: contr.mnem,
                settlDate: settlDate,
                ref: '',
                action: action,
                ticks: ticks,
                lots: lots,
                minLots: 0
            })
        }).done(function(raw) {
        });
    };

    self.submitBuy = function() {
        self.submitOrder('BUY');
    };

    self.submitSell = function() {
        self.submitOrder('SELL');
    };

    self.cancelOrder = function(id) {
        $.ajax({
            type: 'put',
            url: '/api/accnt/order/' + id,
            data: '{"lots":0}'
        }).done(function(raw) {
        });
    };

    self.confirmTrade = function(id) {
        $.ajax({
            type: 'delete',
            url: '/api/accnt/trade/' + data.id
        }).done(function(raw) {
        });
    };
}

function documentReady() {

    $('#tabs').tab();

    $.getJSON('/api/rec/contr', function(raw) {
        var contrs = [];
        $.each(raw, function(key, val) {
            val.priceInc = priceInc(val);
            val.qtyInc = qtyInc(val);
            contrs[val.mnem] = val;
        });
        $("#contr").typeahead({
            items: 4,
            source: Object.keys(contrs)
        });
        var model = new ViewModel(contrs);
        ko.applyBindings(model);
        model.refresh();
    });
}
