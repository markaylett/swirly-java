/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

function ViewModel() {
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

    self.traders = ko.observableArray([]);

    self.mnem = ko.observable();
    self.display = ko.observable();
    self.email = ko.observable();

    self.findTrader = function(mnem) {
        return ko.utils.arrayFirst(self.traders(), function(val) {
            return val.mnem() == mnem;
        });
    };

    self.refreshAll = function() {

        $.getJSON('/api/rec/trader', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new Trader(val);
            });
            self.traders(cooked);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.clearTrader = function() {
        self.mnem(undefined);
        self.display(undefined);
        self.email(undefined);
    };

    self.submitTrader = function() {
        var mnem = self.mnem();
        var display = self.display();
        var email = self.email();
        $.ajax({
            type: 'post',
            url: '/api/rec/trader/',
            data: JSON.stringify({
                mnem: mnem,
                display: display,
                email: email
            })
        }).done(function(raw) {
            trader = self.findTrader(raw.id);
            if (trader !== null) {
                trader.update(raw);
            } else {
                raw.isSelected = false;
                self.traders.push(new Trader(raw));
            }
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };
}

function initApp() {

    var model = new ViewModel();
    ko.applyBindings(model);
    model.refreshAll();
    setInterval(function() {
        model.refreshAll();
    }, 5000);
}
