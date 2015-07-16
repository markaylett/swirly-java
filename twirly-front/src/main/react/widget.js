/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

// State is not updated immediately by setState(), so we introduce the
// concept of a staging area to avoid the following race:
//
// 1. state is { arr: [A, B] }.
// 2. onSomeEvent() appends C to state in step #1 using setState();
// 3. onSomeEvent() appends D to state in step #1 using setState();
// 4. deferred setState() completes for step #2;
// 5. deferred setState() completes for step #3;
// 6. state is now { add: [A, B, D] }.

var AlertLine = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var error = this.props.error;
        return (
            <div>
              <span className="glyphicon glyphicon-warning-sign"></span>
              <strong> error {error.num}:</strong> {this.props.error.msg}<br/>
            </div>
        );
    }
});

var AlertWidget = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        return (
            <div className="alertWidget alert alert-warning" role="alert">
              <AlertLine error={props.error}/>
            </div>
        );
    }
});

var MultiAlertWidget = React.createClass({
    // Mutators.
    // DOM Events.
    onClickClose: function() {
        this.props.module.onClearErrors();
    },
    // Lifecycle.
    render: function() {
        var props = this.props;
        var i = 0;
        var lines = props.errors.map(function(module, error) {
            return (
                <AlertLine key={i++} error={error}/>
            );
        }.bind(this, props.module));
        if (i == 0) {
            return null;
        }
        return (
            <div className="multiAlertWidget alert alert-warning alert-dismissible" role="alert">
              <button type="button" className="close" onClick={this.onClickClose}>
                {times}
              </button>
              {lines}
            </div>
        );
    }
});

// Usage: <RecWidget module={module} contrMap={contrMap} pollInterval={60000}/>

var RecWidget = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/back/rec', function(rec) {
            var contrMap = this.props.contrMap;
            rec.assets.forEach(enrichAsset);
            rec.contrs.forEach(enrichContr);
            rec.markets.forEach(enrichMarket.bind(undefined, contrMap));
            rec.traders.forEach(enrichTrader);
            this.setState({
                assets: rec.assets,
                contrs: rec.contrs,
                markets: rec.markets,
                traders: rec.traders
            });
        }.bind(this)).fail(function(xhr, status, err) {
            this.props.module.onReportError(parseError(xhr));
        }.bind(this));
    },
    // DOM Events.
    // Lifecycle.
    getInitialState: function() {
        return {
            assets: [],
            contrs: [],
            markets: [],
            traders: []
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var state = this.state;
        return (
            <div className="recWidget">
              <h1>Rec</h1>
              <h2>Asset</h2>
              <AssetTable assets={state.assets}/>
              <h2>Contr</h2>
              <ContrTable contrs={state.contrs}/>
              <h2>Market</h2>
              <MarketTable markets={state.markets}/>
              <h2>Trader</h2>
              <TraderTable traders={state.traders}/>
            </div>
        );
    }
});

// Usage: <ViewWidget module={module} contrMap={contrMap} pollInterval={5000}/>

var ViewWidget = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/back/view', function(views) {
            var contrMap = this.props.contrMap;
            views.forEach(enrichView.bind(undefined, contrMap));
            this.setState({
                views: views
            });
        }.bind(this)).fail(function(xhr, status, err) {
            this.props.module.onReportError(parseError(xhr));
        }.bind(this));
    },
    // DOM Events.
    // Lifecycle.
    getInitialState: function() {
        return {
            views: []
        };
    },
    componentDidMount: function() {
        this.refresh();
        setInterval(this.refresh, this.props.pollInterval);
    },
    render: function() {
        var module = this.props.module;
        var views = this.state.views;
        return (
            <div className="viewWidget">
              <h1>View</h1>
              <ViewTable module={module} views={views}/>
            </div>
        );
    }
});

var SessWidget = React.createClass({
    // Mutators.
    // DOM Events.
    onWorkingTab: function(event) {
        this.props.module.onChangeContext('working');
    },
    onDoneTab: function(event) {
        this.props.module.onChangeContext('done');
    },
    onTradesTab: function(event) {
        this.props.module.onChangeContext('trades');
    },
    onPosnsTab: function(event) {
        this.props.module.onChangeContext('posns');
    },
    // Lifecycle.
    componentDidMount: function() {
        var node = this.refs.tabs.getDOMNode();
        $(node).tab();
        this.props.module.onChangeContext('working');
    },
    render: function() {
        var props = this.props;
        var module = props.module;
        var sess = props.sess;
        var working = sess.working;
        var done = sess.done;
        var trades = sess.trades;
        var posns = sess.posns;
        return (
            <div className="sessWidget">
              <ul ref="tabs" className="nav nav-tabs" data-tabs="tabs">
                <li className="active">
                  <a href="#working" data-toggle="tab" onClick={this.onWorkingTab}>
                    Working ({working.length})
                  </a>
                </li>
                <li>
                  <a href="#done" data-toggle="tab" onClick={this.onDoneTab}>
                    Done ({done.length})
                  </a>
                </li>
                <li>
                  <a href="#trades" data-toggle="tab" onClick={this.onTradesTab}>
                    Trades ({trades.length})
                  </a>
                </li>
                <li>
                  <a href="#posns" data-toggle="tab" onClick={this.onPosnsTab}>
                    Posns ({posns.length})
                  </a>
                </li>
              </ul>
              <div className="tab-content">
                <div id="working" className="tab-pane active">
                  <OrderTable ref="working" module={module} orders={working}/>
                </div>
                <div id="done" className="tab-pane">
                  <OrderTable ref="done" module={module} orders={done}/>
                </div>
                <div id="trades" className="tab-pane">
                  <TradeTable ref="trades" module={module} trades={trades}/>
                </div>
                <div id="posns" className="tab-pane">
                  <PosnTable posns={posns}/>
                </div>
              </div>
            </div>
        );
    }
});
