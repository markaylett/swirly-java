/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var TraderModuleImpl = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/back/rec/market', function(markets) {
            var contrMap = this.props.contrMap;
            var marketMap = {};

            markets.forEach(function(market) {
                enrichMarket(contrMap, market);
                marketMap[market.key] = market.contr;
            });

            this.setState({
                marketMap: marketMap
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
        $.getJSON('/back/rec/trader', function(traders) {
            var staging = this.staging;

            staging.traders.clear();
            traders.forEach(function(trader) {
                enrichTrader(trader);
                staging.traders.set(trader.key, trader);
            });

            this.setState({
                traders: staging.traders.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
    },
    postTrader: function(mnem, display, email) {
        console.debug('postTrader: mnem=' + mnem + ', display=' + display + ', email=' + email);
        var req = {};
        if (isSpecified(mnem)) {
            req.mnem = mnem;
        } else {
            this.onReportError(internalError('mnem not specified'));
            return;
        }
        if (isSpecified(display)) {
            req.display = display;
        } else {
            this.onReportError(internalError('display not specified'));
            return;
        }
        if (isSpecified(email)) {
            req.email = email;
        } else {
            this.onReportError(internalError('email not specified'));
            return;
        }

        $.ajax({
            type: 'post',
            url: '/back/rec/trader/',
            data: JSON.stringify(req)
        }).done(function(trader) {
            var staging = this.staging;

            enrichTrader(trader);
            staging.traders.set(trader.key, trader);

            this.setState({
                traders: staging.traders.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    putTrader: function(mnem, display, email) {
        console.debug('putTrader: mnem=' + mnem + ', display=' + display + ', email=' + email);
        var req = {};
        if (isSpecified(mnem)) {
            req.mnem = mnem;
        } else {
            this.onReportError(internalError('mnem not specified'));
            return;
        }
        if (isSpecified(display)) {
            req.display = display;
        } else {
            this.onReportError(internalError('display not specified'));
            return;
        }
        if (isSpecified(email)) {
            req.email = email;
        } else {
            this.onReportError(internalError('email not specified'));
            return;
        }

        $.ajax({
            type: 'put',
            url: '/back/rec/trader/',
            data: JSON.stringify(req)
        }).done(function(trader) {
            var staging = this.staging;

            enrichTrader(trader);
            staging.traders.set(trader.key, trader);

            this.setState({
                traders: staging.traders.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    postTrade: function(trader, market, ref, action, price, lots, role, cpty) {
        console.debug('postTrade: trader=' + trader + ', market=' + market
                      + ', ref=' + ref + ', action=' + action + ', price=' + price
                      + ', lots=' + lots + ', role=' + role + ', cpty=' + cpty);
        var req = {};
        if (isSpecified(trader)) {
            req.trader = trader;
        } else {
            this.onReportError(internalError('trader not specified'));
            return;
        }
        if (!isSpecified(market)) {
            this.onReportError(internalError('market not specified'));
            return;
        }
        var contr = this.state.marketMap[market];
        if (contr === undefined) {
            this.onReportError(internalError('invalid market: ' + market));
            return;
        }
        if (isSpecified(ref)) {
            req.ref = ref;
        }
        if (isSpecified(action)) {
            req.action = action;
        } else {
            this.onReportError(internalError('action not specified'));
            return;
        }
        if (isSpecified(price)) {
            req.ticks = priceToTicks(price, contr);
        } else {
            this.onReportError(internalError('price not specified'));
            return;
        }
        if (isSpecified(lots) && lots > 0) {
            req.lots = parseInt(lots);
        } else {
            this.onReportError(internalError('lots not specified'));
            return;
        }
        if (isSpecified(role)) {
            req.role = role;
        }
        if (isSpecified(cpty)) {
            req.cpty = cpty;
        }

        $.ajax({
            type: 'post',
            url: '/back/sess/trade/' + market,
            data: JSON.stringify(req)
        }).done(function(market) {
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
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
    onPostTrader: function(mnem, display, email) {
        this.postTrader(mnem, display, email);
    },
    onPutTrader: function(mnem, display, email) {
        this.putTrader(mnem, display, email);
    },
    onPostTrade: function(trader, market, ref, action, price, lots, role, cpty) {
        this.postTrade(trader, market, ref, action, price, lots, role, cpty);
    },
    onEditTrader: function(trader) {
        console.debug('onEditTrader: mnem=' + trader.mnem);
        var ref = this.refs.traderDialog;
        var node = ref.getDOMNode();
        ref.setTrader(trader);
        $(node).modal('show');
    },
    onNewTrade: function(trader) {
        console.debug('onNewTrade: mnem=' + trader.mnem);
        var ref = this.refs.tradeDialog;
        var node = ref.getDOMNode();
        ref.setTrader(trader);
        $(node).modal('show');
    },
    onNewTransfer: function(trader) {
        console.debug('onNewTransfer: mnem=' + trader.mnem);
        var ref = this.refs.transferDialog;
        var node = ref.getDOMNode();
        ref.setTrader(trader);
        $(node).modal('show');
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onPostTrader: this.onPostTrader,
                onPutTrader: this.onPutTrader,
                onPostTrade: this.onPostTrade,
                onEditTrader: this.onEditTrader,
                onNewTrade: this.onNewTrade,
                onNewTransfer: this.onNewTransfer
            },
            errors: [],
            marketMap: {},
            traders: []
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        var marketMap = state.marketMap;
        var traders = state.traders;

        var marginBottom = {
            marginBottom: 16
        };
        return (
            <div className="traderModuleImpl">
              <div className="page-header" style={marginBottom}>
                <h3>Traders</h3>
              </div>
              <MultiAlertWidget module={module} errors={errors}/>
              <div className="btn-group">
                <button type="button" className="btn btn-default"
                        data-toggle="modal" data-target="#traderDialog">
                  New Trader
                </button>
              </div>
              <TraderDialog ref="traderDialog" module={module}/>
              <TradeDialog ref="tradeDialog" module={module} marketMap={marketMap}/>
              <TransferDialog ref="transferDialog" module={module} marketMap={marketMap}/>
              <TraderTable module={module} traders={traders}/>
            </div>
        );
    },
    staging: {
        errors: new Tail(5),
        traders: new Map()
    }
});

var TraderModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/back/rec/contr', function(contrs) {
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
                <TraderModuleImpl contrMap={contrMap} pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="traderModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
