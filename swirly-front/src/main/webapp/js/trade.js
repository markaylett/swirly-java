/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/
 
ko.bindingHandlers.depth = {
    update: function(elem, valAccessor, allBindings, viewModel, bindingContext) {
        var val = valAccessor();
        var arr = val();
        if (!bindingContext.$rawData.isSelected()) {
            $(elem).text(optional(arr[0]));
            return;
        }
        var html = '';
        for (var i = 0; i < arr.length; ++i) {
            if (i > 0) {
                html += '<br/>';
            }
            html += optional(arr[i]);
        }
        $(elem).html(html);
    }
};

function ViewModel(contrs) {
    var self = this;

    self.errors = ko.observableArray([]);

    self.clearErrors = function() {
        self.errors.removeAll();
    };

    self.hasErrors = ko.computed(function() {
        return self.errors().length > 0;
    });

    self.showError = function(error) {
        // Add to top of list.
        self.errors.unshift(error);
        // Limit to last 5 errors.
        if (self.errors().length > 5) {
            self.errors.pop();
        }
    };

    self.contrs = contrs;

    self.markets = ko.observableArray([]);
    self.orders = ko.observableArray([]);
    self.trades = ko.observableArray([]);
    self.posns = ko.observableArray([]);

    self.selectedTab = ko.observable();
    self.allMarkets = ko.observable(false);
    self.allWorking = ko.observable(false);
    self.allDone = ko.observable(false);
    self.allTrades = ko.observable(false);

    self.contrMnem = ko.observable();
    self.settlDate = ko.observable();
    self.price = ko.observable();
    self.lots = ko.observable();

    self.markets.extend({ rateLimit: 25 });
    self.orders.extend({ rateLimit: 25 });
    self.trades.extend({ rateLimit: 25 });
    self.posns.extend({ rateLimit: 25 });

    self.working = ko.computed(function() {
        return ko.utils.arrayFilter(self.orders(), function(val) {
            return !val.isDone();
        });
    });

    self.done = ko.computed(function() {
        return ko.utils.arrayFilter(self.orders(), function(val) {
            return val.isDone();
        });
    });

    self.isMarketSelected = ko.computed(function() {
        var markets = self.markets();
        for (var i = 0; i < markets.length; ++i) {
            if (markets[i].isSelected())
                return true;
        }
        return false;
    });

    self.isWorkingSelected = ko.computed(function() {
        if (self.selectedTab() != 'workingTab') {
            return false;
        }
        var orders = self.working();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected())
                return true;
        }
        return false;
    });

    self.isDoneSelected = ko.computed(function() {
        if (self.selectedTab() != 'doneTab') {
            return false;
        }
        var orders = self.done();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected())
                return true;
        }
        return false;
    });

    self.isOrderSelected = ko.computed(function() {
        return self.isWorkingSelected() || self.isDoneSelected();
    });

    self.isTradeSelected = ko.computed(function() {
        if (self.selectedTab() != 'tradeTab') {
            return false;
        }
        var trades = self.trades();
        for (var i = 0; i < trades.length; ++i) {
            if (trades[i].isSelected())
                return true;
        }
        return false;
    });

    self.isArchivableSelected = ko.computed(function() {
        return self.isDoneSelected() || self.isTradeSelected();
    });

    self.allMarkets.subscribe(function(val) {
        var markets = self.markets();
        for (var i = 0; i < markets.length; ++i) {
            markets[i].isSelected(val);
        }
    });

    self.allWorking.subscribe(function(val) {
        var working = self.working();
        for (var i = 0; i < working.length; ++i) {
            working[i].isSelected(val);
        }
    });

    self.allDone.subscribe(function(val) {
        var done = self.done();
        for (var i = 0; i < done.length; ++i) {
            done[i].isSelected(val);
        }
    });

    self.allTrades.subscribe(function(val) {
        var trades = self.trades();
        for (var i = 0; i < trades.length; ++i) {
            trades[i].isSelected(val);
        }
    });

    self.contrMnem.subscribe(function(val) {
        if (val in self.contrs) {
            var contr = self.contrs[val];
            $('#price').attr('step', contr.priceInc);
            $('#lots').attr('min', contr.minLots);
            $('#reviseLots').attr('min', contr.minLots);
        }
    });

    self.selectTab = function(val, event) {
        self.selectedTab(event.target.id);
    };

    self.selectBid = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        var price = val.bidPrice()[0];
        if (price !== null) {
            self.price(price);
        }
        return true;
    };

    self.selectOffer = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        var price = val.offerPrice()[0];
        if (price !== null) {
            self.price(price);
        }
        return true;
    };

    self.selectOrder = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.price());
        self.lots(val.resd());
        return true;
    };

    self.selectTrade = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.price());
        self.lots(val.resd());
        return true;
    };

    self.selectBuy = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.buyPrice());
        return true;
    };

    self.selectSell = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.sellPrice());
        return true;
    };

    self.selectNet = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.netPrice());
        return true;
    };

    self.findMarket = function(id) {
        return ko.utils.arrayFirst(self.markets(), function(val) {
            return val.id() === id;
        });
    };

    self.findOrder = function(id) {
        return ko.utils.arrayFirst(self.orders(), function(val) {
            return val.id() === id;
        });
    };

    self.removeOrder = function(id) {
        self.orders.remove(function(val) {
            return val.id() === id;
        });
    };

    self.findTrade = function(id) {
        return ko.utils.arrayFirst(self.trades(), function(val) {
            return val.id() === id;
        });
    };

    self.removeTrade = function(id) {
        self.trades.remove(function(val) {
            return val.id() === id;
        });
    };

    self.findPosn = function(id) {
        return ko.utils.arrayFirst(self.posns(), function(val) {
            return val.id() === id;
        });
    };

    self.applyTrans = function(raw) {
        if (raw.market !== null) {
            market = self.findMarket(raw.market.id);
            if (market !== null) {
                market.update(raw.market);
            } else {
                raw.market.isSelected = false;
                self.markets.push(new Market(raw.market, self.contrs));
            }
        }
        $.each(raw.orders, function(key, val) {
            order = self.findOrder(val.id);
            if (order !== null) {
                order.update(val);
            } else {
                val.isSelected = false;
                self.orders.push(new Order(val, self.contrs));
            }
        });
        $.each(raw.execs, function(key, val) {
            if (val.state == 'TRADE') {
                val.isSelected = false;
                self.trades.push(new Trade(val, self.contrs));
            }
        });
        if (raw.posn !== null) {
            posn = self.findPosn(raw.posn.id);
            if (posn !== null) {
                posn.update(raw.posn);
            } else {
                self.posns.push(new Posn(raw.posn, self.contrs));
            }
        }
    };

    self.refreshAll = function() {

        $.getJSON('/api/market', function(raw) {

            var cooked = $.map(raw, function(val) {
                market = self.findMarket(val.id);
                if (market !== null) {
                    market.update(val);
                } else {
                    val.isSelected = false;
                    market = new Market(val, self.contrs);
                }
                return market;
            });
            self.markets(cooked);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });

        $.getJSON('/api/accnt', function(raw) {

            var cooked = $.map(raw.orders, function(val) {
                order = self.findOrder(val.id);
                if (order !== null) {
                    order.update(val);
                } else {
                    val.isSelected = false;
                    order = new Order(val, self.contrs);
                }
                return order;
            });
            self.orders(cooked);

            cooked = $.map(raw.trades, function(val) {
                trade = self.findTrade(val.id);
                if (trade === null) {
                    val.isSelected = false;
                    trade = new Trade(val, self.contrs);
                }
                return trade;
            });
            self.trades(cooked);

            cooked = $.map(raw.posns, function(val) {
                posn = self.findPosn(val.id);
                if (posn !== null) {
                    posn.update(val);
                } else {
                    val.isSelected = false;
                    posn = new Posn(val, self.contrs);
                }
                return posn;
            });
            self.posns(cooked);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.submitOrder = function(action) {
        var contr = self.contrMnem();
        if (!isSpecified(contr)) {
            self.showError(internalError('contract not specified'));
            return;
        }
        contr = self.contrs[contr];
        if (contr === undefined) {
            self.showError(internalError('invalid contract: ' + self.contrMnem()));
            return;
        }
        var settlDate = self.settlDate();
        if (!isSpecified(settlDate)) {
            self.showError(internalError('settl-date not specified'));
            return;
        }
        settlDate = toDateInt(settlDate);
        var price = self.price();
        if (!isSpecified(price)) {
            self.showError(internalError('price not specified'));
            return;
        }
        var ticks = priceToTicks(price, contr);
        var lots = self.lots();
        if (!isSpecified(lots) || lots == 0) {
            self.showError(internalError('lots not specified'));
            return;
        }
        lots = parseInt(lots);

        $.ajax({
            type: 'post',
            url: '/api/accnt/order/' + contr.mnem + '/' + settlDate,
            data: JSON.stringify({
                ref: '',
                action: action,
                ticks: ticks,
                lots: lots,
                minLots: 0
            })
        }).done(function(raw) {
            self.applyTrans(raw);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.submitBuy = function() {
        self.submitOrder('BUY');
    };

    self.submitSell = function() {
        self.submitOrder('SELL');
    };

    self.reviseOrder = function(order) {
        var contr = order.contr().mnem;
        var settlDate = toDateInt(order.settlDate());
        var id = order.id();
        var lots = self.lots();
        if (!isSpecified(lots)) {
            self.showError(internalError('lots not specified'));
            return;
        }
        lots = parseInt(lots);

        $.ajax({
            type: 'put',
            url: '/api/accnt/order/' + contr + '/' + settlDate + '/' + id,
            data: JSON.stringify({
                lots: lots
            })
        }).done(function(raw) {
            self.applyTrans(raw);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.reviseAll = function() {
        var orders = self.orders();
        for (var i = 0; i < orders.length; ++i) {
            var order = orders[i];
            if (order.isSelected()) {
                self.reviseOrder(order);
            }
        }
    };

    self.cancelOrder = function(order) {
        var contr = order.contr().mnem;
        var settlDate = toDateInt(order.settlDate());
        var id = order.id();

        $.ajax({
            type: 'put',
            url: '/api/accnt/order/' + contr + '/' + settlDate + '/' + id,
            data: '{"lots":0}'
        }).done(function(raw) {
            self.applyTrans(raw);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.cancelAll = function() {
        var orders = self.orders();
        for (var i = 0; i < orders.length; ++i) {
            var order = orders[i];
            if (order.isSelected()) {
                self.cancelOrder(order);
            }
        }
    };

    self.archiveOrder = function(order) {
        var contr = order.contr().mnem;
        var settlDate = toDateInt(order.settlDate());
        var id = order.id();
        $.ajax({
            type: 'delete',
            url: '/api/accnt/order/' + contr + '/' + settlDate + '/' + id
        }).done(function(raw) {
            self.removeOrder(id);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.archiveTrade = function(trade) {
        var contr = trade.contr().mnem;
        var settlDate = toDateInt(trade.settlDate());
        var id = trade.id();
        $.ajax({
            type: 'delete',
            url: '/api/accnt/trade/' + contr + '/' + settlDate + '/' + id
        }).done(function(raw) {
            self.removeTrade(id);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.archiveAll = function() {
        if (self.selectedTab() === 'doneTab') {
            var orders = self.done();
            for (var i = 0; i < orders.length; ++i) {
                var order = orders[i];
                if (order.isSelected()) {
                    self.archiveOrder(order);
                }
            }
        } else if (self.selectedTab() === 'tradeTab') {
            var trades = self.trades();
            for (var i = 0; i < trades.length; ++i) {
                var trade = trades[i];
                if (trade.isSelected()) {
                    self.archiveTrade(trade);
                }
            }
        }
    };
}

function initApp() {

    $('#tabs').tab();

    $.getJSON('/api/rec/contr', function(raw) {
        var contrs = [];
        $.each(raw, function(key, val) {
            val.priceInc = priceInc(val);
            val.qtyInc = qtyInc(val);
            contrs[val.mnem] = val;
        });
        $('#contr').typeahead({
            items: 4,
            source: Object.keys(contrs)
        });
        var model = new ViewModel(contrs);
        ko.applyBindings(model);
        $('#workingTab').click();
        model.refreshAll();
        setInterval(function() {
            model.refreshAll();
        }, 10000);
    }).fail(function(xhr) {
        self.showError(new Error(xhr));
    });
}
