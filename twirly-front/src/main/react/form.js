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
        this.props.module.onPostTrader(mnem, display);
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
            <form className="signupForm form">
              <h3>Sign up for a trading account</h3>
              <div className="form-group">
                <label htmlFor="mnem" className="control-label">Trader name:</label>
                <input id="mnem" type="text" className="form-control" value={mnem}
                       onChange={this.onChangeMnem}/>
              </div>
              <div className="form-group">
                <label htmlFor="display" className="control-label">Full name:</label>
                <input id="display" type="text" className="form-control" value={display}
                       onChange={this.onChangeDisplay}/>
              </div>
              <div className="btn-group">
                <button type="button" className="btn btn-lg btn-primary btn-block"
                        onClick={this.onClickSignup}>Sign up</button>
              </div>
            </form>
        );
    }
});

var NewOrderForm = React.createClass({
    // Mutators.
    setFields: function(market, lots, price) {
        var newState = {};
        if (market !== undefined) {
            newState.market = market;
        }
        if (lots !== undefined) {
            newState.lots = lots;
        }
        if (price !== undefined) {
            newState.price = price;
        }
        this.setState(newState);
    },
    // DOM Events.
    onChangeMarket: function(event) {
        this.setState({
            market: event.target.value
        });
    },
    onChangeLots: function(event) {
        this.setState({
            lots: event.target.value
        });
    },
    onChangePrice: function(event) {
        this.setState({
            price: event.target.value
        });
    },
    onClickBuy: function(event) {
        event.preventDefault();
        var state = this.state;
        var market = state.market;
        var lots = state.lots;
        var price = state.price;
        this.props.module.onPostOrder(market, 'BUY', lots, price);
    },
    onClickSell: function(event) {
        event.preventDefault();
        var state = this.state;
        var market = state.market;
        var lots = state.lots;
        var price = state.price;
        this.props.module.onPostOrder(market, 'SELL', lots, price);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            market: undefined,
            lots: undefined,
            price: undefined
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
        var market = state.market;
        var lots = state.lots;
        var price = state.price;

        var contr = this.props.marketMap[market];
        var minLots = 1;
        var priceInc = 0.01;
        if (contr !== undefined) {
            minLots = contr.minLots;
            priceInc = contr.priceInc;
        }
        var isSelectedWorking = this.props.isSelectedWorking;
        return (
            <form className="newOrderForm form-inline">
              <div className="form-group">
                <input ref="market" type="text" className="form-control" placeholder="Market"
                       value={market} disabled={isSelectedWorking}
                       onChange={this.onChangeMarket}/>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Lots"
                       value={lots} disabled={isSelectedWorking}
                       onChange={this.onChangeLots} min={minLots}/>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Price"
                       value={price} disabled={isSelectedWorking}
                       onChange={this.onChangePrice} step={priceInc}/>
              </div>
              <div className="btn-group">
                <button type="button" className="btn btn-default"
                        disabled={isSelectedWorking} onClick={this.onClickBuy}>
                  <span className="glyphicon glyphicon-plus"></span> Buy
                </button>
                <button type="button" className="btn btn-default"
                        disabled={isSelectedWorking} onClick={this.onClickSell}>
                  <span className="glyphicon glyphicon-minus"></span> Sell
                </button>
              </div>
            </form>
        );
    }
});

var NewQuoteForm = React.createClass({
    // Mutators.
    setFields: function(market, lots) {
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
    onChangeMarket: function(event) {
        this.setState({
            market: event.target.value
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
        var lots = state.lots;
        this.props.module.onPostQuote(market, 'BUY', lots);
    },
    onClickSell: function(event) {
        event.preventDefault();
        var state = this.state;
        var market = state.market;
        var lots = state.lots;
        this.props.module.onPostQuote(market, 'SELL', lots);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            market: undefined,
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
        var market = state.market;
        var lots = state.lots;

        var contr = this.props.marketMap[market];
        var minLots = 1;
        if (contr !== undefined) {
            minLots = contr.minLots;
        }
        return (
            <form className="newQuoteForm form-inline">
              <div className="form-group">
                <input ref="market" type="text" className="form-control" placeholder="Market"
                       value={market} onChange={this.onChangeMarket}/>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Lots"
                       value={lots} onChange={this.onChangeLots} min={minLots}/>
              </div>
              <div className="btn-group">
                <button type="button" className="btn btn-default"
                        onClick={this.onClickBuy}>
                  <span className="glyphicon glyphicon-plus"></span> Buy
                </button>
                <button type="button" className="btn btn-default"
                        onClick={this.onClickSell}>
                  <span className="glyphicon glyphicon-minus"></span> Sell
                </button>
              </div>
            </form>
        );
    }
});

var ReviseOrderForm = React.createClass({
    // Mutators.
    setFields: function(market, lots, price) {
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
        this.props.module.onCancelAll();
    },
    onClickArchive: function(event) {
        event.preventDefault();
        this.props.module.onArchiveAll();
    },
    onClickRefresh: function(event) {
        event.preventDefault();
        this.props.module.onRefresh();
    },
    onClickRevise: function(event) {
        event.preventDefault();
        this.props.module.onReviseAll(this.state.lots);
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
        var market = state.market;
        var lots = state.lots;

        var contr = this.props.marketMap[market];
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
                  <span className="glyphicon glyphicon-remove"></span> Cancel
                </button>
                <button type="button" className="btn btn-default"
                        disabled={!isSelectedArchivable} onClick={this.onClickArchive}>
                  <span className="glyphicon glyphicon-ok"></span> Archive
                </button>
                <button type="button" className="btn btn-default"
                        onClick={this.onClickRefresh}>
                  <span className="glyphicon glyphicon-refresh"></span> Refresh
                </button>
              </div>
              <div className="form-group">
                <input type="number" className="form-control" placeholder="Lots"
                       value={lots} disabled={!isSelectedWorking} onChange={this.onChangeLots}
                       min={minLots}/>
              </div>
              <div className="btn-group">
                <button type="button" className="btn btn-default"
                        disabled={!isSelectedWorking} onClick={this.onClickRevise}>
                  <span className="glyphicon glyphicon-pencil"></span> Revise
                </button>
              </div>
            </form>
        );
    }
});
