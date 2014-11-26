/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

function ViewModel() {
    var self = this;

    self.users = ko.observableArray([]);

    self.refreshAll = function() {

        $.getJSON('/api/rec/user', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new User(val);
            });
            self.users(cooked);
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
