/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var TradeModuleImpl = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/api/view', function(views) {
            var contrMap = this.props.contrMap;
            var staging = this.staging;
            var marketMap = {};

            staging.views.clear();
            views.forEach(function(view) {
                enrichView(contrMap, view);
                staging.views.set(view.key, view);
                marketMap[view.market] = view.contr;
            });

            this.setState({
                marketMap: marketMap,
                views: staging.views.toSortedArray()
            });
        }.bind(this)).fail(function(xhr, status, err) {
            this.onReportError(parseError(xhr));
        }.bind(this));

        $.getJSON('/api/sess', function(sess) {
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

            this.setState({
                sess: {
                    working: staging.working.toSortedArray(),
                    done: staging.done.toSortedArray(),
                    trades: staging.trades.toSortedArray(),
                    posns: staging.posns.toSortedArray()
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
    placeOrder: function(market, action, price, lots) {
        console.debug('placeOrder: market=' + market + ', action=' + action
                      + ', price=' + price + ', lots=' + lots);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        var contr = this.state.marketMap[market];
        if (contr === undefined) {
            this.onReportError(internalError('invalid market: ' + market));
            return;
        }
        if (!isSpecified(price)) {
            this.onReportError(internalError('price not specified'));
            return;
        }
        var ticks = priceToTicks(price, contr);
        if (!isSpecified(lots) || lots === 0) {
            this.onReportError(internalError('lots not specified'));
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
        }).done(function(trans) {
            this.applyTrans(trans);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    reviseOrder: function(market, id, lots) {
        console.debug('reviseOrder: market=' + market + ', id=' + id + ', lots=' + lots);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!isSpecified(id)) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        if (!isSpecified(lots)) {
            this.onReportError(internalError('lots not specified'));
            return;
        }
        lots = parseInt(lots);

        $.ajax({
            type: 'put',
            url: '/api/sess/order/' + market + '/' + id,
            data: JSON.stringify({
                lots: lots
            })
        }).done(function(trans) {
            this.applyTrans(trans);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    cancelOrder: function(market, id) {
        console.debug('cancelOrder: market=' + market + ', id=' + id);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!isSpecified(id)) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        $.ajax({
            type: 'put',
            url: '/api/sess/order/' + market + '/' + id,
            data: '{"lots":0}'
        }).done(function(trans) {
            this.applyTrans(trans);
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    archiveOrder: function(market, id) {
        console.debug('archiveOrder: market=' + market + ', id=' + id);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!isSpecified(id)) {
            this.onReportError(internalError('order-id not specified'));
            return;
        }
        var k = market + '/' + zeroPad(id);
        $.ajax({
            type: 'delete',
            url: '/api/sess/order/' + k
        }).done(function(unused) {
            var done = this.staging.done;
            var sess = this.state.sess;
            done.delete(k);
            this.setState({
                sess: {
                    working: sess.working,
                    done: done.toSortedArray(),
                    trades: sess.trades,
                    posns: sess.posns
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    archiveTrade: function(market, id) {
        console.debug('archiveTrade: market=' + market + ', id=' + id);
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        if (!isSpecified(id)) {
            this.onReportError(internalError('trade-id not specified'));
            return;
        }
        var k = market + '/' + zeroPad(id);
        $.ajax({
            type: 'delete',
            url: '/api/sess/trade/' + k
        }).done(function(unused) {
            var trades = this.staging.trades;
            var sess = this.state.sess;
            trades.delete(k);
            this.setState({
                sess: {
                    working: sess.working,
                    done: sess.done,
                    trades: trades.toSortedArray(),
                    posns: sess.posns
                }
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    applyTrans: function(trans) {
        var contrMap = this.props.contrMap;
        var staging = this.staging;

        var views = staging.views;
        var working = staging.working;
        var done = staging.done;
        var trades = staging.trades;
        var posns = staging.posns;

        var view = trans.view;
        if (view !== null) {
            enrichView(contrMap, view);
            views.set(view.key, view);
        }
        trans.orders.forEach(function(order) {
            enrichOrder(contrMap, order);
            if (order.isDone) {
                working.delete(order.key);
                done.set(order.key, order);
            } else {
                working.set(order.key, order);
            }
        });
        trans.execs.forEach(function(exec) {
            if (exec.state === 'TRADE') {
                enrichTrade(contrMap, exec);
                trades.set(exec.key, exec);
            }
        });
        var posn = trans.posn;
        if (posn !== null) {
            enrichPosn(contrMap, posn);
            staging.posns.set(posn.key, posn);
        }
        this.setState({
            views: views.toSortedArray(),
            sess: {
                working: working.toSortedArray(),
                done: done.toSortedArray(),
                trades: trades.toSortedArray(),
                posns: posns.toSortedArray()
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
    onClickRefresh: function() {
        console.debug('onClickRefresh');
        this.refresh();
    },
    onChangeContext: function(context) {
        console.debug('onChangeContext: context=' + context);
        this.staging.context = context;
        this.updateSelected();
    },
    onClickItem: function(market, price, lots) {
        console.debug('onClickItem: market=' + market + ', price=' + price + ', lots=' + lots);
        this.refs.newOrder.setItem(market, price, lots);
        this.refs.reviseOrder.setItem(market, price, lots);
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
    onClickPlace: function(market, action, price, lots) {
        this.placeOrder(market, action, price, lots);
    },
    onClickRevise: function(lots) {
        if (this.staging.context === 'working') {
            this.staging.selectedWorking.forEach(function(key, order) {
                this.reviseOrder(order.market, order.id, lots);
            }.bind(this));
        }
    },
    onClickCancel: function() {
        if (this.staging.context === 'working') {
            this.staging.selectedWorking.forEach(function(key, order) {
                this.cancelOrder(order.market, order.id);
            }.bind(this));
        }
    },
    onClickArchive: function() {
        if (this.staging.context === 'done') {
            this.staging.selectedDone.forEach(function(key, order) {
                this.archiveOrder(order.market, order.id);
            }.bind(this));
        } else if (this.staging.context === 'trades') {
            this.staging.selectedTrades.forEach(function(key, trade) {
                this.archiveTrade(trade.market, trade.id);
            }.bind(this));
        }
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onClickRefresh: this.onClickRefresh,
                onChangeContext: this.onChangeContext,
                onClickItem: this.onClickItem,
                onSelectOrder: this.onSelectOrder,
                onSelectTrade: this.onSelectTrade,
                onClickPlace: this.onClickPlace,
                onClickRevise: this.onClickRevise,
                onClickCancel: this.onClickCancel,
                onClickArchive: this.onClickArchive
            },
            errors: [],
            marketMap: {},
            views: [],
            sess: {
                working: [],
                done: [],
                trades: [],
                posns: []
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
        var contrMap = props.contrMap;

        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        var marketMap = state.marketMap;
        var views = state.views;
        var sess = state.sess;
        var isSelectedWorking = state.isSelectedWorking;
        var isSelectedDone = state.isSelectedDone;
        var isSelectedTrade = state.isSelectedTrade;
        var isSelectedArchivable = state.isSelectedArchivable;

        var marginTop = {
            marginTop: 24
        };

        return (
            <div className="tradeModuleImpl">
              <MultiAlertWidget module={module} errors={errors}/>
              <NewOrderForm ref="newOrder" module={module} marketMap={marketMap}
                            isSelectedWorking={isSelectedWorking}/>
              <div style={marginTop}>
                <ViewTable module={module} views={views}/>
              </div>
              <div style={marginTop}>
                <ReviseOrderForm ref="reviseOrder" module={module} marketMap={marketMap}
                                 isSelectedArchivable={isSelectedArchivable}
                                 isSelectedWorking={isSelectedWorking}/>
              </div>
              <div style={marginTop}>
                <SessWidget module={module} sess={sess}/>
              </div>
            </div>
        );
    },
    staging: {
        errors: new Tail(5),
        views: new Map(),
        working: new Map(),
        done: new Map(),
        trades: new Map(),
        posns: new Map(),
        selectedWorking: new Map(),
        selectedDone: new Map(),
        selectedTrades: new Map()
    }
});

var TradeModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/api/rec/contr', function(contrs) {
            var contrMap = {};
            contrs.forEach(function(contr) {
                enrichContr(contr);
                contrMap[contr.mnem] = contr;
            });
            this.setState({
                contrMap: contrMap
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
    },
    // DOM Events.
    // Lifecycle.
    getInitialState: function() {
        return {
            error: null,
            contrMap: null
        };
    },
    componentDidMount: function() {
        this.refresh();
    },
    render: function() {
        var state = this.state;
        var contrMap = state.contrMap;
        var error = state.error;
        var body = undefined;
        if (error !== null) {
            body = (
                <AlertWidget error={error}/>
            );
        } else if (contrMap !== null) {
            body = (
                <TradeModuleImpl contrMap={contrMap} pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="tradeModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
