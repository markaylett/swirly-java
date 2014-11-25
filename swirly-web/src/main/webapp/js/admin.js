/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

function ViewModel(contrs) {
    var self = this;

    self.contrs = ko.observableArray([]);

    self.refreshAll = function() {

        $.getJSON('/api/rec/contr', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new Contr(val);
            });
            self.contrs(cooked);
        });
    };
}

function documentReady() {

    var model = new ViewModel();
    ko.applyBindings(model);
    model.refreshAll();
    setInterval(function() {
        model.refreshAll();
    }, 10000);
}
