/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var MarketModuleImpl = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/api/rec/market', function(markets) {
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            staging.markets.clear();
            markets.forEach(function(market) {
                enrichMarket(contrMap, market);
                staging.markets.set(market.key, market);
            });

            this.setState({
                markets: staging.markets.toSortedArray()
            });
        }.bind(this)).fail(function(xhr) {
            this.setState({
                error: parseError(xhr)
            });
        }.bind(this));
    },
    newMarket: function(mnem, display, contr, settlDate, expiryDate, state) {
        console.debug('newMarket: mnem=' + mnem + ', display=' + display + ', contr='
                      + contr + ', settlDate=' + settlDate + ', expiryDate='
                      + expiryDate + ', state=' + state);
        if (!isSpecified(mnem)) {
            this.onReportError(internalError('mnem not specified'));
            return;
        }
        if (!isSpecified(display)) {
            this.onReportError(internalError('display not specified'));
            return;
        }
        if (!isSpecified(contr)) {
            this.onReportError(internalError('contr not specified'));
            return;
        }
        if (!isSpecified(settlDate)) {
            this.onReportError(internalError('settlDate not specified'));
            return;
        }
        settlDate = toDateInt(settlDate);
        if (!isSpecified(expiryDate)) {
            this.onReportError(internalError('expiryDate not specified'));
            return;
        }
        expiryDate = toDateInt(expiryDate);
        if (!isSpecified(state)) {
            this.onReportError(internalError('state not specified'));
            return;
        }
        state = parseInt(state);
        $.ajax({
            type: 'post',
            url: '/api/rec/market/',
            data: JSON.stringify({
                mnem: mnem,
                display: display,
                contr: contr,
                settlDate: settlDate,
                expiryDate: expiryDate,
                state: state
            })
        }).done(function(market) {
            var contrMap = this.props.contrMap;
            var staging = this.staging;

            enrichMarket(contrMap, market);
            staging.markets.set(market.key, market);

            this.setState({
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
    onClickNewMarket: function(mnem, display, contr, settlDate, expiryDate, state) {
        this.newMarket(mnem, display, contr, settlDate, expiryDate, state);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onClickNewMarket: this.onClickNewMarket
            },
            errors: [],
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
              <button type="button" className="btn btn-default"
                      data-toggle="modal" data-target="#newMarketDialog">
                New Market
              </button>
              <NewMarketDialog module={module} contrMap={contrMap}/>
              <MarketTable markets={markets}/>
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