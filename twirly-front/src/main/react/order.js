/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var OrderModuleImpl = React.createClass({
    // Mutators.
    refresh: function(url) {
        if (url === undefined) {
            url = '/front/sess/order,trade,posn,view';
        }
        $.getJSON(url, function(sess, status, xhr) {
            this.resetTimeout(xhr);
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            staging.working.clear();
            staging.done.clear();
            sess.orders.forEach(function(order) {
                enrichOrder(contrMap, order);
                var orders = order.isDone ? staging.done : staging.working;
                orders.set(order.key, order);
            });

            staging.trades.clear();
            sess.trades.forEach(function(trade) {
                enrichTrade(contrMap, trade);
                staging.trades.set(trade.key, trade);
            });

            staging.posns.clear();
            sess.posns.forEach(function(posn) {
                enrichPosn(contrMap, posn);
                staging.posns.set(posn.key, posn);
            });

            staging.views.clear();
            sess.views.forEach(function(view) {
                enrichView(contrMap, view);
                staging.views.set(view.key, view);
            });

            this.setState({
                sess: {
                    working: staging.working.toSortedArray(),
                    done: staging.done.toSortedArray(),
                    trades: staging.trades.toSortedArray(),
                    posns: staging.posns.toSortedArray(),
                    views: staging.views.toSortedArray()
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    updateSelected: function() {
        var isSelectedWorking = false;
        var isSelectedDone = false;
        var isSelectedTrade = false;
        var isSelectedArchivable = false;
        if (this.staging.context === 'working') {
            if (!this.staging.selectedWorking.isEmpty()) {
                isSelectedWorking = true;
            }
        } else if (this.staging.context === 'done') {
            if (!this.staging.selectedDone.isEmpty()) {
                isSelectedDone = true;
                isSelectedArchivable = true;
            }
        } else if (this.staging.context === 'trades') {
            if (!this.staging.selectedTrades.isEmpty()) {
                isSelectedTrade = true;
                isSelectedArchivable = true;
            }
        }
        this.setState({
            isSelectedWorking: isSelectedWorking,
            isSelectedDone: isSelectedDone,
            isSelectedTrade: isSelectedTrade,
            isSelectedArchivable: isSelectedArchivable
        });
    },
    postOrder: function(market, quoteId, side, lots, price) {
        console.debug('postOrder: market=' + market + ', quoteId=' + quoteId
                      + ', side=' + side + ', lots=' + lots + ', price=' + price);
        var req = {};
        var contr = undefined;
        if (isSpecified(market)) {
            contr = this.props.marketMap[market];
        } else {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (contr === undefined) {
            this.onReportError(internalError('invalid market: ' + market));
            return;
        }
        // Optional quoteId.
        if (isSpecified(quoteId) && quoteId > 0) {
            req.quoteId = parseInt(quoteId);
        }
        if (isSpecified(side)) {
            req.side = side;
        } else {
            this.onReportError(internalError('side not specified'));
            return;
        }
        if (isSpecified(lots) && lots > 0) {
            req.lots = parseInt(lots);
        } else {
            this.onReportError(internalError('lots not specified'));
            return;
        }
        if (isSpecified(price)) {
            req.ticks = priceToTicks(price, contr);
        } else {
            this.onReportError(internalError('price not specified'));
            return;
        }

        $.ajax({
            type: 'post',
            url: '/back/sess/order/' + market,
            data: JSON.stringify(req)
        }).done(function(result, status, xhr) {
            this.resetTimeout(xhr);
            this.applyResult(result);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    putOrder: function(market, ids, lots) {
        console.debug('putOrder: market=' + market + ', ids=[' + ids + '], lots=' + lots);
        var req = {};
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!Array.isArray(ids) || ids.length === 0) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        if (isSpecified(lots)) {
            req.lots = parseInt(lots);
        } else {
            this.onReportError(internalError('lots not specified'));
            return;
        }
        $.ajax({
            type: 'put',
            url: '/back/sess/order/' + market + '/' + ids,
            data: JSON.stringify(req)
        }).done(function(result, status, xhr) {
            this.resetTimeout(xhr);
            this.applyResult(result);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    deleteOrder: function(market, ids) {
        console.debug('deleteOrder: market=' + market + ', id=' + ids);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!Array.isArray(ids) || ids.length === 0) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        $.ajax({
            type: 'delete',
            url: '/back/sess/order/' + market + '/' + ids
        }).done(function(unused, status, xhr) {
            this.resetTimeout(xhr);
            var done = this.staging.done;
            var sess = this.state.sess;
            ids.forEach(function(id) {
                var key = market + '/' + zeroPad(id);
                done.delete(key);
            }.bind(this));
            this.setState({
                sess: {
                    working: sess.working,
                    done: done.toSortedArray(),
                    trades: sess.trades,
                    posns: sess.posns,
                    views: sess.views
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    deleteTrade: function(market, ids) {
        console.debug('deleteTrade: market=' + market + ', ids=[' + ids + ']');
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!Array.isArray(ids) || ids.length === 0) {
            this.onReportError(internalError('trade-id not specified'));
            return;
        }
        $.ajax({
            type: 'delete',
            url: '/back/sess/trade/' + market + '/' + ids
        }).done(function(unused, status, xhr) {
            this.resetTimeout(xhr);
            var trades = this.staging.trades;
            var sess = this.state.sess;
            ids.forEach(function(id) {
                var key = market + '/' + zeroPad(id);
                trades.delete(key);
            }.bind(this));
            this.setState({
                sess: {
                    working: sess.working,
                    done: sess.done,
                    trades: trades.toSortedArray(),
                    posns: sess.posns,
                    views: sess.views
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    applyResult: function(result) {
        var contrMap = this.props.contrMap;
        var staging = this.staging;

        var working = staging.working;
        var done = staging.done;
        var trades = staging.trades;
        var posns = staging.posns;
        var views = staging.views;

        result.orders.forEach(function(order) {
            enrichOrder(contrMap, order);
            if (order.isDone) {
                working.delete(order.key);
                done.set(order.key, order);
            } else {
                working.set(order.key, order);
            }
        });
        result.execs.forEach(function(exec) {
            if (exec.state === 'TRADE') {
                enrichTrade(contrMap, exec);
                trades.set(exec.key, exec);
            }
        });
        var posn = result.posn;
        if (posn !== null) {
            enrichPosn(contrMap, posn);
            staging.posns.set(posn.key, posn);
        }
        var view = result.view;
        if (view !== null) {
            enrichView(contrMap, view);
            views.set(view.key, view);
        }
        this.setState({
            sess: {
                working: working.toSortedArray(),
                done: done.toSortedArray(),
                trades: trades.toSortedArray(),
                posns: posns.toSortedArray(),
                views: views.toSortedArray()
            }
        });
    },
    // DOM Events.
    onClearErrors: function() {
        console.debug('onClearErrors');
        var errors = this.staging.errors;
        errors.clear();
        this.setState({
            errors: errors.toArray()
        });
    },
    onReportError: function(error) {
        console.debug('onReportError: num=' + error.num + ', msg=' + error.msg);
        var errors = this.staging.errors;
        errors.push(error);
        this.setState({
            errors: errors.toArray()
        });
    },
    onRefresh: function() {
        console.debug('onRefresh');
        this.refresh();
    },
    onChangeContext: function(context) {
        console.debug('onChangeContext: context=' + context);
        this.staging.context = context;
        this.updateSelected();
    },
    onChangeFields: function(market, lots, price) {
        console.debug('onChangeFields: market=' + market + ', lots=' + lots + ', price=' + price);
        this.refs.newOrder.setFields(market, lots, price);
        this.refs.reviseOrder.setFields(market, lots, price);
    },
    onSelectOrder: function(order, isSelected) {
        console.debug('onSelectOrder: key=' + order.key + ', isSelected=' + isSelected);
        if (order.isDone) {
            var selectedDone = this.staging.selectedDone;
            if (isSelected) {
                selectedDone.set(order.key, order);
            } else {
                selectedDone.delete(order.key);
            }
        } else {
            var selectedWorking = this.staging.selectedWorking;
            if (isSelected) {
                selectedWorking.set(order.key, order);
            } else {
                selectedWorking.delete(order.key);
            }
        }
        this.updateSelected();
    },
    onSelectTrade: function(trade, isSelected) {
        console.debug('onSelectTrade: key=' + trade.key + ', isSelected=' + isSelected);
        var selectedTrades = this.staging.selectedTrades;
        if (isSelected) {
            selectedTrades.set(trade.key, trade);
        } else {
            selectedTrades.delete(trade.key);
        }
        this.updateSelected();
    },
    onPostOrder: function(market, quoteId, side, lots, price) {
        this.postOrder(market, quoteId, side, lots, price);
    },
    onReviseAll: function(lots) {
        if (this.staging.context === 'working') {
            var batches = new Map();
            this.staging.selectedWorking.forEach(function(key, order) {
                batches.push(order.market, order.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.putOrder(market, ids, lots);
            }.bind(this));
        }
    },
    onCancelAll: function() {
        if (this.staging.context === 'working') {
            var batches = new Map();
            this.staging.selectedWorking.forEach(function(key, order) {
                batches.push(order.market, order.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.putOrder(market, ids, 0);
            }.bind(this));
        }
    },
    onArchiveAll: function() {
        if (this.staging.context === 'done') {
            var batches = new Map();
            this.staging.selectedDone.forEach(function(key, order) {
                batches.push(order.market, order.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.deleteOrder(market, ids);
            }.bind(this));
        } else if (this.staging.context === 'trades') {
            var batches = new Map();
            this.staging.selectedTrades.forEach(function(key, trade) {
                batches.push(trade.market, trade.id);
            }.bind(this));
            batches.forEach(function(market, ids) {
                this.deleteTrade(market, ids);
            }.bind(this));
        }
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onRefresh: this.onRefresh,
                onChangeContext: this.onChangeContext,
                onChangeFields: this.onChangeFields,
                onSelectOrder: this.onSelectOrder,
                onSelectTrade: this.onSelectTrade,
                onPostOrder: this.onPostOrder,
                onReviseAll: this.onReviseAll,
                onCancelAll: this.onCancelAll,
                onArchiveAll: this.onArchiveAll
            },
            errors: [],
            sess: {
                working: [],
                done: [],
                trades: [],
                posns: [],
                views: []
            },
            isSelectedWorking: false,
            isSelectedDone: false,
            isSelectedTrade: false,
            isSelectedArchivable: false
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var props = this.props;
        var marketMap = props.marketMap;

        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        var sess = state.sess;
        var isSelectedWorking = state.isSelectedWorking;
        var isSelectedDone = state.isSelectedDone;
        var isSelectedTrade = state.isSelectedTrade;
        var isSelectedArchivable = state.isSelectedArchivable;

        var marginTop = {
            marginTop: 24
        };

        return (
            <div className="orderModule">
              <MultiAlertWidget module={module} errors={errors}/>
              <NewOrderForm ref="newOrder" module={module} marketMap={marketMap}
                            isSelectedWorking={isSelectedWorking}/>
              <div style={marginTop}>
                <ViewTable module={module} views={sess.views}/>
              </div>
              <div style={marginTop}>
                <ReviseOrderForm ref="reviseOrder" module={module} marketMap={marketMap}
                                 isSelectedArchivable={isSelectedArchivable}
                                 isSelectedWorking={isSelectedWorking}/>
              </div>
              <div style={marginTop}>
                <OrderWidget module={module} sess={sess}/>
              </div>
            </div>
        );
    },
    resetTimeout: function(xhr) {
        var timeout = parseInt(xhr.getResponseHeader('Twirly-Timeout'));
        clearTimeout(this.timeout);
        if (timeout !== 0) {
            console.debug('timeout set for ' + new Date(timeout));
            var delta = timeout - Date.now();
            this.timeout = setTimeout(function() {
                console.debug('timeout now at ' + new Date(timeout));
                this.refresh('/back/sess/order,trade,posn,view');
            }.bind(this), delta);
        } else {
            console.debug('timeout not set');
        }
    },
    staging: {
        errors: new Tail(5),
        working: new Map(),
        done: new Map(),
        trades: new Map(),
        posns: new Map(),
        views: new Map(),
        selectedWorking: new Map(),
        selectedDone: new Map(),
        selectedTrades: new Map()
    },
    timeout: undefined
});

var OrderModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/front/rec/contr,market', function(rec, status, xhr) {
            var contrMap = {};
            var marketMap = {};
            rec.contrs.forEach(function(contr) {
                enrichContr(contr);
                contrMap[contr.mnem] = contr;
            });
            rec.markets.forEach(function(market) {
                enrichMarket(contrMap, market);
                marketMap[market.key] = market.contr;
            });
            this.setState({
                contrMap: contrMap,
                marketMap: marketMap
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
        // App Engine work-around: this deliberate "ping" to the back-end ensures that it is alive
        // and ready to service trade requests.
        $.getJSON('/back/task/poll', function(data) { });
    },
    // DOM Events.
    // Lifecycle.
    getInitialState: function() {
        return {
            error: null,
            contrMap: null,
            marketMap: null
        };
    },
    componentDidMount: function() {
        this.refresh();
    },
    render: function() {
        var state = this.state;
        var contrMap = state.contrMap;
        var marketMap = state.marketMap;
        var error = state.error;
        var body = undefined;
        if (error !== null) {
            body = (
                <AlertWidget error={error}/>
            );
        } else if (contrMap !== null) {
            body = (
                <OrderModuleImpl contrMap={contrMap} marketMap={marketMap}
                                 pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="orderModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
