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
    dbr.Model = function (fn) {

        var that = this;

        this.fn = fn;
        this.asset = undefined;
        this.contr = undefined;
        this.book = undefined;
        this.order = undefined;
        this.trade = undefined;
        this.posn = undefined;

        var maybeReady = function() {

            if (that.asset === undefined
                || that.contr === undefined
                || that.book === undefined
                || that.order === undefined
                || that.trade === undefined
                || that.posn === undefined)
                return;

            dbr.eachValue(that.asset, that.enrichAsset.bind(that));
            dbr.eachValue(that.contr, that.enrichContr.bind(that));
            dbr.eachValue(that.book, that.enrichBook.bind(that));
            dbr.eachValue(that.order, that.enrichOrder.bind(that));
            dbr.eachValue(that.trade, that.enrichTrade.bind(that));
            dbr.eachValue(that.posn, that.enrichPosn.bind(that));

            console.log('ready');
            fn.ready(that);

            // setInterval(function() {
            // that.refresh();
            // }, 10000);
        };

        $.ajax({
            type: 'get',
            url: '/api/rec'
        }).done(function(arr) {
            var dict = [];
            $.each(arr.asset, function(k, v) {
                dict[v.mnem] = v;
            });
            that.asset = dict;
            dict = [];
            $.each(arr.contr, function(k, v) {
                dict[v.mnem] = v;
            });
            that.contr = dict;
            maybeReady();
        });

        $.ajax({
            type: 'get',
            url: '/api/book'
        }).done(function(arr) {
            that.book = arr;
            maybeReady();
        });

        $.ajax({
            type: 'get',
            url: '/api/accnt'
        }).done(function(arr) {
            that.order = arr.order;
            that.trade = arr.trade;
            that.posn = arr.posn;
            maybeReady();
        });
    };
    root.dbr = dbr;
}).call(this);

dbr.Model.prototype.enrichAsset = function(v) {
};

dbr.Model.prototype.enrichContr = function(v) {
    v.priceInc = dbr.priceInc(v);
    v.qtyInc = dbr.qtyInc(v);
};

dbr.Model.prototype.enrichBook = function(v) {
    var contr = this.contr[v.contr];
    v.bidPrice = dbr.mapValue(v.bidTicks, function(w) {
        return dbr.ticksToPrice(w, contr);
    });
    v.offerPrice = dbr.mapValue(v.offerTicks, function(w) {
        return dbr.ticksToPrice(w, contr);
    });
    v.bidPrice0 = v.bidPrice[0];
    v.bidTicks0 = v.bidTicks[0];
    v.bidLots0 = v.bidLots[0];
    v.bidCount0 = v.bidCount[0];
    v.offerPrice0 = v.offerPrice[0];
    v.offerTicks0 = v.offerTicks[0];
    v.offerLots0 = v.offerLots[0];
    v.offerCount0 = v.offerCount[0];
};

dbr.Model.prototype.enrichOrder = function(v) {
    var contr = this.contr[v.contr];
    v.price = dbr.ticksToPrice(v.ticks, contr);
    v.lastPrice = dbr.ticksToPrice(v.lastTicks, contr);
    v.created = new Date(v.created);
    v.modified = new Date(v.modified);
};

dbr.Model.prototype.enrichTrade = function(v) {
    var contr = this.contr[v.contr];
    v.price = dbr.ticksToPrice(v.ticks, contr);
    v.lastPrice = dbr.ticksToPrice(v.lastTicks, contr);
    v.created = new Date(v.created);
    v.modified = new Date(v.modified);
};

dbr.Model.prototype.enrichPosn = function(v) {
    var contr = this.contr[v.contr];
    if (v.buyLots > 0) {
        v.buyTicks = dbr.fractToReal(v.buyLicks, v.buyLots);
    } else {
        v.buyTicks = 0;
    }
    if (v.sellLots > 0) {
        v.sellTicks = dbr.fractToReal(v.sellLicks, v.sellLots);
    } else {
        v.sellTicks = 0;
    }
    v.buyPrice = dbr.ticksToPrice(v.buyTicks, contr);
    v.sellPrice = dbr.ticksToPrice(v.sellTicks, contr);
};

dbr.Model.prototype.refresh = function() {
    var that = this;
    $.ajax({
        type: 'get',
        url: '/api/book'
    }).done(function(arr) {
        dbr.eachValue(arr, that.enrichBook.bind(that));
        that.book = arr;
        that.fn.refreshBook();
    });
    $.ajax({
        type: 'get',
        url: '/api/accnt'
    }).done(function(arr) {
        dbr.eachValue(arr.order, that.enrichOrder.bind(that));
        dbr.eachValue(arr.trade, that.enrichTrade.bind(that));
        dbr.eachValue(arr.posn, that.enrichPosn.bind(that));
        that.order = arr.order;
        that.trade = arr.trade;
        that.posn = arr.posn;
        that.fn.refreshAccnt();
    });
}

dbr.Model.prototype.submitOrder = function(contr, settlDate, action, price, lots, fn) {
    var that = this;
    contr = this.contr[contr];
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
        var order = v.order[0];
        var contr = model.contr[order.contr];
        order.price = dbr.ticksToPrice(order.ticks, contr);
        order.lastPrice = dbr.ticksToPrice(order.lastTicks, contr);
        order.created = new Date(order.created);
        order.modified = new Date(order.modified);
        fn(order);
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
    });
};

dbr.Model.prototype.cancelOrder = function(fn, data) {
    var that = this;
    $.ajax({
        type: 'put',
        url: '/api/accnt/order/' + data.id,
        data: '{"lots":0}'
    }).done(function(v) {
        var order = v.order[0];
        var contr = model.contr[order.contr];
        order.price = dbr.ticksToPrice(order.ticks, contr);
        order.lastPrice = dbr.ticksToPrice(order.lastTicks, contr);
        order.created = new Date(order.created);
        order.modified = new Date(order.modified);
        fn(data, order);
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
    });
};

dbr.Model.prototype.confirmTrade = function(fn, data) {
    var that = this;
    $.ajax({
        type: 'delete',
        url: '/api/accnt/trade/' + data.id
    }).done(function(v) {
        fn(data);
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
    });
};

// Lifecycle phases.

function documentReady() {

    var bookSource = {
        dataType: 'array',
        dataFields: [
            { name: 'id', type: 'int' },
            { name: 'contr', type: 'string' },
            { name: 'settlDate', type: 'int' },
            { name: 'bidPrice0', type: 'string' },
            { name: 'bidLots0', type: 'int' },
            { name: 'bidCount0', type: 'int' },
            { name: 'offerPrice0', type: 'string' },
            { name: 'offerLots0', type: 'int' },
            { name: 'offerCount0', type: 'int' }
        ],
        id: 'id',
        localData: []
    };
    var bookAdapter = new $.jqx.dataAdapter(bookSource);
    var orderSource = {
        dataType: 'array',
        dataFields: [
            { name: 'id', type: 'int' },
            { name: 'user', type: 'string' },
            { name: 'contr', type: 'string' },
            { name: 'settlDate', type: 'int' },
            { name: 'ref', type: 'string' },
            { name: 'state', type: 'string' },
            { name: 'action', type: 'string' },
            { name: 'price', type: 'string' },
            { name: 'lots', type: 'int' },
            { name: 'resd', type: 'int' },
            { name: 'exec', type: 'int' },
            { name: 'lastPrice', type: 'string' },
            { name: 'lastLots', type: 'int' },
            { name: 'minLots', type: 'int' },
            { name: 'created', type: 'date', format: 'yyyy-MM-dd' },
            { name: 'modified', type: 'date', format: 'yyyy-MM-dd' }
        ],
        id: 'id',
        localData: []
    };
    var orderAdapter = new $.jqx.dataAdapter(orderSource);
    var tradeSource = {
        dataType: 'array',
        dataFields: [
            { name: 'id', type: 'int' },
            { name: 'orderId', type: 'int' },
            { name: 'user', type: 'string' },
            { name: 'contr', type: 'string' },
            { name: 'settlDate', type: 'int' },
            { name: 'ref', type: 'string' },
            { name: 'state', type: 'string' },
            { name: 'action', type: 'string' },
            { name: 'price', type: 'string' },
            { name: 'lots', type: 'int' },
            { name: 'resd', type: 'int' },
            { name: 'exec', type: 'int' },
            { name: 'lastPrice', type: 'string' },
            { name: 'lastLots', type: 'int' },
            { name: 'minLots', type: 'int' },

            { name: 'matchId', type: 'int' },
            { name: 'role', type: 'string' },
            { name: 'cpty', type: 'string' },

            { name: 'created', type: 'date', format: 'yyyy-MM-dd' }
        ],
        id: 'id',
        localData: []
    };
    var tradeAdapter = new $.jqx.dataAdapter(tradeSource);
    var posnSource = {
        dataType: 'array',
        dataFields: [
            { name: 'id', type: 'int' },
            { name: 'user', type: 'string' },
            { name: 'contr', type: 'string' },
            { name: 'settlDate', type: 'int' },
            { name: 'buyPrice', type: 'string' },
            { name: 'buyLots', type: 'int' },
            { name: 'sellPrice', type: 'string' },
            { name: 'sellLots', type: 'int' }
        ],
        id: 'id',
        localData: []
    };
    var posnAdapter = new $.jqx.dataAdapter(posnSource);

    var modelReady = function(model) {

        var theme = 'energyblue';

        $('#refresh').jqxButton({
            theme: theme,
            height: 27,
            template: 'primary'
        });
        $('#refresh').on('click', function () {
            model.refresh();
        });
        $('#newOrder').jqxButton({
            theme: theme,
            height: 27,
            template: 'primary'
        });
        $('#newOrder').on('click', function () {
            $('#orderContr').val('EURUSD');
            $('#orderPrice').val(1.2345);
            $('#orderLots').val(10);

            $('#orderDialog').jqxWindow('open');
        });
        $('#reviseOrder').jqxButton({
            theme: theme,
            height: 27,
            template: 'primary'
        });
        $('#reviseOrder').on('click', function () {
        });
        $('#cancelOrder').jqxButton({
            theme: theme,
            height: 27,
            template: 'primary'
        });
        $('#cancelOrder').on('click', function () {
            var selection = $('#orderTable').jqxDataTable('getSelection');
            for (var i = 0; i < selection.length; ++i) {
                var data = selection[i];
                model.cancelOrder(function(data, v) {
                    var rows = $('#orderTable').jqxDataTable('getRows');
                    var index = rows.indexOf(data);
                    $("#orderTable").jqxDataTable('updateRow', index, v);
                }, data);
            }
        });
        $('#confirmTrade').jqxButton({
            theme: theme,
            height: 27,
            template: 'primary'
        });
        $('#confirmTrade').on('click', function () {
            var selection = $('#tradeTable').jqxDataTable('getSelection');
            for (var i = 0; i < selection.length; ++i) {
                var data = selection[i];
                model.confirmTrade(function(data) {
                    var rows = $('#tradeTable').jqxDataTable('getRows');
                    var index = rows.indexOf(data);
                    $("#tradeTable").jqxDataTable('deleteRow', index);
                }, data);
            }
        });
        $('#bookTable').jqxDataTable({
            theme: theme,
            columns: [
                { text: 'Contr', dataField: 'contr', width: 80 },
                { text: 'Settl Date', dataField: 'settlDate', width: 80 },
                { text: 'Bid Price', dataField: 'bidPrice0', width: 80 },
                { text: 'Bid Lots', dataField: 'bidLots0', width: 80 },
                { text: 'Bid Count', dataField: 'bidCount0', width: 80 },
                { text: 'Offer Price', dataField: 'offerPrice0', width: 80 },
                { text: 'Offer Lots', dataField: 'offerLots0', width: 80 },
                { text: 'Offer Count', dataField: 'offerCount0', width: 80 }
            ],
            columnsResize: true,
            pageable: true,
            pagerButtonsCount: 10,
            source: bookAdapter
        });
        bookSource.localdata = model.book;
        bookAdapter.dataBind();
        $('#tabs').jqxTabs({
            theme: theme,
            position: 'top'
        });
        $('#tabs').on('selected', function (event) {
            var item = event.args.item;
            if (item === 0) {
                orderSource.localdata = model.order;
                orderAdapter.dataBind();
            } else if (item === 1) {
                tradeSource.localdata = model.trade;
                tradeAdapter.dataBind();
            } else if (item === 2) {
                posnSource.localdata = model.posn;
                posnAdapter.dataBind();
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
        orderSource.localdata = model.order;
        orderAdapter.dataBind();
        $('#tradeTable').jqxDataTable({
            theme: theme,
            columns: [
                { text: 'Id', dataField: 'id', width: 80 },
                { text: 'Order', dataField: 'orderId', width: 80 },
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
        tradeSource.localdata = model.trade;
        tradeAdapter.dataBind();
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
        posnSource.localdata = model.posn;
        posnAdapter.dataBind();
        $('#orderContr').jqxInput({
            theme: theme,
            width: 160,
            height: 27,
            source: Object.keys(model.contr)
        });
        $('#orderContr').on('select', function () {
            var contr = model.contr[$('#orderContr').val()];
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
        $('#orderPrice').jqxNumberInput({
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
        $('#submitOrder').on('click', function () {
            $('#orderDialog').jqxWindow('close');
            var contr = $('#orderContr').val();
            var settlDate = $('#orderSettlDate').val();
            var action = $('#orderAction').val() ? 'BUY' : 'SELL';
            var price = $('#orderPrice').val();
            var lots = $('#orderLots').val();
            model.submitOrder(contr, settlDate, action, price, lots, function(v) {
                if (v.resd > 0)
                    $("#orderTable").jqxDataTable('addRow', v.id, v);
            });
        });
        $('#closeOrder').jqxButton({
            theme: theme,
            width: 80,
            height: 27
        });
        $('#closeOrder').on('click', function () {
            $('#orderDialog').jqxWindow('close');
        });
        $('#orderDialog').jqxWindow({
            theme: theme,
            width: 320,
            autoOpen: false,
            resizable: false
        });
        $('#orderDialog').css('visibility', 'visible');
    };

    model = new dbr.Model({
        ready: modelReady,
        refreshBook: function() {
            bookSource.localdata = model.book;
            bookAdapter.dataBind();
        },
        refreshAccnt: function() {
            var item = $('#tabs').jqxTabs('selectedItem');
            if (item === 0) {
                orderSource.localdata = model.order;
                orderAdapter.dataBind();
            } else if (item === 1) {
                tradeSource.localdata = model.trade;
                tradeAdapter.dataBind();
            } else if (item === 2) {
                posnSource.localdata = model.posn;
                posnAdapter.dataBind();
            }
        }
    });
}
