/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var MarketModuleImpl = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/api/rec/market', function(markets) {
            var contrMap = this.props.contrMap;
            var staging = this.staging;
            var marketMap = {};

            staging.markets.clear();
            markets.forEach(function(market) {
                enrichMarket(contrMap, market);
                marketMap[market.key] = market.contr;
                staging.markets.set(market.key, market);
            });

            this.setState({
                marketMap: marketMap,
                markets: staging.markets.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
    },
    postMarket: function(mnem, display, contr, settlDate, expiryDate, state) {
        console.debug('postMarket: mnem=' + mnem + ', display=' + display + ', contr='
                      + contr + ', settlDate=' + settlDate + ', expiryDate='
                      + expiryDate + ', state=' + state);
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
        if (isSpecified(contr)) {
            req.contr = contr;
        } else {
            this.onReportError(internalError('contr not specified'));
            return;
        }
        if (isSpecified(settlDate)) {
            req.settlDate = toDateInt(settlDate);
        }
        if (isSpecified(expiryDate)) {
            req.expiryDate = toDateInt(expiryDate);
        }
        if (isSpecified(state)) {
            req.state = parseInt(state);
        }
        $.ajax({
            type: 'post',
            url: '/api/rec/market/',
            data: JSON.stringify(req)
        }).done(function(market) {
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            enrichMarket(contrMap, market);
            staging.markets.set(market.key, market);

            marketMap = {};
            staging.markets.forEach(function(key, market) {
                marketMap[market.key] = market.contr;
            });

            this.setState({
                marketMap: marketMap,
                markets: staging.markets.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.onReportError(parseError(xhr));
        }.bind(this));
    },
    putMarket: function(mnem, display, state) {
        console.debug('putMarket: mnem=' + mnem + ', display=' + display + ', state=' + state);
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
        if (isSpecified(state)) {
            req.state = parseInt(state);
        } else {
            this.onReportError(internalError('state not specified'));
            return;
        }
        $.ajax({
            type: 'put',
            url: '/api/rec/market/',
            data: JSON.stringify(req)
        }).done(function(market) {
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            enrichMarket(contrMap, market);
            staging.markets.set(market.key, market);

            marketMap = {};
            staging.markets.forEach(function(key, market) {
                marketMap[market.key] = market.contr;
            });

            this.setState({
                marketMap: marketMap,
                markets: staging.markets.toSortedArray()
            });
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
    onPostMarket: function(mnem, display, contr, settlDate, expiryDate, state) {
        this.postMarket(mnem, display, contr, settlDate, expiryDate, state);
    },
    onPutMarket: function(mnem, display, state) {
        this.putMarket(mnem, display, state);
    },
    onEditMarket: function(market) {
        console.debug('onEditMarket: mnem=' + market.mnem);
        var ref = this.refs.marketDialog;
        var node = ref.getDOMNode();
        ref.setMarket(market);
        $(node).modal('show');
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onPostMarket: this.onPostMarket,
                onPutMarket: this.onPutMarket,
                onEditMarket: this.onEditMarket
            },
            errors: [],
            marketMap: {},
            markets: []
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var contrMap = this.props.contrMap;
        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        var marketMap = state.marketMap;
        var markets = state.markets;

        var marginBottom = {
            marginBottom: 16
        };
        return (
            <div className="marketModuleImpl">
              <div className="page-header" style={marginBottom}>
                <h3>Markets</h3>
              </div>
              <MultiAlertWidget module={module} errors={errors}/>
              <div className="btn-group">
                <button type="button" className="btn btn-default"
                        data-toggle="modal" data-target="#marketDialog">
                  New Market
                </button>
              </div>
              <MarketDialog ref="marketDialog" module={module} contrMap={contrMap}/>
              <MarketTable module={module} markets={markets}/>
            </div>
        );
    },
    staging: {
        errors: new Tail(5),
        markets: new Map()
    }
});

var MarketModule = React.createClass({
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
                <MarketModuleImpl contrMap={contrMap} pollInterval={this.props.pollInterval}/>
            );
        }
        if (body !== undefined) {
            return (
                <div className="marketModule">
                  {body}
                </div>
            );
        }
        return null;
    }
});
