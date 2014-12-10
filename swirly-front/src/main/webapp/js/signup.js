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

    self.mnem = ko.observable();
    self.display = ko.observable();

    self.signup = function() {
        var mnem = self.mnem();
        var display = self.display();
        $.ajax({
            type: 'post',
            url: '/api/rec/trader/',
            data: JSON.stringify({
                mnem: mnem,
                display: display
            })
        }).done(function(raw) {
            window.location.reload();
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };
}

function initApp() {

    var model = new ViewModel();
    ko.applyBindings(model);
}
