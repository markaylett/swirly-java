/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var QuoteModuleImpl = React.createClass({
    // Mutators.
    refresh: function(url) {
        if (url === undefined) {
            url = '/front/sess/quote,trade,posn,view';
        }
        $.getJSON(url, function(sess, status, xhr) {
            this.resetTimeout(xhr);
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            staging.quotes.clear();
            sess.quotes.forEach(function(quote) {
                enrichQuote(contrMap, quote);
                staging.quotes.set(quote.key, quote);
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
                    quotes: staging.quotes.toSortedArray(),
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
        var isSelectedTrade = false;
        var isSelectedArchivable = false;
        if (this.staging.context === 'trades') {
            if (!this.staging.selectedTrades.isEmpty()) {
                isSelectedTrade = true;
                isSelectedArchivable = true;
            }
        }
        this.setState({
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
    postQuote: function(market, side, lots) {
        console.debug('postQuote: market=' + market + ', side=' + side
                       + ', lots=' + lots);
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

        $.ajax({
            type: 'post',
            url: '/back/sess/quote/' + market,
            data: JSON.stringify(req)
        }).done(function(quote, status, xhr) {
            this.resetTimeout(xhr);
            var contrMap = this.props.contrMap;
            var quotes = this.staging.quotes;
            var sess = this.state.sess;
            enrichQuote(contrMap, quote);
            quotes.set(quote.key, quote);
            this.setState({
                sess: {
                    quotes: quotes.toSortedArray(),
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
                    quotes: sess.quotes,
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

        var quotes = staging.quotes;
        var trades = staging.trades;
        var posns = staging.posns;
        var views = staging.views;

        result.execs.forEach(function(exec) {
            if (exec.state === 'TRADE') {
                enrichTrade(contrMap, exec);
                trades.set(exec.key, exec);
                if (exec.quoteId !== 0) {
                    var key = exec.market + '/' + zeroPad(exec.quoteId);
                    quotes.delete(key);
                }
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
                quotes: quotes.toSortedArray(),
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
        this.refs.newQuote.setFields(market, lots);
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
    onPostQuote: function(market, side, lots) {
        this.postQuote(market, side, lots);
    },
    onArchiveAll: function() {
        if (this.staging.context === 'trades') {
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
                onSelectTrade: this.onSelectTrade,
                onPostOrder: this.onPostOrder,
                onPostQuote: this.onPostQuote,
                onArchiveAll: this.onArchiveAll
            },
            errors: [],
            sess: {
                quotes: [],
                trades: [],
                posns: [],
                views: []
            },
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
        var isSelectedTrade = state.isSelectedTrade;
        var isSelectedArchivable = state.isSelectedArchivable;

        var marginTop = {
            marginTop: 24
        };

        return (
            <div className="quoteModule">
              <MultiAlertWidget module={module} errors={errors}/>
              <NewQuoteForm ref="newQuote" module={module} marketMap={marketMap}/>
              <div style={marginTop}>
                <ViewTable module={module} views={sess.views}/>
              </div>
              <div style={marginTop}>
                <ArchiveForm ref="archive" module={module} marketMap={marketMap}
                                 isSelectedArchivable={isSelectedArchivable}/>
              </div>
              <div style={marginTop}>
                <QuoteWidget module={module} sess={sess}/>
              </div>
            </div>
        );
    },
    resetTimeout: function(xhr) {
        var timeout = parseInt(xhr.getResponseHeader('Swirly-Timeout'));
        clearTimeout(this.timeout);
        if (timeout !== 0) {
            console.debug('timeout set for ' + new Date(timeout));
            var delta = timeout - Date.now();
            this.timeout = setTimeout(function() {
                console.debug('timeout now at ' + new Date(timeout));
                this.refresh('/back/sess/quote,trade,posn,view');
            }.bind(this), delta);
        } else {
            console.debug('timeout not set');
        }
    },
    staging: {
        errors: new Tail(5),
        quotes: new Map(),
        trades: new Map(),
        posns: new Map(),
        views: new Map(),
        selectedTrades: new Map()
    },
    timeout: undefined
});

var QuoteModule = React.createClass({
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
                <QuoteModuleImpl contrMap={contrMap} marketMap={marketMap}
                                 pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="quoteModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
