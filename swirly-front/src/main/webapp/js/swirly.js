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
    return ('000' + year).slice(-4)
        + '-' + ('0' + mon).slice(-2)
        + '-' + ('0' + mday).slice(-2);
}

function optional(x) {
    return x !== null ? x : '-';
}

ko.bindingHandlers.mnem = {
    update: function(elem, valAccessor) {
        var val = valAccessor();
        $(elem).text(val().mnem);
    }
};

ko.bindingHandlers.optional = {
    update: function(elem, valAccessor) {
        var val = valAccessor();
        $(elem).text(optional(val()));
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

function internalError(msg) {
    return new Error({
        num: 500,
        msg: msg
    });
}

function isSpecified(x) {
    return x !== undefined && x !== '';
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

function Market(val, contrs) {
    var self = this;

    var contr = contrs[val.contr];

    self.isSelected = ko.observable(val.isSelected);
    self.id = ko.observable(val.id);
    self.contr = ko.observable(contr);
    self.settlDate = ko.observable(toDateStr(val.settlDate));
    self.bidTicks = ko.observableArray(val.bidTicks);
    self.bidLots = ko.observableArray(val.bidLots);
    self.bidCount = ko.observableArray(val.bidCount);
    self.offerTicks = ko.observableArray(val.offerTicks);
    self.offerLots = ko.observableArray(val.offerLots);
    self.offerCount = ko.observableArray(val.offerCount);

    self.bidPrice = ko.computed(function() {
        // We use Ko's map function because jQuery's ignores null elements.
        return ko.utils.arrayMap(self.bidTicks(), function(val) {
            return val !== null ? ticksToPrice(val, self.contr()) : null;
        });
    });

    self.offerPrice = ko.computed(function() {
        // We use Ko's map function because jQuery's ignores null elements.
        return ko.utils.arrayMap(self.offerTicks(), function(val) {
            return val !== null ? ticksToPrice(val, self.contr()) : null;
        });
    });

    self.update = function(val) {
        self.bidTicks(val.bidTicks);
        self.bidLots(val.bidLots);
        self.bidCount(val.bidCount);
        self.offerTicks(val.offerTicks);
        self.offerLots(val.offerLots);
        self.offerCount(val.offerCount);
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
        var ticks = self.lastTicks();
        return ticks !== null ? ticksToPrice(ticks, self.contr()) : null;
    });

    self.isDone = function() {
        return self.resd() === 0;
    };

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
        var contr = self.contr();
        // Extra decimal place.
        return incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp + 1);
    });

    self.sellPrice = ko.computed(function() {
        var ticks = 0;
        var lots = self.sellLots();
        if (lots !== 0) {
            ticks = fractToReal(self.sellLicks(), lots);
        }
        var contr = self.contr();
        // Extra decimal place.
        return incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp + 1);
    });

    self.netPrice = ko.computed(function() {
        var ticks = 0;
        var licks = self.buyLicks() - self.sellLicks();
        var lots = self.buyLots() - self.sellLots();
        if (lots !== 0) {
            ticks = fractToReal(licks, lots);
        }
        var contr = self.contr();
        // Extra decimal place.
        return incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp + 1);
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
