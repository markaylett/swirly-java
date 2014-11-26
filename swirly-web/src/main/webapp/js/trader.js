/***************************************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 **************************************************************************************************/
 
function ViewModel(contrs) {
    var self = this;

    self.contrs = contrs;

    self.books = ko.observableArray([]);
    self.orders = ko.observableArray([]);
    self.trades = ko.observableArray([]);
    self.posns = ko.observableArray([]);

    self.selectedTab = ko.observable();
    self.allOrders = ko.observable(false);
    self.allTrades = ko.observable(false);

    self.contrMnem = ko.observable();
    self.settlDate = ko.observable();
    self.price = ko.observable();
    self.lots = ko.observable();

    self.books.extend({ rateLimit: 25 });
    self.orders.extend({ rateLimit: 25 });
    self.trades.extend({ rateLimit: 25 });
    self.posns.extend({ rateLimit: 25 });

    self.isOrderSelected = ko.computed(function() {
        if (self.selectedTab() != 'orderTab') {
            return false;
        }
        var orders = self.orders();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected())
                return true;
        }
        return false;
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

    self.allOrders.subscribe(function(val) {
        var orders = self.orders();
        for (var i = 0; i < orders.length; ++i) {
            orders[i].isSelected(val);
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
        }
    });

    self.selectBid = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.bidPrice());
        return true;
    };

    self.selectTab = function(val, event) {
        self.selectedTab(event.target.id);
    };

    self.selectOffer = function(val) {
        self.contrMnem(val.contr().mnem);
        self.settlDate(val.settlDate());
        self.price(val.offerPrice());
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

    self.findBook = function(id) {
        return ko.utils.arrayFirst(self.books(), function(val) {
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
        if ('book' in raw) {
            book = self.findBook(raw.book.id);
            if (book !== null) {
                book.update(raw.book);
            } else {
                self.books.push(new Book(raw.book, self.contrs));
            }
        }
        $.each(raw.orders, function(key, val) {
            if (val.resd > 0) {
                order = self.findOrder(val.id);
                if (order !== null) {
                    order.update(val);
                } else {
                    val.isSelected = false;
                    self.orders.push(new Order(val, self.contrs));
                }
            } else {
                self.removeOrder(val.id);
            }
        });
        $.each(raw.execs, function(key, val) {
            if (val.state == 'TRADE') {
                val.isSelected = false;
                self.trades.push(new Trade(val, self.contrs));
            }
        });
        if ('posn' in raw) {
            posn = self.findPosn(raw.posn.id);
            if (posn !== null) {
                posn.update(raw.posn);
            } else {
                self.posns.push(new Posn(raw.posn, self.contrs));
            }
        }
    };

    self.refreshAll = function() {

        $.getJSON('/api/book', function(raw) {

            var cooked = $.map(raw, function(val) {
                return new Book(val, self.contrs);
            });
            self.books(cooked);
        });

        $.getJSON('/api/accnt', function(raw) {

            var cooked = $.map(raw.orders, function(val) {
                order = self.findOrder(val.id);
                val.isSelected = (order !== null && order.isSelected());
                return new Order(val, self.contrs);
            });
            self.orders(cooked);

            cooked = $.map(raw.trades, function(val) {
                order = self.findTrade(val.id);
                val.isSelected = (order !== null && order.isSelected());
                return new Trade(val, self.contrs);
            });
            self.trades(cooked);

            cooked = $.map(raw.posns, function(val) {
                return new Posn(val, self.contrs);
            });
            self.posns(cooked);
        });
    };

    self.submitOrder = function(action) {
        var contr = self.contrs[self.contrMnem()];
        var settlDate = toDateInt(self.settlDate());
        var ticks = priceToTicks(self.price(), contr);
        var lots = parseInt(self.lots());
        $.ajax({
            type: 'post',
            url: '/api/accnt/order/',
            data: JSON.stringify({
                contr: contr.mnem,
                settlDate: settlDate,
                ref: '',
                action: action,
                ticks: ticks,
                lots: lots,
                minLots: 0
            })
        }).done(function(raw) {
            self.applyTrans(raw);
        });
    };

    self.submitBuy = function() {
        self.submitOrder('BUY');
    };

    self.submitSell = function() {
        self.submitOrder('SELL');
    };

    self.reviseOrder = function(id) {
        var lots = parseInt(self.lots());
        $.ajax({
            type: 'put',
            url: '/api/accnt/order/' + id,
            data: JSON.stringify({
                lots: lots
            })
        }).done(function(raw) {
            self.applyTrans(raw);
        });
    };

    self.reviseAll = function() {
        var orders = self.orders();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected()) {
                self.reviseOrder(orders[i].id());
            }
        }
    };

    self.cancelOrder = function(id) {
        $.ajax({
            type: 'put',
            url: '/api/accnt/order/' + id,
            data: '{"lots":0}'
        }).done(function(raw) {
            self.applyTrans(raw);
        });
    };

    self.cancelAll = function() {
        var orders = self.orders();
        for (var i = 0; i < orders.length; ++i) {
            if (orders[i].isSelected()) {
                self.cancelOrder(orders[i].id());
            }
        }
    };

    self.confirmTrade = function(id) {
        $.ajax({
            type: 'delete',
            url: '/api/accnt/trade/' + id
        }).done(function(raw) {
            self.removeTrade(id);
        });
    };

    self.confirmAll = function() {
        var trades = self.trades();
        for (var i = 0; i < trades.length; ++i) {
            if (trades[i].isSelected()) {
                self.confirmTrade(trades[i].id());
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
        $('#orderTab').click();
        model.refreshAll();
        setInterval(function() {
            model.refreshAll();
        }, 10000);
    });
}