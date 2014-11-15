/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 * 
 * All rights reserved.
 ******************************************************************************/
module('conversion');

test('fraction to real', function() {
    equal(dbr.fractToReal(100, 1).toFixed(2), "100.00", 'one hundred');
    equal(dbr.fractToReal(1, 100).toFixed(2), "0.01", 'one hundredth');
})

test('price increment', function() {
    var contr = {
        tickNumer: 1,
        tickDenom: 10000,
        priceDp: 4
    };
    equal(dbr.priceInc(contr), "0.0001", 'one ten thousandth');
});

test('quantity increment', function() {
    var contr = {
        lotNumer: 1000000,
        lotDenom: 1,
        lotDp: 1
    };
    equal(dbr.qtyInc(contr), 1000000, 'one million');
});

module('reference data');

asyncTest('get all contracts', function() {
    expect(1);
    $.ajax({
        type: 'get',
        url: '/api/rec/contr/'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[v.mnem] = v;
        });
        var v = dict['EURJPY'];
        equal(v !== undefined ? v.mnem : undefined, 'EURJPY', 'mnem');
    }).always(function() {
        start();
    });
});

asyncTest('get single contract', function() {
    expect(14);
    $.ajax({
        type: 'get',
        url: '/api/rec/contr/EURJPY'
    }).done(function(v) {
        equal(v.mnem, 'EURJPY', 'mnem');
        equal(v.display, 'EURJPY', 'display');
        equal(v.assetType, 'CURRENCY', 'assetType');
        equal(v.asset, 'EUR', 'asset');
        equal(v.ccy, 'JPY', 'ccy');
        equal(v.tickNumer, 1, 'tickNumer');
        equal(v.tickDenom, 100, 'tickDenom');
        equal(v.lotNumer, 1000000, 'lotNumer');
        equal(v.lotDenom, 1, 'lotDenom');
        equal(v.priceDp, 2, 'priceDp');
        equal(v.pipDp, 2, 'pipDp');
        equal(v.qtyDp, 0, 'qtyDp');
        equal(v.minLots, 1, 'minLots');
        equal(v.maxLots, 10, 'maxLots');
    }).always(function() {
        start();
    });
});
