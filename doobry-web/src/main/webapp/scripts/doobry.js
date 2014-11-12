/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 * 
 * All rights reserved.
 **************************************************************************************************/
(function() {

    // Window in browser.
    var root = this;

    var dbr = Object.create(null);

    dbr.fractToReal = function(numer, denom) {
        return numer / denom;
    };
    dbr.realToIncs = function(real, inc_size) {
        return Math.round(real / inc_size);
    };
    dbr.incsToReal = function(incs, inc_size) {
        return incs * inc_size;
    };
    dbr.qtyToLots = function(qty, contr) {
        return dbr.realToIncs(qty, contr.qty_inc);
    };
    dbr.lotsToQty = function(lots, contr) {
        return dbr.incsToReal(lots, contr.qty_inc).toFixed(contr.qty_dp);
    };
    dbr.priceToTicks = function(price, contr) {
        return dbr.realToIncs(price, contr.price_inc);
    };
    dbr.ticksToPrice = function(ticks, contr) {
        return dbr.incsToReal(ticks, contr.price_inc).toFixed(contr.price_dp);
    };

    dbr.qtyInc = function(contr) {
        return dbr.fractToReal(contr.lot_numer, contr.lot_denom).toFixed(contr.qty_dp);
    };
    dbr.priceInc = function(contr) {
        return dbr.fractToReal(contr.tick_numer, contr.tick_denom).toFixed(contr.price_dp);
    };

    dbr.eachKey = function(arr, fn) {
        for (var k in arr) {
            fn(k);
        }
    };
    dbr.eachValue = function(arr, fn) {
        for (var k in arr) {
            fn(arr[k]);
        }
    };
    dbr.eachPair = function(arr, fn) {
        for (var k in arr) {
            fn(k, arr[k]);
        }
    };
    dbr.mapValue = function(arr, fn) {
        var out = [];
        for (var k in arr) {
            out.push(fn(arr[k]));
        }
        return out;
    }
    root.dbr = dbr;
}).call(this);

function Model(ready) {

    var that = this;

    this.contrs = undefined;

    var maybeReady = function() {

        if (that.contrs === undefined)
            return;

        console.log('ready');
        ready(that);

        // setInterval(function() {
        // that.refresh();
        // }, 10000);
    };

    $.ajax({
        type: 'get',
        url: '/api/rec/contr'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            v.price_inc = dbr.priceInc(v);
            v.qty_inc = dbr.qtyInc(v);
            dict[v.mnem] = v;
        });
        that.contrs = dict;
        maybeReady();
    });
}

Model.prototype.refresh = function() {
}

Model.prototype.submitOrder = function(contr, settlDate, action, price, lots) {
    var that = this;
    contr = this.contrs[contr];
    settlDate = parseInt(settlDate);
    var ticks = dbr.priceToTicks(price, contr);
    $.ajax({
        type: 'post',
        url: '/api/accnt/order/',
        data: JSON.stringify({
            contr: contr.mnem,
            settl_date: settlDate,
            ref: '',
            action: action,
            ticks: ticks,
            lots: parseInt(lots),
            min_lots: 0
        })
    }).done(function(v) {
        // TODO: display pending new.
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
    });
};

var model = null;

// Lifecycle phases.

function documentReady() {

    model = new Model(function(model) {

        var url = '/api/rec/contr';
        var source = {
            dataType: 'json',
            dataFields: [
                { name: 'mnem', type: 'string' },
                { name: 'display', type: 'string' }
            ],
            id: 'mnem',
            url: url
        };

        var dataAdapter = new $.jqx.dataAdapter(source);

        var theme = 'energyblue';

        $("#toolbar").jqxButtonGroup({
            theme: theme,
            mode: 'default'
        });
        $("#toolbar").on('buttonclick', function (event) {
            var button = event.args.button;
            var id = button[0].id;
            if (id === 'newOrder') {
                $('#orderDialog').jqxWindow('open');
            } else if (id === 'refresh') {
                model.refresh();
            }
        });
        $('#tabs').jqxTabs({
            theme: theme,
            width: '90%',
            position: 'top'
        });
        $('#contrTab').jqxDataTable({
            theme: theme,
            columns: [
                { text: 'Mnem', dataField: 'mnem' },
                { text: 'Display', dataField: 'display' }
            ],
            columnsResize: true,
            pageable: true,
            pagerButtonsCount: 10,
            source: dataAdapter
        });
        $('#orderContr').jqxInput({
            theme: theme,
            width: 160,
            height: 27,
            source: Object.keys(model.contrs)
        });
        $('#orderContr').on('select', function () {
            var contr = model.contrs[$('#orderContr').val()];
            $('#orderPrice').jqxNumberInput({
                'decimalDigits': contr.price_dp
            });
            $('#orderLots').jqxNumberInput({
                'min': contr.min_lots,
                'max': contr.max_lots
            });
        });
        $('#orderSettlDate').jqxDateTimeInput({
            theme: theme,
            width: 160,
            height: 27,
            formatString: 'yyyyMMdd'
        });
        $('#orderAction').jqxSwitchButton({
            theme: theme,
            height: 27,
            checked: true,
            onLabel: 'BUY',
            offLabel: 'SELL'
        });
        $("#orderPrice").jqxNumberInput({
            theme: theme,
            width: 160,
            height: 27,
            decimalDigits: 4,
            inputMode: 'simple',
            spinButtons: true
        });
        $('#orderLots').jqxNumberInput({
            theme: theme,
            width: 160,
            height: 27,
            decimalDigits: 0,
            inputMode: 'simple',
            spinButtons: true
        });
        $('#submitOrder').jqxButton({
            theme: theme,
            width: 80,
            height: 27
        });
        $("#submitOrder").on('click', function () {
            $("#orderDialog").jqxWindow('close');
            var contr = $('#orderContr').val();
            var settlDate = $('#orderSettlDate').val();
            var action = $('#orderAction').val() ? 'BUY' : 'SELL';
            var price = $('#orderPrice').val();
            var lots = $('#orderLots').val();
            model.submitOrder(contr, settlDate, action, price, lots);
        });
        $('#closeOrder').jqxButton({
            theme: theme,
            width: 80,
            height: 27
        });
        $("#closeOrder").on('click', function () {
            $("#orderDialog").jqxWindow('close');
        });
        $('#orderDialog').jqxWindow({
            theme: theme,
            width: 320,
            autoOpen: false,
            resizable: false
        });
        $("#orderDialog").css('visibility', 'visible');
    });
}
