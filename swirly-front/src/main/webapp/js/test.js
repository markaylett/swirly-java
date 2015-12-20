/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var assets = [
    {"mnem":"AUD","display":"Australia, Dollars","type":"CURRENCY"},
    {"mnem":"CAD","display":"Canada, Dollars","type":"CURRENCY"},
    {"mnem":"CHF","display":"Switzerland, Francs","type":"CURRENCY"},
    {"mnem":"CZK","display":"Czech Republic, Koruny","type":"CURRENCY"},
    {"mnem":"DKK","display":"Denmark, Kroner","type":"CURRENCY"},
    {"mnem":"EUR","display":"Euro Member Countries, Euro","type":"CURRENCY"},
    {"mnem":"GBP","display":"United Kingdom, Pounds","type":"CURRENCY"},
    {"mnem":"HKD","display":"Hong Kong, Dollars","type":"CURRENCY"},
    {"mnem":"HUF","display":"Hungary, Forint","type":"CURRENCY"},
    {"mnem":"ILS","display":"Israel, New Shekels","type":"CURRENCY"},
    {"mnem":"JPY","display":"Japan, Yen","type":"CURRENCY"},
    {"mnem":"MXN","display":"Mexico, Pesos","type":"CURRENCY"},
    {"mnem":"NOK","display":"Norway, Krone","type":"CURRENCY"},
    {"mnem":"NZD","display":"New Zealand, Dollars","type":"CURRENCY"},
    {"mnem":"PLN","display":"Poland, Zlotych","type":"CURRENCY"},
    {"mnem":"RON","display":"Romania, New Lei","type":"CURRENCY"},
    {"mnem":"SEK","display":"Sweden, Kronor","type":"CURRENCY"},
    {"mnem":"SGD","display":"Singapore, Dollars","type":"CURRENCY"},
    {"mnem":"THB","display":"Thailand, Baht","type":"CURRENCY"},
    {"mnem":"TRY","display":"Turkey, New Lira","type":"CURRENCY"},
    {"mnem":"USD","display":"United States of America, Dollars","type":"CURRENCY"},
    {"mnem":"ZAR","display":"South Africa, Rand","type":"CURRENCY"}
];

var contrs = [
    {"mnem":"AUDUSD","display":"AUDUSD","asset":"AUD","ccy":"USD","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"EURCHF","display":"EURCHF","asset":"EUR","ccy":"CHF","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"EURCZK","display":"EURCZK","asset":"EUR","ccy":"CZK","lotNumer":1,"lotDenom":100,
     "tickNumer":1000000,"tickDenom":1,"pipDp":2,"minLots":1,"maxLots":10},
    {"mnem":"EURDKK","display":"EURDKK","asset":"EUR","ccy":"DKK","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"EURGBP","display":"EURGBP","asset":"EUR","ccy":"GBP","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"EURHUF","display":"EURHUF","asset":"EUR","ccy":"HUF","lotNumer":1,"lotDenom":100,
     "tickNumer":1000000,"tickDenom":1,"pipDp":2,"minLots":1,"maxLots":10},
    {"mnem":"EURJPY","display":"EURJPY","asset":"EUR","ccy":"JPY","lotNumer":1,"lotDenom":100,
     "tickNumer":1000000,"tickDenom":1,"pipDp":2,"minLots":1,"maxLots":10},
    {"mnem":"EURNOK","display":"EURNOK","asset":"EUR","ccy":"NOK","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"EURPLN","display":"EURPLN","asset":"EUR","ccy":"PLN","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"EURRON","display":"EURRON","asset":"EUR","ccy":"RON","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"EURSEK","display":"EURSEK","asset":"EUR","ccy":"SEK","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"EURUSD","display":"EURUSD","asset":"EUR","ccy":"USD","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"GBPUSD","display":"GBPUSD","asset":"GBP","ccy":"USD","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"NZDUSD","display":"NZDUSD","asset":"NZD","ccy":"USD","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"USDCAD","display":"USDCAD","asset":"USD","ccy":"CAD","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"USDCHF","display":"USDCHF","asset":"USD","ccy":"CHF","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"USDHKD","display":"USDHKD","asset":"USD","ccy":"HKD","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"USDILS","display":"USDILS","asset":"USD","ccy":"ILS","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"USDJPY","display":"USDJPY","asset":"USD","ccy":"JPY","lotNumer":1,"lotDenom":100,
     "tickNumer":1000000,"tickDenom":1,"pipDp":2,"minLots":1,"maxLots":10},
    {"mnem":"USDMXN","display":"USDMXN","asset":"USD","ccy":"MXN","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10},
    {"mnem":"USDSGD","display":"USDSGD","asset":"USD","ccy":"SGD","lotNumer":1,"lotDenom":10000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"USDTHB","display":"USDTHB","asset":"USD","ccy":"THB","lotNumer":1,"lotDenom":100,
     "tickNumer":1000000,"tickDenom":1,"pipDp":2,"minLots":1,"maxLots":10},
    {"mnem":"USDTRY","display":"USDTRY","asset":"USD","ccy":"TRY","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":4,"minLots":1,"maxLots":10},
    {"mnem":"USDZAR","display":"USDZAR","asset":"USD","ccy":"ZAR","lotNumer":1,"lotDenom":1000,
     "tickNumer":1000000,"tickDenom":1,"pipDp":3,"minLots":1,"maxLots":10}
];

var markets = [
    {"mnem":"EURUSD","display":"EURUSD","contr":"EURUSD","settlDate":null,"expiryDate":null,
     "state":0},
    {"mnem":"GBPUSD","display":"GBPUSD","contr":"GBPUSD","settlDate":null,"expiryDate":null,
     "state":0},
    {"mnem":"USDCHF","display":"USDCHF","contr":"USDCHF","settlDate":null,"expiryDate":null,
     "state":0},
    {"mnem":"USDJPY","display":"USDJPY","contr":"USDJPY","settlDate":null,"expiryDate":null,
     "state":0}
];

var traders = [
    {"mnem":"MARAYL","display":"Mark Aylett","email":"mark.aylett@gmail.com"}
];

function toMap(arr) {
    obj = {};
    $.each(arr, function(k, v) {
        obj[v.mnem] = v;
    });
    return obj;
}

var assetMap = toMap(assets);
var contrMap = toMap(contrs);
var marketMap = toMap(markets);
var traderMap = toMap(traders);

function cancelAndArchive(market, id) {
    $.ajax({
        type: 'put',
        url: '/back/sess/order/' + market + '/' + id,
        data: '{"lots":0}'
    }).done(function() {
        $.ajax({
            type: 'delete',
            url: '/back/sess/order/' + market + '/' + id
        });
    });
}

// Rec
// Asset

module('Asset');

$.each(['back', 'front'], function(index, module) {

    asyncTest('GET /' + module + '/rec/asset/', function() {
        expect(assets.length);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/asset/'
        }).done(function(arr) {
            var m = toMap(arr);
            $.each(assets, function(k, v) {
                deepEqual(v, m[v.mnem], v.mnem);
            });
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/asset/EUR', function() {
        expect(1);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/asset/EUR'
        }).done(function(v) {
            deepEqual(v, assetMap['EUR'], 'EUR');
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/asset/x', function() {
        expect(2);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/asset/x'
        }).fail(function(xhr) {
            var err = $.parseJSON(xhr.responseText);
            equal(err.num, 404, 'num');
            equal(err.msg, "record 'x' does not exist", 'msg');
        }).always(function() {
            start();
        });
    });
});

// Contr

module('Contr');

$.each(['back', 'front'], function(index, module) {

    asyncTest('GET /' + module + '/rec/contr/', function() {
        expect(contrs.length);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/contr/'
        }).done(function(arr) {
            var m = toMap(arr);
            $.each(contrs, function(k, v) {
                deepEqual(v, m[v.mnem], v.mnem);
            });
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/contr/EURUSD', function() {
        expect(1);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/contr/EURUSD'
        }).done(function(v) {
            deepEqual(v, contrMap['EURUSD'], 'EURUSD');
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/contr/x', function() {
        expect(2);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/contr/x'
        }).fail(function(xhr) {
            var err = $.parseJSON(xhr.responseText);
            equal(err.num, 404, 'num');
            equal(err.msg, "record 'x' does not exist", 'msg');
        }).always(function() {
            start();
        });
    });
});

// Market

module('Market');

$.each(['back', 'front'], function(index, module) {

    asyncTest('GET /' + module + '/rec/market/', function() {
        expect(markets.length);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/market/'
        }).done(function(arr) {
            var m = toMap(arr);
            $.each(markets, function(k, v) {
                deepEqual(v, m[v.mnem], v.mnem);
            });
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/market/EURUSD', function() {
        expect(1);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/market/EURUSD'
        }).done(function(v) {
            deepEqual(v, marketMap['EURUSD'], 'EURUSD');
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/market/x', function() {
        expect(2);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/market/x'
        }).fail(function(xhr) {
            var err = $.parseJSON(xhr.responseText);
            equal(err.num, 404, 'num');
            equal(err.msg, "record 'x' does not exist", 'msg');
        }).always(function() {
            start();
        });
    });
});

// Trader

module('Trader');

$.each(['back', 'front'], function(index, module) {

    asyncTest('GET /' + module + '/rec/trader/', function() {
        expect(traders.length);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/trader/'
        }).done(function(arr) {
            var m = toMap(arr);
            $.each(traders, function(k, v) {
                deepEqual(v, m[v.mnem], v.mnem);
            });
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/trader/MARAYL', function() {
        expect(1);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/trader/MARAYL'
        }).done(function(v) {
            deepEqual(v, traderMap['MARAYL'], 'MARAYL');
        }).always(function() {
            start();
        });
    });

    asyncTest('GET /' + module + '/rec/trader/x', function() {
        expect(2);
        $.ajax({
            type: 'get',
            url: '/' + module + '/rec/trader/x'
        }).fail(function(xhr) {
            var err = $.parseJSON(xhr.responseText);
            equal(err.num, 404, 'num');
            equal(err.msg, "record 'x' does not exist", 'msg');
        }).always(function() {
            start();
        });
    });
});

// Order

module('Order');

asyncTest('POST /back/sess/order/EURUSD', function() {
    expect(5);
    var req = {"side":"BUY","lots":1,"ticks":12345}
    var res = {
        "view":{"market":"EURUSD","contr":"EURUSD","settlDate":null,
                "lastLots":null,"lastTicks":null,"lastTime":null,
                "bidTicks":[12345,null,null],
                "bidResd":[1,null,null],
                "bidQuot":[0,null,null],
                "bidCount":[1,null,null],
                "offerTicks":[null,null,null],
                "offerResd":[null,null,null],
                "offerQuot":[null,null,null],
                "offerCount":[null,null,null]},
        "orders":[{"trader":"MARAYL","market":"EURUSD","contr":"EURUSD",
                   "settlDate":null,"ref":null,"state":"NEW","side":"BUY","lots":1,"ticks":12345,
                   "resd":1,"exec":0,"cost":0,"lastLots":null,"lastTicks":null,"minLots":0,
                   "pecan":false}],
        "execs":[{"trader":"MARAYL","market":"EURUSD","contr":"EURUSD",
                  "settlDate":null,"ref":null,"state":"NEW","side":"BUY","lots":1,"ticks":12345,
                  "resd":1,"exec":0,"cost":0,"lastLots":null,"lastTicks":null,"minLots":0,
                  "matchId":null,"role":null,"cpty":null}],
        "posn":null
    };
    $.ajax({
        type: 'post',
        url: '/back/sess/order/EURUSD',
        data: JSON.stringify(req)
    }).done(function(trans) {

        var newOrder = trans.orders[0];
        var newExec = trans.execs[0];

        var id = newOrder.id;
        equal(newExec.orderId, id, 'orderId');

        delete newOrder.id;
        delete newOrder.created;
        delete newOrder.modified;

        delete newExec.id;
        delete newExec.orderId;
        delete newExec.created;

        deepEqual(trans.view, res.view, 'view');
        deepEqual(trans.orders, res.orders, 'orders');
        deepEqual(trans.execs, res.execs, 'execs');
        deepEqual(trans.posn, res.posn, 'posn');

        cancelAndArchive('EURUSD', id);
    }).always(function() {
        start();
    });
});

// View

// Sess
// Trade
// Posn
