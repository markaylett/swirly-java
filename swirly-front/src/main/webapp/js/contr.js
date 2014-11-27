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
        // Limit to last 10 errors.
        if (self.errors().length > 10) {
            self.errors.pop();
        }
    };

    self.contrs = ko.observableArray([]);

    self.refreshAll = function() {

        $.getJSON('/api/rec/contr', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new Contr(val);
            });
            self.contrs(cooked);
        }).fail(function(xhr) {
            self.showError(new Error($.parseJSON(xhr.responseText)));
        });
    };
}

function initApp() {

    var model = new ViewModel();
    ko.applyBindings(model);
    model.refreshAll();
    setInterval(function() {
        model.refreshAll();
    }, 10000);
}
