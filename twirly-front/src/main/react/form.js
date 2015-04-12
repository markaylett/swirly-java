/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var SignupForm = React.createClass({
    // Mutators.
    // DOM Events.
    onChangeMnem: function(event) {
        this.setState({
            mnem: event.target.value
        });
    },
    onChangeDisplay: function(event) {
        this.setState({
            display: event.target.value
        });
    },
    onClickSignup: function(event) {
        event.preventDefault();
        var state = this.state;
        var mnem = state.mnem;
        var display = state.display;
        this.props.module.onClickSignup(mnem, display);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            mnem: undefined,
            display: undefined
        };
    },
    render: function() {
        var state = this.state;
        var mnem = state.mnem;
        var display = state.display;
        return (
            <form className="signupForm">
              <h3>Sign up for a trading account</h3>
              <div className="form-group">
                <label htmlFor="mnem">Trader name:</label>
                <input id="mnem" type="text" className="form-control" value={mnem}
                       onChange={this.onChangeMnem}/>
              </div>
              <div className="form-group">
                <label htmlFor="display">Full name:</label>
                <input id="display" type="text" className="form-control" value={display}
                       onChange={this.onChangeDisplay}/>
              </div>
                <button type="button" className="btn btn-lg btn-primary btn-block"
                        onClick={this.onClickSignup}>Sign up</button>
            </form>
        );
    }
});

var NewOrderForm = React.createClass({
    // Mutators.
    setItem: function(market, price, lots) {
        var newState = {};
        if (market !== undefined) {
            newState.market = market;
        }
        if (price !== undefined) {
            newState.price = price;
        }
        if (lots !== undefined) {
            newState.lots = lots;
        }
        this.setState(newState);
    },
    // DOM Events.
    onChangeMarket: function(event) {
        this.setState({
            market: event.target.value
        });
    },
    onChangePrice: function(event) {
        this.setState({
            price: event.target.value
        });
    },
    onChangeLots: function(event) {
        this.setState({
            lots: event.target.value
        });
    },
    onClickBuy: function(event) {
        event.preventDefault();
        var state = this.state;
        var market = state.market;
        var price = state.price;
        var lots = state.lots;
        this.props.module.onClickPlace(market, 'BUY', price, lots);
    },
    onClickSell: function(event) {
        event.preventDefault();
        var state = this.state;
        var market = state.market;
        var price = state.price;
        var lots = state.lots;
        this.props.module.onClickPlace(market, 'SELL', price, lots);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            market: undefined,
            price: undefined,
            lots: undefined
        };
    },
    componentDidMount: function() {
        var node = this.refs.market.getDOMNode();
        $(node).typeahead({
            items: 4,
            source: function(query, process) {
                var marketKeys = Object.keys(this.props.marketMap);
                process(marketKeys);
            }.bind(this),
            updater: function(value) {
                this.setState({
                    market: value
                });
                return value;
            }.bind(this)
        });
    },
    render: function() {
        var state = this.state;
        var contr = this.props.marketMap[state.market];

        var priceInc = 0.01;
        var minLots = 1;
        if (contr !== undefined) {
            priceInc = contr.priceInc;
            minLots = contr.minLots;
        }
        var isSelectedWorking = this.props.isSelectedWorking;
        return (
            <form className="newOrderForm form-inline">
              <div className="form-group">
                <input ref="market" type="text" className="form-control" placeholder="Enter market"
                       value={state.market} disabled={isSelectedWorking} onChange={this.onChangeMarket}/>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Enter price"
                       value={state.price} disabled={isSelectedWorking} onChange={this.onChangePrice}
                       step={priceInc}/>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Enter lots"
                       value={state.lots} disabled={isSelectedWorking} onChange={this.onChangeLots}
                       min={minLots}/>
              </div>
              <button type="button" className="btn btn-default"
                      disabled={isSelectedWorking} onClick={this.onClickBuy}>
                <span className="glyphicon glyphicon-plus"></span>
                Buy
              </button>
              <button type="button" className="btn btn-default"
                      disabled={isSelectedWorking} onClick={this.onClickSell}>
                <span className="glyphicon glyphicon-minus"></span>
                Sell
              </button>
            </form>
        );
    }
});

var ReviseOrderForm = React.createClass({
    // Mutators.
    setItem: function(market, price, lots) {
        var newState = {};
        if (market !== undefined) {
            newState.market = market;
        }
        if (lots !== undefined) {
            newState.lots = lots;
        }
        this.setState(newState);
    },
    // DOM Events.
    onChangeLots: function(event) {
        this.setState({
            lots: event.target.value
        });
    },
    onClickCancel: function(event) {
        event.preventDefault();
        this.props.module.onClickCancel();
    },
    onClickArchive: function(event) {
        event.preventDefault();
        this.props.module.onClickArchive();
    },
    onClickRefresh: function(event) {
        event.preventDefault();
        this.props.module.onClickRefresh();
    },
    onClickRevise: function(event) {
        event.preventDefault();
        this.props.module.onClickRevise(this.state.lots);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            market: undefined,
            lots: undefined
        };
    },
    render: function() {
        var state = this.state;
        var contr = this.props.marketMap[state.market];

        var minLots = 1;
        if (contr !== undefined) {
            minLots = contr.minLots;
        }
        var isSelectedWorking = this.props.isSelectedWorking;
        var isSelectedArchivable = this.props.isSelectedArchivable;
        return (
            <form className="reviseOrderForm form-inline">
              <div className="btn-group">
                <button type="button" className="btn btn-default"
                        disabled={!isSelectedWorking} onClick={this.onClickCancel}>
                  <span className="glyphicon glyphicon-remove"></span>
                  Cancel
                </button>
                <button type="button" className="btn btn-default"
                        disabled={!isSelectedArchivable} onClick={this.onClickArchive}>
                  <span className="glyphicon glyphicon-ok"></span>
                  Archive
                </button>
                <button type="button" className="btn btn-default"
                        onClick={this.onClickRefresh}>
                  <span className="glyphicon glyphicon-refresh"></span>
                  Refresh
                </button>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Enter lots"
                       value={state.lots} disabled={!isSelectedWorking} onChange={this.onChangeLots}
                       min={minLots}/>
              </div>
              <button type="button" className="btn btn-default"
                      disabled={!isSelectedWorking} onClick={this.onClickRevise}>
                <span className="glyphicon glyphicon-pencil"></span>
                Revise
              </button>
            </form>
        );
    }
});
