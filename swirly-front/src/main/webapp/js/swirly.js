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

ko.bindingHandlers.optnum = {
    update: function(elem, valAccessor) {
        var val = valAccessor();
        $(elem).text(parseFloat(val()) === 0 ? '-' : val());
    }
};

function Error(val) {
    var self = this;

    if ('responseText' in val) {
        if (val.responseText.length > 0) {
            val = $.parseJSON(val.responseText);
        } else {
            val = {
                num: val.status,
                msg: val.statusText
            };
        }
    }
    self.num = ko.observable(val.num);
    self.msg = ko.observable(val.msg);
}

function Contr(val) {
    var self = this;

    self.mnem = ko.observable(val.mnem);
    self.display = ko.observable(val.display);
    self.assetType = ko.observable(val.assetType);
    self.asset = ko.observable(val.asset);
    self.ccy = ko.observable(val.ccy);
    self.tickNumer = ko.observable(val.tickNumer);
    self.tickDenom = ko.observable(val.tickDenom);
    self.lotNumer = ko.observable(val.lotNumer);
    self.lotDenom = ko.observable(val.lotDenom);
    self.priceDp = ko.observable(val.priceDp);
    self.pipDp = ko.observable(val.pipDp);
    self.qtyDp = ko.observable(val.qtyDp);
    self.minLots = ko.observable(val.minLots);
    self.maxLots = ko.observable(val.maxLots);
}

function User(val) {
    var self = this;

    self.mnem = ko.observable(val.mnem);
    self.display = ko.observable(val.display);
    self.email = ko.observable(val.email);
}

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
    });

    self.offerPrice = ko.computed(function() {
        return ticksToPrice(self.offerTicks(), self.contr());
    });

    self.update = function(val) {
        self.bidTicks(val.bidTicks[0]);
        self.bidLots(val.bidLots[0]);
        self.bidCount(val.bidCount[0]);
        self.offerTicks(val.offerTicks[0]);
        self.offerLots(val.offerLots[0]);
        self.offerCount(val.offerCount[0]);
    };
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
    });

    self.lastPrice = ko.computed(function() {
        return ticksToPrice(self.lastTicks(), self.contr());
    });

    self.update = function(val) {
        self.state(val.state);
        self.lots(val.lots);
        self.resd(val.resd);
        self.exec(val.exec);
        self.lastTicks(val.lastTicks);
        self.lastLots(val.lastLots);
        self.modified(val.modified);
    };
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
    });

    self.lastPrice = ko.computed(function() {
        return ticksToPrice(self.lastTicks(), self.contr());
    });
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
        if (lots !== 0) {
            ticks = fractToReal(self.buyLicks(), lots);
        }
        return ticksToPrice(ticks, self.contr());
    });

    self.sellPrice = ko.computed(function() {
        var ticks = 0;
        var lots = self.sellLots();
        if (lots !== 0) {
            ticks = fractToReal(self.sellLicks(), lots);
        }
        return ticksToPrice(ticks, self.contr());
    });

    self.netPrice = ko.computed(function() {
        var ticks = 0;
        var licks = self.buyLicks() - self.sellLicks();
        var lots = self.buyLots() - self.sellLots();
        if (lots !== 0) {
            ticks = fractToReal(licks, lots);
        }
        return ticksToPrice(ticks, self.contr());
    });

    self.netLots = ko.computed(function() {
        return self.buyLots() - self.sellLots();
    });

    self.update = function(val) {
        self.buyLicks(val.buyLicks);
        self.buyLots(val.buyLots);
        self.sellLicks(val.sellLicks);
        self.sellLots(val.sellLots);
    };
}
