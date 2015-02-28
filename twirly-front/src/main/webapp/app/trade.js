/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/

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
    self.markets = [];

    self.views = ko.observableArray([]);
    self.orders = ko.observableArray([]);
    self.trades = ko.observableArray([]);
    self.posns = ko.observableArray([]);

    self.selectedTab = ko.observable();
    self.allViews = ko.observable(false);
    self.allWorking = ko.observable(false);
    self.allDone = ko.observable(false);
    self.allTrades = ko.observable(false);

    self.market = ko.observable();
    self.price = ko.observable();
    self.lots = ko.observable();

    self.views.extend({ rateLimit: 25 });
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

    self.isViewSelected = ko.computed(function() {
        var views = self.views();
        for (var i = 0; i < views.length; ++i) {
            if (views[i].isSelected())
                return true;
        }
        return false;
    });

    self.isWorkingSelected = ko.computed(function() {
        if (self.selectedTab() !== 'workingTab') {
            return false;
        }
        var orders = self.working();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected()) {
                return true;
            }
        }
        return false;
    });

    self.isDoneSelected = ko.computed(function() {
        if (self.selectedTab() !== 'doneTab') {
            return false;
        }
        var orders = self.done();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected()) {
                return true;
            }
        }
        return false;
    });

    self.isOrderSelected = ko.computed(function() {
        return self.isWorkingSelected() || self.isDoneSelected();
    });

    self.isTradeSelected = ko.computed(function() {
        if (self.selectedTab() !== 'tradeTab') {
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

    self.allViews.subscribe(function(val) {
        var views = self.views();
        for (var i = 0; i < views.length; ++i) {
            views[i].isSelected(val);
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

    self.market.subscribe(function(val) {
        var contr = self.markets[val];
        if (contr !== undefined) {
            $('#price').attr('step', contr.priceInc);
            $('#lots').attr('min', contr.minLots);
            $('#reviseLots').attr('min', contr.minLots);
        }
    });

    self.selectTab = function(val, event) {
        self.selectedTab(event.target.id);
    };

    self.selectView = function(val) {
        self.market(val.market());
        self.price(0);
        return true;
    };

    self.selectBid = function(val) {
        self.market(val.market());
        var price = val.bidPrice()[0];
        if (price !== null) {
            self.price(price);
            self.lots(val.bidLots()[0]);
        } else {
            self.price(0);
        }
        return true;
    };

    self.selectOffer = function(val) {
        self.market(val.market());
        var price = val.offerPrice()[0];
        if (price !== null) {
            self.price(price);
            self.lots(val.offerLots()[0]);
        } else {
            self.price(0);
        }
        return true;
    };

    self.selectLast = function(val) {
        self.market(val.market());
        var price = val.lastPrice();
        if (price !== null) {
            self.price(price);
        } else {
            self.price(0);
        }
        return true;
    };

    self.selectOrder = function(val) {
        self.market(val.market());
        self.price(val.price());
        self.lots(val.resd() > 0 ? val.resd() : val.lots());
        return true;
    };

    self.selectTrade = function(val) {
        self.market(val.market());
        self.price(val.price());
        self.lots(val.resd() > 0 ? val.resd() : val.lots());
        return true;
    };

    self.findView = function(market) {
        return ko.utils.arrayFirst(self.views(), function(val) {
            return val.market() === market;
        });
    };

    self.findOrder = function(market, id) {
        return ko.utils.arrayFirst(self.orders(), function(val) {
            return val.market() === market && val.id() === id;
        });
    };

    self.removeOrder = function(market, id) {
        self.orders.remove(function(val) {
            return val.market() === market && val.id() === id;
        });
    };

    self.findTrade = function(market, id) {
        return ko.utils.arrayFirst(self.trades(), function(val) {
            return val.market() === market && val.id() === id;
        });
    };

    self.removeTrade = function(market, id) {
        self.trades.remove(function(val) {
            return val.market() === market && val.id() === id;
        });
    };

    self.findPosn = function(contr, settlDate) {
        return ko.utils.arrayFirst(self.posns(), function(val) {
            return val.contr().mnem === contr && toDateInt(val.settlDate()) === settlDate;
        });
    };

    self.applyTrans = function(raw) {
        if (raw.view !== null) {
            view = self.findView(raw.view.market);
            if (view !== null) {
                view.update(raw.view);
            } else {
                raw.view.isSelected = false;
                self.views.push(new View(raw.view, self.contrs));
            }
        }
        $.each(raw.orders, function(key, val) {
            order = self.findOrder(val.market, val.id);
            if (order !== null) {
                order.update(val);
            } else {
                val.isSelected = false;
                self.orders.push(new Order(val, self.contrs));
            }
        });
        $.each(raw.execs, function(key, val) {
            if (val.state === 'TRADE') {
                val.isSelected = false;
                self.trades.push(new Trade(val, self.contrs));
            }
        });
        if (raw.posn !== null) {
            posn = self.findPosn(raw.posn.contr, raw.posn.settlDate);
            if (posn !== null) {
                posn.update(raw.posn);
            } else {
                self.posns.push(new Posn(raw.posn, self.contrs));
            }
        }
    };

    self.refreshAll = function() {

        $.getJSON('/api/view', function(raw) {

            var markets = [];
            var cooked = $.map(raw, function(val) {
                view = self.findView(val.market);
                if (view !== null) {
                    view.update(val);
                } else {
                    val.isSelected = false;
                    view = new View(val, self.contrs);
                }
                markets[val.market] = view.contr();
                return view;
            });
            self.markets = markets;
            self.views(cooked);

            $('#market').typeahead({
                items: 4,
                source: Object.keys(markets)
            });
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });

        $.getJSON('/api/sess', function(raw) {

            var cooked = $.map(raw.orders, function(val) {
                order = self.findOrder(val.market, val.id);
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
                trade = self.findTrade(val.market, val.id);
                if (trade === null) {
                    val.isSelected = false;
                    trade = new Trade(val, self.contrs);
                }
                return trade;
            });
            self.trades(cooked);

            cooked = $.map(raw.posns, function(val) {
                posn = self.findPosn(val.contr, val.settlDate);
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
        var market = self.market();
        if (!isSpecified(market)) {
            self.showError(internalError('market not specified'));
            return;
        }
        var contr = self.markets[market];
        if (contr === undefined) {
            self.showError(internalError('invalid market: ' + market));
            return;
        }
        var price = self.price();
        if (!isSpecified(price)) {
            self.showError(internalError('price not specified'));
            return;
        }
        var ticks = priceToTicks(price, contr);
        var lots = self.lots();
        if (!isSpecified(lots) || lots === 0) {
            self.showError(internalError('lots not specified'));
            return;
        }
        lots = parseInt(lots);

        $.ajax({
            type: 'post',
            url: '/api/sess/order/' + market,
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
        var market = order.market();
        var id = order.id();
        var lots = self.lots();
        if (!isSpecified(lots)) {
            self.showError(internalError('lots not specified'));
            return;
        }
        lots = parseInt(lots);

        $.ajax({
            type: 'put',
            url: '/api/sess/order/' + market + '/' + id,
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
        var market = order.market();
        var id = order.id();
        $.ajax({
            type: 'put',
            url: '/api/sess/order/' + market + '/' + id,
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
        var market = order.market();
        var id = order.id();
        $.ajax({
            type: 'delete',
            url: '/api/sess/order/' + market + '/' + id
        }).done(function(raw) {
            self.removeOrder(market, id);
        }).fail(function(xhr) {
            self.showError(new Error(xhr));
        });
    };

    self.archiveTrade = function(trade) {
        var market = trade.market();
        var id = trade.id();
        $.ajax({
            type: 'delete',
            url: '/api/sess/trade/' + market + '/' + id
        }).done(function(raw) {
            self.removeTrade(market, id);
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

    $.getJSON('/api/rec/contr', function(raw) {
        var contrs = [];
        $.each(raw, function(key, val) {

            var tickNumer = val.tickNumer;
            var tickDenom = val.tickDenom;
            var priceInc = fractToReal(tickNumer, tickDenom);

            var lotNumer = val.lotNumer;
            var lotDenom = val.lotDenom;
            var qtyInc = fractToReal(lotNumer, lotDenom);

            val.priceDp = realToDp(priceInc);
            val.priceInc = priceInc.toFixed(val.priceDp);

            val.qtyDp = realToDp(qtyInc);
            val.qtyInc = qtyInc.toFixed(val.qtyDp);

            contrs[val.mnem] = val;
        });
        var model = new ViewModel(contrs);
        ko.applyBindings(model);
        $('#tabs').tab();
        $('#workingTab').click();
        model.refreshAll();
        setInterval(function() {
            model.refreshAll();
        }, 5000);
    }).fail(function(xhr) {
        var model = new ViewModel([]);
        ko.applyBindings(model);
        $('#tabs').tab();
        $('#workingTab').click();
        model.showError(new Error(xhr));
    });
}
