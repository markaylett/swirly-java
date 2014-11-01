(function() {

    // Window in browser.
    var root = this;

    var dbr = Object.create(null);

    dbr.getExpires = function() {
        var expires = new Date();
        expires.setDate(expires.getDate() + 1);
        return expires;
    };
    dbr.setCookie = function(name, value) {
        var value = escape(value) + '; expires=' + dbr.getExpires().toUTCString();
        document.cookie = name + '=' + value;
    };
    // http://www.w3schools.com/js/js_cookies.asp
    dbr.getCookie = function(name) {
        var value = document.cookie;
        var start = value.indexOf(' ' + name + '=');
        if (start == -1) {
            start = value.indexOf(name + '=');
        }
        if (start == -1) {
            value = null;
        } else {
            start = value.indexOf('=', start) + 1;
            var end = value.indexOf(';', start);
            if (end == -1) {
                end = value.length;
            }
            value = unescape(value.substring(start, end));
        }
        return value;
    };
    dbr.setViewFilter = function(filter) {
        dbr.setCookie('views', filter.join(','));
    }
    dbr.getViewFilter = function() {
        var cookie = dbr.getCookie('views');
        return cookie ? cookie.split(',') : [];
    }
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
    dbr.createRow = function() {
        var tr = document.createElement('tr');

        var cb = document.createElement('input');
        cb.type = 'checkbox';
        var td = document.createElement('td');
        td.appendChild(cb);
        tr.appendChild(td);

        for (var i = 0; i < arguments.length; ++i) {
            var td = document.createElement('td');
            td.innerText = arguments[i];
            tr.appendChild(td);
        }
        return tr;
    };
    dbr.appendField = function(elem, name, value) {
        var fname = document.createElement('span');
        fname.className = 'dbr-field-name';
        fname.innerText = name;

        var fvalue = document.createElement('span');
        fvalue.className = 'dbr-field-value';
        fvalue.innerText = value;

        elem.appendChild(fname);
        elem.appendChild(fvalue);
        elem.appendChild(document.createElement('br'));
    }
    root.dbr = dbr;
}).call(this);

function Model(trader, giveup, pass, ready) {

    var that = this;

    this.trader = trader;
    this.giveup = giveup;
    this.filter = [];

    this.accnts = undefined;
    this.contrs = undefined;

    this.orders = undefined;
    this.trades = undefined;
    this.posns = undefined;

    this.views = undefined;

    var auth = 'Basic ' + btoa(trader + ':' + pass);
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', auth);
        }
    });

    var enrich = function() {
        if (that.accnts === undefined
            || that.contrs === undefined
            || that.views === undefined)
            return;

        dbr.eachValue(that.accnts, that.enrichAccnt.bind(that));
        dbr.eachValue(that.contrs, that.enrichContr.bind(that));
        dbr.eachValue(that.views, that.enrichView.bind(that));

        var filter = dbr.getViewFilter();
        console.log('view filter is ' + filter);
        dbr.eachValue(filter, function(k) {
            if (k in model.contrs) {
                console.log('set filter for ' + k);
                that.filter[k] = true;
            }
        });

        $('#view-tbody').replaceWith(that.createViews());
        $('#view-tbody button').button({
            icons: {
                primary: 'ui-icon-trash'
            }
        });

        console.log('ready');
        ready(that);

        setInterval(function() {
            that.refresh();
        }, 2000);
    };

    $.ajax({
        type: 'get',
        url: '/api/accnt'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[v.mnem] = v;
        });
        that.accnts = dict;
        enrich();
    });

    $.ajax({
        type: 'get',
        url: '/api/contr'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[v.mnem] = v;
        });
        that.contrs = dict;
        enrich();
    });

    $.ajax({
        type: 'get',
        url: '/api/view'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[[v.contr, v.settl_date]] = v;
        });
        that.views = dict;
        enrich();
    });
}

Model.prototype.subscribe = function(k) {
    console.log('subscribe: ' + k);
    if (k in this.contrs) {
        this.filter[k] = true;
        dbr.setViewFilter(Object.keys(this.filter));
        $('#view-tbody').replaceWith(this.createViews());
        $('#view-tbody button').button({
            icons: {
                primary: 'ui-icon-trash'
            }
        });
    }
}

Model.prototype.unsubscribe = function(k) {
    console.log('unsubscribe: ' + k);
    if (k in this.filter) {
        delete this.filter[k];
        dbr.setViewFilter(Object.keys(this.filter));
        $('#view-tbody').replaceWith(this.createViews());
        $('#view-tbody button').button({
            icons: {
                primary: 'ui-icon-trash'
            }
        });
    }
}

Model.prototype.enrichAccnt = function(v) {
};

Model.prototype.enrichContr = function(v) {
    v.price_inc = dbr.priceInc(v);
    v.qty_inc = dbr.qtyInc(v);
};

Model.prototype.enrichOrder = function(v) {
    v.contr = this.contrs[v.contr];
    v.price = dbr.ticksToPrice(v.ticks, v.contr);
    v.last_price = dbr.ticksToPrice(v.last_ticks, v.contr);
    v.created = new Date(v.created);
    v.modified = new Date(v.modified);
};

Model.prototype.enrichTrade = function(v) {
    v.contr = this.contrs[v.contr];
    v.price = dbr.ticksToPrice(v.ticks, v.contr);
    v.last_price = dbr.ticksToPrice(v.last_ticks, v.contr);
    v.created = new Date(v.created);
    v.modified = new Date(v.modified);
};

Model.prototype.enrichPosn = function(v) {
    v.contr = this.contrs[v.contr];
    if (v.buy_lots > 0) {
        v.buy_ticks = dbr.fractToReal(v.buy_licks, v.buy_lots);
    } else {
        v.buy_ticks = 0;
    }
    if (v.sell_lots > 0) {
        v.sell_ticks = dbr.fractToReal(v.sell_licks, v.sell_lots);
    } else {
        v.sell_ticks = 0;
    }
    v.buy_price = dbr.ticksToPrice(v.buy_ticks, v.contr);
    v.sell_price = dbr.ticksToPrice(v.sell_ticks, v.contr);
};

Model.prototype.enrichView = function(v) {
    v.contr = this.contrs[v.contr];
    v.bid_price = dbr.mapValue(v.bid_ticks, function(w) {
        return dbr.ticksToPrice(w, v.contr);
    });
    v.offer_price = dbr.mapValue(v.offer_ticks, function(w) {
        return dbr.ticksToPrice(w, v.contr);
    });
};

Model.prototype.createOrders = function() {
    var tbody = document.createElement('tbody');
    tbody.id = 'order-tbody';
    var ks = Object.keys(this.orders).sort();
    for (var i = 0; i < ks.length; ++i) {
        var v = this.orders[ks[i]];
        var tr = dbr.createRow(
            v.id,
            v.giveup,
            v.contr.mnem,
            v.settl_date,
            v.state,
            v.action,
            v.price,
            v.lots,
            v.resd,
            v.exec,
            v.last_price,
            v.last_lots);
        tr.onclick = this.setContext.bind(this, v.contr.mnem, v.settl_date, v.price, v.lots);
        tr.onmouseleave = this.clearInfo.bind(this);
        tr.onmouseover = this.setOrderInfo.bind(this, v);
        tbody.appendChild(tr);
    }
    return tbody;
};

Model.prototype.createTrades = function() {
    var tbody = document.createElement('tbody');
    tbody.id = 'trade-tbody';
    var ks = Object.keys(this.trades).sort();
    for (var i = 0; i < ks.length; ++i) {
        var v = this.trades[ks[i]];
        var tr = dbr.createRow(
            v.id,
            v.order,
            v.giveup,
            v.contr.mnem,
            v.settl_date,
            v.action,
            v.price,
            v.lots,
            v.resd,
            v.exec,
            v.last_price,
            v.last_lots);
        tr.onclick = this.setContext.bind(this, v.contr.mnem, v.settl_date, v.price, v.lots);
        tr.onmouseleave = this.clearInfo.bind(this);
        tr.onmouseover = this.setTradeInfo.bind(this, v);
        tbody.appendChild(tr);
    }
    return tbody;
};

Model.prototype.createPosns = function() {
    var tbody = document.createElement('tbody');
    tbody.id = 'posn-tbody';
    var ks = Object.keys(this.posns).sort();
    for (var i = 0; i < ks.length; ++i) {
        var v = this.posns[ks[i]];
        tbody.appendChild(dbr.createRow(
            v.accnt,
            v.contr.mnem,
            v.settl_date,
            v.buy_price,
            v.buy_lots,
            v.sell_price,
            v.sell_lots
        ));
    }
    return tbody;
};

Model.prototype.createViews = function() {
    var tbody = document.createElement('tbody');
    tbody.id = 'view-tbody';
    var ks = Object.keys(this.views).sort();
    for (var i = 0; i < ks.length; ++i) {
        var k = ks[i];
        if (!(k.split(',')[0] in this.filter))
            continue;
        var v = this.views[k];
        var tr = dbr.createRow(
            v.contr.mnem,
            v.settl_date,
            v.bid_price[0],
            v.bid_lots[0],
            v.bid_count[0],
            v.offer_price[0],
            v.offer_lots[0],
            v.offer_count[0]
        );
        tr.onclick = this.setContext.bind(this, v.contr.mnem, v.settl_date, '', '');
        tr.onmouseleave = this.clearInfo.bind(this);
        tr.onmouseover = this.setContrInfo.bind(this, v.contr);
        tbody.appendChild(tr);
    }
    return tbody;
};

Model.prototype.refresh = function() {
    this.refreshOrders();
    this.refreshTrades();
    this.refreshPosns();
    this.refreshViews();
}

Model.prototype.refreshOrders = function() {
    var that = this;
    $.ajax({
        type: 'get',
        url: '/api/order/' + that.trader
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict['_' + v.id] = v;
        });
        that.orders = dict;
        dbr.eachValue(that.orders, that.enrichOrder.bind(that));
        $('#order-tbody').replaceWith(that.createOrders())
        $('#order-tbody button').button();
    });
};

Model.prototype.refreshTrades = function() {
    var that = this;
    $.ajax({
        type: 'get',
        url: '/api/trade/' + that.trader
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict['_' + v.id] = v;
        });
        that.trades = dict;
        dbr.eachValue(that.trades, that.enrichTrade.bind(that));
        $('#trade-tbody').replaceWith(that.createTrades());
        $('#trade-tbody button').button();
    });
};

Model.prototype.refreshPosns = function() {
    var that = this;
    $.ajax({
        type: 'get',
        url: '/api/posn/' + that.giveup
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[[v.group, v.contr, v.settl_date]] = v;
        });
        that.posns = dict;
        dbr.eachValue(that.posns, that.enrichPosn.bind(that));
        $('#posn-tbody').replaceWith(that.createPosns());
    });
};

Model.prototype.refreshViews = function() {
    var that = this;
    $.ajax({
        type: 'get',
        url: '/api/view'
    }).done(function(arr) {
        var dict = [];
        $.each(arr, function(k, v) {
            dict[[v.contr, v.settl_date]] = v;
        });
        that.views = dict;
        dbr.eachValue(that.views, that.enrichView.bind(that));
        $('#view-tbody').replaceWith(that.createViews());
        $('#view-tbody button').button({
            icons: {
                primary: 'ui-icon-trash'
            }
        });
    });
};

Model.prototype.clearContext = function() {
	$('#contr').val('');
	$('#settl-date').val('');
	$('#price').val('');
	$('#lots').val('');
}

Model.prototype.setContext = function(contr, settl_date, price, lots) {
	$('#contr').val(contr);
	$('#settl-date').val(settl_date);
	$('#price').val(price);
	$('#lots').val(lots);
}

Model.prototype.clearInfo = function() {
    var div = document.createElement('div');
    div.id = 'info';
    div.className = 'ui-widget-content ui-corner-all';
    $('#info').replaceWith(div);
};

Model.prototype.setContrInfo = function(v) {
    var div = document.createElement('div');
    div.id = 'info';
    div.className = 'ui-widget-content ui-corner-all';
    dbr.appendField(div, 'Mnem', v.mnem);
    dbr.appendField(div, 'Display', v.display);
    dbr.appendField(div, 'Asset Type', v.asset_type);
    dbr.appendField(div, 'Asset', v.asset);
    dbr.appendField(div, 'Ccy', v.ccy);
    dbr.appendField(div, 'Price Inc', v.price_inc);
    dbr.appendField(div, 'Qty Inc', v.qty_inc);
    dbr.appendField(div, 'Price Dp', v.price_dp);
    dbr.appendField(div, 'Pip Dp', v.pip_dp);
    dbr.appendField(div, 'Qty Dp', v.qty_dp);
    dbr.appendField(div, 'Min Lots', v.min_lots);
    dbr.appendField(div, 'Max Lots', v.max_lots);
    $('#info').replaceWith(div);
};

Model.prototype.setTradeInfo = function(v) {
    var div = document.createElement('div');
    div.id = 'info';
    div.className = 'ui-widget-content ui-corner-all';
    dbr.appendField(div, 'Id', v.id);
    dbr.appendField(div, 'Order', v.order);
    dbr.appendField(div, 'Trader', v.trader);
    dbr.appendField(div, 'Giveup', v.giveup);
    dbr.appendField(div, 'Contr', v.contr.mnem);
    dbr.appendField(div, 'Settl Date', v.settl_date);
    dbr.appendField(div, 'Ref', v.ref);
    dbr.appendField(div, 'State', v.state);
    dbr.appendField(div, 'Action', v.action);
    dbr.appendField(div, 'Price', v.price);
    dbr.appendField(div, 'Lots', v.lots);
    dbr.appendField(div, 'Resd', v.resd);
    dbr.appendField(div, 'Exec', v.exec);
    dbr.appendField(div, 'Last Price', v.last_price);
    dbr.appendField(div, 'Last Lots', v.last_lots);
    dbr.appendField(div, 'Match', v.match);
    dbr.appendField(div, 'Role', v.role);
    dbr.appendField(div, 'Cpty', v.cpty);
    dbr.appendField(div, 'Created', v.created);
    $('#info').replaceWith(div);
};

Model.prototype.setOrderInfo = function(v) {
    var div = document.createElement('div');
    div.id = 'info';
    div.className = 'ui-widget-content ui-corner-all';
    dbr.appendField(div, 'Id', v.id);
    dbr.appendField(div, 'Trader', v.trader);
    dbr.appendField(div, 'Giveup', v.giveup);
    dbr.appendField(div, 'Contr', v.contr.mnem);
    dbr.appendField(div, 'Settl Date', v.settl_date);
    dbr.appendField(div, 'Ref', v.ref);
    dbr.appendField(div, 'State', v.state);
    dbr.appendField(div, 'Action', v.action);
    dbr.appendField(div, 'Price', v.price);
    dbr.appendField(div, 'Lots', v.lots);
    dbr.appendField(div, 'Resd', v.resd);
    dbr.appendField(div, 'Exec', v.exec);
    dbr.appendField(div, 'Last Price', v.last_price);
    dbr.appendField(div, 'Last Lots', v.last_lots);
    dbr.appendField(div, 'Created', v.created);
    dbr.appendField(div, 'Modified', v.modified);
    $('#info').replaceWith(div);
};

Model.prototype.submitOrder = function(contr, settl_date, action, price, lots) {
    var that = this;
    var contr = this.contrs[contr];
    var ticks = dbr.priceToTicks(price, contr);
    $.ajax({
        type: 'post',
        url: '/api/order/' + that.trader,
        data: JSON.stringify({
            giveup: that.giveup,
            contr: contr.mnem,
            settl_date: parseInt(settl_date),
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
        $('#error-num').html(v.num);
        $('#error-msg').html(v.msg);
		$('#error-dialog').dialog('open');
    });
};

Model.prototype.cancelOrder = function(id) {
    var that = this;
    $.ajax({
        type: 'put',
        url: '/api/order/' + that.trader + '/' + id,
        data: '{"lots":0}'
    }).done(function(v) {
        v.contr = that.contrs[v.contr];
        v.price = dbr.ticksToPrice(v.ticks, v.contr);
        v.created = new Date(v.created);
        v.modified = new Date(v.modified);
        that.orders['_' + v.id] = v;
        $('#order-tbody').replaceWith(that.createOrders());
        $('#order-tbody button').button();
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
        $('#error-num').html(v.num);
        $('#error-msg').html(v.msg);
		$('#error-dialog').dialog('open');
    });
};

Model.prototype.ackTrade = function(id) {
    var that = this;
    $.ajax({
        type: 'delete',
        url: '/api/trade/' + that.trader + '/' + id
    }).done(function(v) {
        delete that.trades['_' + id];
        $('#trade-tbody').replaceWith(that.createTrades());
        $('#trade-tbody button').button();
    }).fail(function(r) {
        var v = $.parseJSON(r.responseText);
        $('#error-num').html(v.num);
        $('#error-msg').html(v.msg);
		$('#error-dialog').dialog('open');
    });
};

var model = null;

// Lifecycle phases.

function documentReady() {

	var contr = $('#contr'),
        settl_date = $('#settl-date'),
	    price = $('#price'),
	    lots = $('#lots');

    model = new Model('WRAMIREZ', 'DBRA', 'test', function(model) {
	    contr.autocomplete({
		    source: Object.keys(model.contrs)
	    });
    });

    $('#subscribe').button()
        .click(function(event) {
            model.subscribe(contr.val());
		});
    $('#unsubscribe').button()
        .click(function(event) {
            model.unsubscribe(contr.val());
		});
    $('#buy').button()
	    .click(function() {
            model.submitOrder(contr.val(), settl_date.val(), 'BUY', price.val(), lots.val());
		});
    $('#sell').button()
	    .click(function() {
            model.submitOrder(contr.val(), settl_date.val(), 'SELL', price.val(), lots.val());
		});
    $('#revise').button();
    $('#cancel').button();
    $('#ack').button();

    $('#lots').spinner();
    $('#tabs').tabs();

	$('#error-dialog').dialog({
		autoOpen: false,
		modal: true,
		buttons: {
			Ok: function() {
	            $(this).dialog('close');
	        }
		}
	});
}
