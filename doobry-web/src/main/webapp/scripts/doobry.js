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
    dbr.realToIncs = function(real, incSize) {
        return Math.round(real / incSize);
    };
    dbr.incsToReal = function(incs, incSize) {
        return incs * incSize;
    };
    dbr.qtyToLots = function(qty, contr) {
        return dbr.realToIncs(qty, contr.qtyInc);
    };
    dbr.lotsToQty = function(lots, contr) {
        return dbr.incsToReal(lots, contr.qtyInc).toFixed(contr.qtyDp);
    };
    dbr.priceToTicks = function(price, contr) {
        return dbr.realToIncs(price, contr.priceInc);
    };
    dbr.ticksToPrice = function(ticks, contr) {
        return dbr.incsToReal(ticks, contr.priceInc).toFixed(contr.priceDp);
    };

    dbr.qtyInc = function(contr) {
        return dbr.fractToReal(contr.lotNumer, contr.lotDenom).toFixed(contr.qtyDp);
    };
    dbr.priceInc = function(contr) {
        return dbr.fractToReal(contr.tickNumer, contr.tickDenom).toFixed(contr.priceDp);
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
            v.priceInc = dbr.priceInc(v);
            v.qtyInc = dbr.qtyInc(v);
            dict[v.mnem] = v;
        });
        that.contrs = dict;
        maybeReady();
    });
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
            settlDate: settlDate,
            ref: '',
            action: action,
            ticks: ticks,
            lots: parseInt(lots),
            minLots: 0
        })
    }).done(function(v) {
        // TODO: display pending new.
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
    });
};

Model.prototype.cancelOrder = function(id) {
    var that = this;
    $.ajax({
        type: 'put',
        url: '/api/accnt/order/' + id,
        data: '{"lots":0}'
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

        var orderSource = {
            dataType: 'json',
            dataFields: [
                { name: 'id', type: 'int' },
                { name: 'user', type: 'string' },
                { name: 'contr', type: 'string' },
                { name: 'settlDate', type: 'int' },
                { name: 'ref', type: 'string' },
                { name: 'state', type: 'string' },
                { name: 'action', type: 'string' },
                { name: 'ticks', type: 'int' },
                { name: 'lots', type: 'int' },
                { name: 'resd', type: 'int' },
                { name: 'exec', type: 'int' },
                { name: 'lastTicks', type: 'int' },
                { name: 'lastLots', type: 'int' },
                { name: 'created', type: 'date', format: 'yyyy-MM-dd' },
                { name: 'modified', type: 'date', format: 'yyyy-MM-dd' }
            ],
            id: 'id',
            url: '/api/accnt/order'
        };
        var orderAdapter = new $.jqx.dataAdapter(orderSource, {
            beforeLoadComplete: function (rs) {
                for (var i = 0; i < rs.length; ++i) {
                    var r = rs[i];
                    var contr = model.contrs[r.contr];
                    r.price = dbr.ticksToPrice(r.ticks, contr);
                    r.lastPrice = dbr.ticksToPrice(r.lastTicks, contr);
                    r.created = new Date(r.created);
                    r.modified = new Date(r.modified);
                }
                return rs;
            }
        });
        var tradeSource = {
            dataType: 'json',
            dataFields: [
                { name: 'id', type: 'int' },
                { name: 'order', type: 'int' },
                { name: 'user', type: 'string' },
                { name: 'contr', type: 'string' },
                { name: 'settlDate', type: 'int' },
                { name: 'ref', type: 'string' },
                { name: 'state', type: 'string' },
                { name: 'action', type: 'string' },
                { name: 'ticks', type: 'int' },
                { name: 'lots', type: 'int' },
                { name: 'resd', type: 'int' },
                { name: 'exec', type: 'int' },
                { name: 'lastTicks', type: 'int' },
                { name: 'lastLots', type: 'int' },

                { name: 'match', type: 'int' },
                { name: 'role', type: 'string' },
                { name: 'cpty', type: 'string' },

                { name: 'created', type: 'date', format: 'yyyy-MM-dd' }
            ],
            id: 'id',
            url: '/api/accnt/trade'
        };
        var tradeAdapter = new $.jqx.dataAdapter(tradeSource, {
            beforeLoadComplete: function (rs) {
                for (var i = 0; i < rs.length; ++i) {
                    var r = rs[i];
                    var contr = model.contrs[r.contr];
                    r.price = dbr.ticksToPrice(r.ticks, contr);
                    r.lastPrice = dbr.ticksToPrice(r.lastTicks, contr);
                    r.created = new Date(r.created);
                    r.modified = new Date(r.modified);
                }
                return rs;
            }
        });
        var posnSource = {
            dataType: 'json',
            dataFields: [
                { name: 'id', type: 'int' },
                { name: 'user', type: 'string' },
                { name: 'contr', type: 'string' },
                { name: 'settlDate', type: 'int' },
                { name: 'buyLicks', type: 'int' },
                { name: 'buyLots', type: 'int' },
                { name: 'sellLicks', type: 'int' },
                { name: 'sellLots', type: 'int' }
            ],
            id: 'id',
            url: '/api/accnt/posn'
        };
        var posnAdapter = new $.jqx.dataAdapter(posnSource, {
            beforeLoadComplete: function (rs) {
                for (var i = 0; i < rs.length; ++i) {
                    var r = rs[i];
                    var contr = model.contrs[r.contr];
                    if (r.buyLots > 0) {
                        r.buyTicks = dbr.fractToReal(r.buyLicks, r.buyLots);
                    } else {
                        r.buyTicks = 0;
                    }
                    if (r.sellLots > 0) {
                        r.sellTicks = dbr.fractToReal(r.sellLicks, r.sellLots);
                    } else {
                        r.sellTicks = 0;
                    }
                    r.buyPrice = dbr.ticksToPrice(r.buyTicks, contr);
                    r.sellPrice = dbr.ticksToPrice(r.sellTicks, contr);
                }
                return rs;
            }
        });

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
            } else if (id === 'reviseOrder') {
            } else if (id === 'cancelOrder') {
                var rows = $("#orderTable").jqxDataTable('getSelection');
                for (var i = 0; i < rows.length; i++) {
	                var row = rows[i];
                    model.cancelOrder(row.id);
                }
            } else if (id === 'refresh') {
                var item = $('#tabs').jqxTabs('selectedItem');
                if (item === 0) {
                    $("#orderTable").jqxDataTable('updateBoundData');
                } else if (item === 1) {
                    $("#tradeTable").jqxDataTable('updateBoundData');
                } else if (item === 2) {
                    $("#posnTable").jqxDataTable('updateBoundData');
                }
            }
        });
        $('#tabs').jqxTabs({
            theme: theme,
            width: '90%',
            position: 'top'
        });
        $('#tabs').on('selected', function (event) {
            var item = event.args.item;
            if (item === 0) {
                $("#orderTable").jqxDataTable('updateBoundData');
            } else if (item === 1) {
                $("#tradeTable").jqxDataTable('updateBoundData');
            } else if (item === 2) {
                $("#posnTable").jqxDataTable('updateBoundData');
            }
        });
        $('#orderTable').jqxDataTable({
            theme: theme,
            columns: [
                { text: 'Id', dataField: 'id', width: 80 },
                { text: 'Contr', dataField: 'contr', width: 80 },
                { text: 'Settl Date', dataField: 'settlDate', width: 80 },
                { text: 'State', dataField: 'state', width: 80 },
                { text: 'Action', dataField: 'action', width: 80 },
                { text: 'Price', dataField: 'price', width: 80 },
                { text: 'Lots', dataField: 'lots', width: 80 },
                { text: 'Resd', dataField: 'resd', width: 80 },
                { text: 'Exec', dataField: 'exec', width: 80 },
                { text: 'Last Price', dataField: 'lastPrice', width: 80 },
                { text: 'Last Lots', dataField: 'lastLots', width: 80 }
            ],
            columnsResize: true,
            pageable: true,
            pagerButtonsCount: 10,
            source: orderAdapter
        });
        $('#tradeTable').jqxDataTable({
            theme: theme,
            columns: [
                { text: 'Id', dataField: 'id', width: 80 },
                { text: 'Order', dataField: 'order', width: 80 },
                { text: 'Contr', dataField: 'contr', width: 80 },
                { text: 'Settl Date', dataField: 'settlDate', width: 80 },
                { text: 'Action', dataField: 'action', width: 80 },
                { text: 'Price', dataField: 'price', width: 80 },
                { text: 'Lots', dataField: 'lots', width: 80 },
                { text: 'Resd', dataField: 'resd', width: 80 },
                { text: 'Exec', dataField: 'exec', width: 80 },
                { text: 'Last Price', dataField: 'lastPrice', width: 80 },
                { text: 'Last Lots', dataField: 'lastLots', width: 80 }
            ],
            columnsResize: true,
            pageable: true,
            pagerButtonsCount: 10,
            source: tradeAdapter
        });
        $('#posnTable').jqxDataTable({
            theme: theme,
            columns: [
                { text: 'Contr', dataField: 'contr', width: 80 },
                { text: 'Settl Date', dataField: 'settlDate', width: 80 },
                { text: 'Buy Price', dataField: 'buyPrice', width: 80 },
                { text: 'Buy Lots', dataField: 'buyLots', width: 80 },
                { text: 'Sell Price', dataField: 'sellPrice', width: 80 },
                { text: 'Sell Lots', dataField: 'sellLots', width: 80 }
            ],
            columnsResize: true,
            pageable: true,
            pagerButtonsCount: 10,
            source: posnAdapter
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
                'decimalDigits': contr.priceDp
            });
            $('#orderLots').jqxNumberInput({
                'min': contr.minLots,
                'max': contr.maxLots
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
