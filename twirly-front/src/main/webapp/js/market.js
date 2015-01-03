/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

function ViewModel(contrs) {
    var self = this;

    self.errors = ko.observableArray([]);

    self.clearErrors = function() {
        self.errors.removeAll();
    };

    self.hasErrors = ko.computed(function() {
        return self.errors().length > 0;
    });

    self.showError = function(error) {
        // Add to top of list.
        self.errors.unshift(error);
        // Limit to last 5 errors.
        if (self.errors().length > 5) {
            self.errors.pop();
        }
    };

    self.contrs = contrs;

    self.markets = ko.observableArray([]);
    self.allMarkets = ko.observable(false);
    self.markets.extend({ rateLimit: 25 });

    self.contrMnem = ko.observable();
    self.settlDate = ko.observable();
    self.fixingDate = ko.observable();
    self.expiryDate = ko.observable();

    self.isMarketSelected = ko.computed(function() {
        var markets = self.markets();
        for (var i = 0; i < markets.length; ++i) {
            if (markets[i].isSelected())
                return true;
        }
        return false;
    });

    self.allMarkets.subscribe(function(val) {
        var markets = self.markets();
        for (var i = 0; i < markets.length; ++i) {
            markets[i].isSelected(val);
        }
    });

    self.findMarket = function(id) {
        return ko.utils.arrayFirst(self.markets(), function(val) {
            return val.id() === id;
        });
    };

    self.refreshAll = function() {

        $.getJSON('/api/market', function(raw) {

            var cooked = $.map(raw, function(val) {
                market = self.findMarket(val.id);
                if (market !== null) {
                    market.update(val);
                } else {
                    val.isSelected = false;
                    market = new Market(val, self.contrs);
                }
                return market;
            });
            self.markets(cooked);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.clearMarket = function() {
        //self.contrMnem(undefined);
        //self.settlDate(undefined);
        //self.fixingDate(undefined);
        //self.expiryDate(undefined);
    };

    self.submitMarket = function() {
        var contr = self.contrMnem();
        if (!isSpecified(contr)) {
            self.showError(internalError('contract not specified'));
            return;
        }
        contr = self.contrs[contr];
        if (contr === undefined) {
            self.showError(internalError('invalid contract: ' + self.contrMnem()));
            return;
        }
        var settlDate = self.settlDate();
        if (!isSpecified(settlDate)) {
            self.showError(internalError('settl-date not specified'));
            return;
        }
        settlDate = toDateInt(settlDate);
        var fixingDate = self.fixingDate();
        if (!isSpecified(fixingDate)) {
            self.showError(internalError('fixing-date not specified'));
            return;
        }
        fixingDate = toDateInt(fixingDate);
        var expiryDate = self.expiryDate();
        if (!isSpecified(expiryDate)) {
            self.showError(internalError('expiry-date not specified'));
            return;
        }
        expiryDate = toDateInt(expiryDate);

        $.ajax({
            type: 'post',
            url: '/api/market/' + contr.mnem,
            data: JSON.stringify({
                settlDate: settlDate,
                fixingDate: fixingDate,
                expiryDate: expiryDate
            })
        }).done(function(raw) {
            market = self.findMarket(raw.id);
            if (market !== null) {
                market.update(raw);
            } else {
                raw.isSelected = false;
                self.markets.push(new Market(raw, self.contrs));
            }
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };
}

function initApp() {

    $.getJSON('/api/rec/contr', function(raw) {
        var contrs = [];
        $.each(raw, function(key, val) {

            var tickNumer = val.tickNumer;
            var tickDenom = val.tickDenom;
            var priceInc = fractToReal(tickNumer, tickDenom);

            var lotNumer = val.lotNumer;
            var lotDenom = val.lotDenom;
            var qtyInc = fractToReal(lotNumer, lotDenom);

            val.priceDp = realToDp(priceInc);
            val.priceInc = priceInc.toFixed(val.priceDp);

            val.qtyDp = realToDp(qtyInc);
            val.qtyInc = qtyInc.toFixed(val.qtyDp);

            contrs[val.mnem] = val;
        });
        var model = new ViewModel(contrs);
        ko.applyBindings(model);
        $('#contr').typeahead({
            items: 4,
            source: Object.keys(contrs)
        });
        model.refreshAll();
        setInterval(function() {
            model.refreshAll();
        }, 5000);
    }).fail(function(xhr) {
        var model = new ViewModel([]);
        ko.applyBindings(model);
        model.showError(new Error(xhr));
    });
}
