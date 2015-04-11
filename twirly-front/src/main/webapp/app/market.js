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

    self.mnem = ko.observable();
    self.display = ko.observable();
    self.contr = ko.observable();
    self.settlDate = ko.observable();
    self.expiryDate = ko.observable();
    self.state = ko.observable();

    self.settlDate.subscribe(function(val) {
        var expiryDate = self.expiryDate();
        if (!isSpecified(expiryDate)) {
            self.expiryDate(val);
        }
    });

    self.findMarket = function(mnem) {
        return ko.utils.arrayFirst(self.markets(), function(val) {
            return val.mnem() === mnem;
        });
    };

    self.refreshAll = function() {

        $.getJSON('/api/rec/market', function(raw) {

            var cooked = $.map(raw, function(val) {
                market = self.findMarket(val.mnem);
                if (market !== null) {
                    market.update(val);
                } else {
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
        //self.mnem(undefined);
        //self.display(undefined);
        //self.contr(undefined);
        //self.settlDate(undefined);
        //self.expiryDate(undefined);
        //self.state(undefined);
    };

    self.submitMarket = function() {
        var mnem = self.mnem();
        if (!isSpecified(mnem)) {
            self.showError(internalError('mnem not specified'));
            return;
        }
        var display = self.display();
        if (!isSpecified(display)) {
            self.showError(internalError('display not specified'));
            return;
        }
        var contr = self.contr();
        if (!isSpecified(contr)) {
            self.showError(internalError('contract not specified'));
            return;
        }
        if (!(contr in self.contrs)) {
            self.showError(internalError('invalid contract: ' + contr));
            return;
        }
        var settlDate = self.settlDate();
        if (!isSpecified(settlDate)) {
            self.showError(internalError('settl-date not specified'));
            return;
        }
        settlDate = toDateInt(settlDate);
        var expiryDate = self.expiryDate();
        if (!isSpecified(expiryDate)) {
            self.showError(internalError('expiry-date not specified'));
            return;
        }
        expiryDate = toDateInt(expiryDate);
        var state = self.state();
        if (!isSpecified(state)) {
            self.showError(internalError('state not specified'));
            return;
        }
        state = parseInt(state);

        $.ajax({
            type: 'post',
            url: '/api/rec/market/',
            data: JSON.stringify({
                mnem: mnem,
                display: display,
                contr: contr,
                settlDate: settlDate,
                expiryDate: expiryDate,
                state: state
            })
        }).done(function(raw) {
            market = self.findMarket(raw.mnem);
            if (market !== null) {
                market.update(raw);
            } else {
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
