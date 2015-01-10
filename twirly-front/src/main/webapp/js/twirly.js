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

ko.bindingHandlers.depth = {
    update: function(elem, valAccessor, allBindings, viewModel, bindingContext) {
        var val = valAccessor();
        var arr = val();
        if (!bindingContext.$rawData.isSelected()) {
            $(elem).text(optional(arr[0]));
            return;
        }
        var html = '';
        for (var i = 0; i < arr.length; ++i) {
            if (i > 0) {
                html += '<br/>';
            }
            html += optional(arr[i]);
        }
        $(elem).html(html);
    }
};

function Error(val) {
    var self = this;

    if ('responseText' in val) {
        if (val.responseText.length > 0
            && (val.getResponseHeader('Content-Type') || '')
            .indexOf('application/json') >= 0) {
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
    self.pipDp = ko.observable(val.pipDp);
    self.minLots = ko.observable(val.minLots);
    self.maxLots = ko.observable(val.maxLots);

    self.priceDp = ko.computed(function() {
        var tickNumer = self.tickNumer();
        var tickDenom = self.tickDenom();
        return realToDp(fractToReal(tickNumer, tickDenom));
    });

    self.qtyDp = ko.computed(function() {
        var lotNumer = self.lotNumer();
        var lotDenom = self.lotDenom();
        return realToDp(fractToReal(lotNumer, lotDenom));
    });

    self.priceInc = ko.computed(function() {
        var tickNumer = self.tickNumer();
        var tickDenom = self.tickDenom();
        return fractToReal(tickNumer, tickDenom).toFixed(self.priceDp());
    });

    self.qtyInc = ko.computed(function() {
        var lotNumer = self.lotNumer();
        var lotDenom = self.lotDenom();
        return fractToReal(lotNumer, lotDenom).toFixed(self.qtyDp());
    });
}

function Trader(val) {
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
    self.fixingDate = ko.observable(toDateStr(val.fixingDate));
    self.expiryDate = ko.observable(toDateStr(val.expiryDate));
    self.bidTicks = ko.observableArray(val.bidTicks);
    self.bidLots = ko.observableArray(val.bidLots);
    self.bidCount = ko.observableArray(val.bidCount);
    self.offerTicks = ko.observableArray(val.offerTicks);
    self.offerLots = ko.observableArray(val.offerLots);
    self.offerCount = ko.observableArray(val.offerCount);
    self.lastTicks = ko.observable(val.lastTicks);
    self.lastLots = ko.observable(val.lastLots);
    self.lastTime = ko.observable(val.lastTime);

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

    self.lastPrice = ko.computed(function() {
        var ticks = self.lastTicks();
        return ticks !== null ? ticksToPrice(ticks, self.contr()) : null;
    });

    self.update = function(val) {
        self.fixingDate(toDateStr(val.fixingDate));
        self.expiryDate(toDateStr(val.expiryDate));
        self.bidTicks(val.bidTicks);
        self.bidLots(val.bidLots);
        self.bidCount(val.bidCount);
        self.offerTicks(val.offerTicks);
        self.offerLots(val.offerLots);
        self.offerCount(val.offerCount);
        self.lastTicks(val.lastTicks);
        self.lastLots(val.lastLots);
        self.lastTime(val.lastTime);
    };
}

function Order(val, contrs) {
    var self = this;

    var contr = contrs[val.contr];

    self.isSelected = ko.observable(val.isSelected);
    self.id = ko.observable(val.id);
    self.trader = ko.observable(val.trader);
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
    self.trader = ko.observable(val.trader);
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
    self.trader = ko.observable(val.trader);
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