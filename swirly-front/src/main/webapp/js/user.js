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

    self.users = ko.observableArray([]);

    self.mnem = ko.observable();
    self.display = ko.observable();
    self.email = ko.observable();

    self.clearUser = function() {
        self.mnem('');
        self.display('');
        self.email('');
    };

    self.refreshAll = function() {

        $.getJSON('/api/rec/user', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new User(val);
            });
            self.users(cooked);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.submitUser = function() {
        var mnem = self.mnem();
        var display = self.display();
        var email = self.email();
        $.ajax({
            type: 'post',
            url: '/api/rec/user/',
            data: JSON.stringify({
                mnem: mnem,
                display: display,
                email: email
            })
        }).done(function(raw) {
            self.books.push(new User(mnem, display, email));
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
    }, 10000);
}
