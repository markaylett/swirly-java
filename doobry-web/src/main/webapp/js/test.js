module('conversion');

test('fraction to real', function() {
    equal(dbr.fractToReal(100, 1).toFixed(2), "100.00", 'one hundred');
    equal(dbr.fractToReal(1, 100).toFixed(2), "0.01", 'one hundredth');
})

test('price increment', function() {
    var contr = {
        tick_numer: 1,
        tick_denom: 10000,
        price_dp: 4
    };
    equal(dbr.priceInc(contr), "0.0001", 'one ten thousandth');
});

test('quantity increment', function() {
    var contr = {
        lot_numer: 1000000,
        lot_denom: 1,
        lot_dp: 1
    };
    equal(dbr.qtyInc(contr), 1000000, 'one million');
});

module('reference data');

asyncTest('get all contracts', function() {
    expect(1);
    $.ajax({
        type: 'get',
        url: '/api/contr/'
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
        url: '/api/contr/EURJPY'
    }).done(function(v) {
        equal(v.mnem, 'EURJPY', 'mnem');
        equal(v.display, 'EURJPY', 'display');
        equal(v.asset_type, 'CURRENCY', 'asset_type');
        equal(v.asset, 'EUR', 'asset');
        equal(v.ccy, 'JPY', 'ccy');
        equal(v.tick_numer, 1, 'tick_numer');
        equal(v.tick_denom, 100, 'tick_denom');
        equal(v.lot_numer, 1000000, 'lot_numer');
        equal(v.lot_denom, 1, 'lot_denom');
        equal(v.price_dp, 2, 'price_dp');
        equal(v.pip_dp, 2, 'pip_dp');
        equal(v.qty_dp, 0, 'qty_dp');
        equal(v.min_lots, 1, 'min_lots');
        equal(v.max_lots, 10, 'max_lots');
    }).always(function() {
        start();
    });
});

asyncTest('get all accounts', function() {
    expect(1);
    $.ajax({
        type: 'get',
        url: '/api/accnt/'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[v.mnem] = v;
        });
        var v = dict['WRAMIREZ'];
        equal(v !== undefined ? v.mnem : undefined, 'WRAMIREZ', 'mnem');
    }).always(function() {
        start();
    });
});

asyncTest('get single accnt', function() {
    expect(3);
    $.ajax({
        type: 'get',
        url: '/api/accnt/WRAMIREZ'
    }).done(function(v) {
        equal(v.mnem, 'WRAMIREZ', 'mnem');
        equal(v.display, 'Wayne Ramirez', 'display');
        equal(v.email, 'wayne.ramirez@doobry.org', 'email');
    }).always(function() {
        start();
    });
});
