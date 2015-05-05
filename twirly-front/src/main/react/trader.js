/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var TraderModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/api/rec/trader', function(traders) {
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
    newTrader: function(mnem, display, email) {
        console.debug('newTrader: mnem=' + mnem + ', display=' + display + ', email=' + email);
        if (!isSpecified(mnem)) {
            this.onReportError(internalError('mnem not specified'));
            return;
        }
        if (!isSpecified(display)) {
            this.onReportError(internalError('display not specified'));
            return;
        }
        if (!isSpecified(email)) {
            this.onReportError(internalError('email not specified'));
            return;
        }
        $.ajax({
            type: 'post',
            url: '/api/rec/trader/',
            data: JSON.stringify({
                mnem: mnem,
                display: display,
                email: email
            })
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
    onClickNewTrader: function(mnem, display, email) {
        this.newTrader(mnem, display, email);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onClickNewTrader: this.onClickNewTrader
            },
            errors: [],
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
        var traders = state.traders;

        var marginBottom = {
            marginBottom: 16
        };
        return (
            <div className="traderModule">
              <div className="page-header" style={marginBottom}>
                <h3>Traders</h3>
              </div>
              <MultiAlertWidget module={module} errors={errors}/>
              <button type="button" className="btn btn-default"
                      data-toggle="modal" data-target="#newTraderDialog">
                New Trader
              </button>
              <NewTraderDialog module={module}/>
              <TraderTable traders={traders}/>
            </div>
        );
    },
    staging: {
        errors: new Tail(5),
        traders: new Map()
    }
});
