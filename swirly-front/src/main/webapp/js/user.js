/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

function ViewModel() {
    var self = this;

    self.errors = ko.observableArray([]);
    self.users = ko.observableArray([]);

    self.mnem = ko.observable();
    self.display = ko.observable();
    self.email = ko.observable();

    self.haveErrors = ko.computed(function() {
        return self.errors().length > 0;
    });

    self.clearErrors = function() {
        self.errors.removeAll();
    };

    self.showError = function(error) {
        self.errors.unshift(error);
        if (self.errors().length > 10) {
            self.errors.pop();
        }
    };

    self.clearUser = function() {
        self.mnem('MARAYL');
        self.display('Mark Aylett');
        self.email('mark.aylett@swirlycloud.com');
    };

    self.applyTrans = function(raw) {
    };

    self.refreshAll = function() {

        $.getJSON('/api/rec/user', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new User(val);
            });
            self.users(cooked);
        }).fail(function(xhr) {
            self.showError(new Error($.parseJSON(xhr.responseText)));
        });
    };

    self.submitUser = function() {
        console.log(self.mnem() + ' ' + self.display() + ' ' + self.email());
        $.ajax({
            type: 'post',
            url: '/api/rec/user/',
            data: JSON.stringify({
                mnem: self.mnem(),
                display: self.display(),
                email: self.email()
            })
        }).done(function(raw) {
            self.applyTrans(raw);
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
