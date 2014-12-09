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
}

function initApp() {

    $.getJSON('/api/rec/contr', function(raw) {
        var contrs = [];
        $.each(raw, function(key, val) {
            val.priceInc = priceInc(val);
            val.qtyInc = qtyInc(val);
            contrs[val.mnem] = val;
        });
        var model = new ViewModel(contrs);
        ko.applyBindings(model);
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
