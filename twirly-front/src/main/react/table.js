/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var alignRight = {
    textAlign: 'right'
};

var AssetRow = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var asset = this.props.asset;
        return (
            <tr>
              <td>{asset.mnem}</td>
              <td>{asset.display}</td>
              <td>{asset.type}</td>
            </tr>
        );
    }
});

var AssetTable = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        var rows = props.assets.map(function(asset) {
            return (
                <AssetRow key={asset.key} asset={asset}/>
            );
        });
        return (
            <table className="assetTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>Mnem</th>
                  <th>Display</th>
                  <th>Type</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var ContrRow = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var contr = this.props.contr;
        return (
            <tr>
              <td>{contr.mnem}</td>
              <td>{contr.display}</td>
              <td>{contr.asset}</td>
              <td>{contr.ccy}</td>
              <td style={alignRight}>{contr.lotNumer}</td>
              <td style={alignRight}>{contr.lotDenom}</td>
              <td style={alignRight}>{contr.tickNumer}</td>
              <td style={alignRight}>{contr.tickDenom}</td>
              <td style={alignRight}>{contr.pipDp}</td>
              <td style={alignRight}>{contr.minLots}</td>
              <td style={alignRight}>{contr.maxLots}</td>
            </tr>
        );
    }
});

var ContrTable = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        var rows = props.contrs.map(function(contr) {
            return (
                <ContrRow key={contr.key} contr={contr}/>
            );
        });
        return (
            <table className="contrTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>Mnem</th>
                  <th>Display</th>
                  <th>Asset</th>
                  <th>Ccy</th>
                  <th style={alignRight}>Lot Numer</th>
                  <th style={alignRight}>Lot Denom</th>
                  <th style={alignRight}>Tick Numer</th>
                  <th style={alignRight}>Tick Denom</th>
                  <th style={alignRight}>Pip Dp</th>
                  <th style={alignRight}>Min Lots</th>
                  <th style={alignRight}>Max Lots</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var MarketRow = React.createClass({
    // Mutators.
    // DOM Events.
    onClickEditMarket: function(event) {
        var props = this.props;
        props.module.onEditMarket(props.market);
    },
    // Lifecycle.
    render: function() {
        var props = this.props;
        var market = props.market;
        return (
            <tr>
              <td>{market.mnem}</td>
              <td>{market.display}</td>
              <td>{market.contr.mnem}</td>
              <td>{optional(market.settlDate)}</td>
              <td>{optional(market.expiryDate)}</td>
              <td>{market.state}</td>
              <td>
                <div className="btn-group btn-group-xs" role="group">
                  <button type="button" className="btn btn-default"
                          onClick={this.onClickEditMarket}>
                    <span className="glyphicon glyphicon-pencil"></span>
                  </button>
                </div>
              </td>
            </tr>
        );
    }
});

var MarketTable = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        var module = props.module;
        var rows = props.markets.map(function(module, market) {
            return (
                <MarketRow key={market.key} market={market}
                           module={module}/>
            );
        }.bind(this, module));
        return (
            <table className="marketTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>Mnem</th>
                  <th>Display</th>
                  <th>Contr</th>
                  <th>Settl Date</th>
                  <th>Expiry Date</th>
                  <th>State</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var TraderRow = React.createClass({
    // Mutators.
    // DOM Events.
    onClickEditTrader: function(event) {
        var props = this.props;
        props.module.onEditTrader(props.trader);
    },
    onClickNewTrade: function(event) {
        var props = this.props;
        props.module.onNewTrade(props.trader);
    },
    onClickNewTransfer: function(event) {
        var props = this.props;
        props.module.onNewTransfer(props.trader);
    },
    // Lifecycle.
    render: function() {
        var props = this.props;
        var trader = props.trader;
        return (
            <tr>
              <td>{trader.mnem}</td>
              <td>{trader.display}</td>
              <td>{trader.email}</td>
              <td>
                <div className="btn-group btn-group-xs" role="group">
                  <button type="button" className="btn btn-default"
                          onClick={this.onClickEditTrader}>
                    <span className="glyphicon glyphicon-pencil"></span>
                  </button>
                  <button type="button" className="btn btn-default"
                          onClick={this.onClickNewTrade}>
                    <span className="glyphicon glyphicon-record"></span>
                  </button>
                  <button type="button" className="btn btn-default"
                          onClick={this.onClickNewTransfer}>
                    <span className="glyphicon glyphicon-transfer"></span>
                  </button>
                </div>
              </td>
            </tr>
        );
    }
});

var TraderTable = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        var module = props.module;
        var rows = props.traders.map(function(module, trader) {
            return (
                <TraderRow key={trader.key} trader={trader}
                           module={module}/>
            );
        }.bind(this, module));
        return (
            <table className="traderTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>Mnem</th>
                  <th>Display</th>
                  <th>Email</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

function levels(arr, showDepth) {
    var html = '';
    if (!showDepth) {
        html = optional(arr[0]);
    } else {
        for (var i = 0; i < arr.length; ++i) {
            if (i > 0) {
                html += '<br/>';
            }
            html += optional(arr[i]);
        }
    }
    return {__html: html};
}

var ViewRow = React.createClass({
    // Mutators.
    setSelected: function(isSelected) {
        if (this.state.isSelected != isSelected) {
            this.setState({
                isSelected: isSelected
            });
        }
    },
    // DOM Events.
    onChangeCheckbox: function(event) {
        this.setSelected(event.target.checked);
    },
    onClickBid: function(event) {
        var view = this.props.view;
        var market = view.market;
        var lots = view.bidResd[0];
        var price = view.bidPrice[0];
        if (price === null) {
            price = view.lastPrice;
            if (price === null) {
                price = 0;
            }
            lots = undefined;
        }
        this.props.module.onChangeFields(market, lots, price);
    },
    onClickLast: function(event) {
        var view = this.props.view;
        var market = view.market;
        var lots = undefined;
        var price = view.lastPrice;
        if (price === null) {
            price = 0;
        }
        this.props.module.onChangeFields(market, lots, price);
    },
    onClickOffer: function(event) {
        var view = this.props.view;
        var market = view.market;
        var lots = view.offerResd[0];
        var price = view.offerPrice[0];
        if (price === null) {
            price = view.lastPrice;
            if (price === null) {
                price = 0;
            }
            lots = undefined;
        }
        this.props.module.onChangeFields(market, lots, price);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            isSelected: false
        };
    },
    componentWillUnmount: function() {
        this.setSelected(false);
    },
    render: function() {
        var view = this.props.view;
        var showDepth = this.state.isSelected;
        return (
            <tr style={{cursor: 'hand'}}>
              <td onClick={this.onClickLast}>
                <input type="checkbox" checked={this.state.isSelected}
                       onChange={this.onChangeCheckbox}/>
              </td>
              <td onClick={this.onClickLast}>{view.market}</td>
              <td style={alignRight}
                  dangerouslySetInnerHTML={levels(view.bidCount, showDepth)}
                  onClick={this.onClickBid}/>
              <td style={alignRight}
                  dangerouslySetInnerHTML={levels(view.bidResd, showDepth)}
                  onClick={this.onClickBid}/>
              <td style={alignRight}
                  dangerouslySetInnerHTML={levels(view.bidPrice, showDepth)}
                  onClick={this.onClickBid}/>
              <td style={alignRight}
                  onClick={this.onClickLast}>{optional(view.lastPrice)}</td>
              <td style={alignRight}
                  dangerouslySetInnerHTML={levels(view.offerPrice, showDepth)}
                  onClick={this.onClickOffer}/>
              <td style={alignRight}
                  dangerouslySetInnerHTML={levels(view.offerResd, showDepth)}
                  onClick={this.onClickOffer}/>
              <td style={alignRight}
                  dangerouslySetInnerHTML={levels(view.offerCount, showDepth)}
                  onClick={this.onClickOffer}/>
            </tr>
        );
    }
});

var ViewTable = React.createClass({
    // Mutators.
    // DOM Events.
    onChangeCheckbox: function(event) {
        var isSelected = event.target.checked;
        // Cascade to child nodes.
        for (k in this.refs) {
            this.refs[k].setSelected(isSelected);
        }
    },
    // Lifecycle.
    render: function() {
        var i = 0;
        var props = this.props;
        var rows = props.views.map(function(module, view) {
            return (
                <ViewRow key={view.key} ref={'row' + i++}
                         module={module} view={view}/>
            );
        }.bind(this, props.module));
        return (
            <table className="viewTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>
                    <input type="checkbox" onChange={this.onChangeCheckbox}/>
                  </th>
                  <th>Market</th>
                  <th style={alignRight}>Bid Count</th>
                  <th style={alignRight}>Bid Lots</th>
                  <th style={alignRight}>Bid Price</th>
                  <th style={alignRight}>Last Price</th>
                  <th style={alignRight}>Offer Price</th>
                  <th style={alignRight}>Offer Lots</th>
                  <th style={alignRight}>Offer Count</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var OrderRow = React.createClass({
    // Mutators.
    setSelected: function(isSelected) {
        var node = this.refs.checkbox.getDOMNode();
        node.checked = isSelected;
        this.props.module.onSelectOrder(this.props.order, isSelected);
    },
    // DOM Events.
    onChangeCheckbox: function(event) {
        var isSelected = event.target.checked;
        this.props.module.onSelectOrder(this.props.order, isSelected);
    },
    onClickOrder: function(event) {
        var order = this.props.order;
        var market = order.market;
        var lots = order.resd > 0 ? order.resd : order.lots;
        var price = order.price;
        this.props.module.onChangeFields(market, lots, price);
    },
    // Lifecycle.
    componentWillUnmount: function() {
        this.setSelected(false);
    },
    render: function() {
        var order = this.props.order;
        return (
            <tr style={{cursor: 'hand'}} onClick={this.onClickOrder}>
              <td>
                <input ref="checkbox" type="checkbox" onChange={this.onChangeCheckbox}/>
              </td>
              <td>{order.market}</td>
              <td>{order.id}</td>
              <td>{order.state}</td>
              <td>{order.side}</td>
              <td style={alignRight}>{order.lots}</td>
              <td style={alignRight}>{order.price}</td>
              <td style={alignRight}>{order.resd}</td>
              <td style={alignRight}>{order.exec}</td>
              <td style={alignRight}>{optional(order.lastLots)}</td>
              <td style={alignRight}>{optional(order.lastPrice)}</td>
            </tr>
        );
    }
});

var OrderTable = React.createClass({
    // Mutators.
    // DOM Events.
    onChangeCheckbox: function(event) {
        var isSelected = event.target.checked;
        // Cascade to child nodes.
        for (k in this.refs) {
            this.refs[k].setSelected(isSelected);
        }
    },
    // Lifecycle.
    render: function() {
        var i = 0;
        var props = this.props;
        var rows = props.orders.map(function(module, order) {
            return (
                <OrderRow key={order.key} ref={'row' + i++}
                          module={module} order={order}/>
            );
        }.bind(this, props.module));
        return (
            <table className="orderTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>
                    <input type="checkbox" onChange={this.onChangeCheckbox}/>
                  </th>
                  <th>Market</th>
                  <th>Id</th>
                  <th>State</th>
                  <th>Side</th>
                  <th style={alignRight}>Lots</th>
                  <th style={alignRight}>Price</th>
                  <th style={alignRight}>Resd</th>
                  <th style={alignRight}>Exec</th>
                  <th style={alignRight}>Last Lots</th>
                  <th style={alignRight}>Last Price</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var TradeRow = React.createClass({
    // Mutators.
    setSelected: function(isSelected) {
        var node = this.refs.checkbox.getDOMNode();
        node.checked = isSelected;
        this.props.module.onSelectTrade(this.props.trade, isSelected);
    },
    // DOM Events.
    onChangeCheckbox: function(event) {
        var isSelected = event.target.checked;
        this.props.module.onSelectTrade(this.props.trade, isSelected);
    },
    onClickTrade: function(event) {
        var trade = this.props.trade;
        var market = trade.market;
        var lots = trade.resd > 0 ? trade.resd : trade.lastLots;
        var price = trade.lastPrice;
        this.props.module.onChangeFields(market, lots, price);
    },
    // Lifecycle.
    componentWillUnmount: function() {
        this.setSelected(false);
    },
    render: function() {
        var trade = this.props.trade;
        return (
            <tr style={{cursor: 'hand'}} onClick={this.onClickTrade}>
              <td>
                <input ref="checkbox" type="checkbox" onChange={this.onChangeCheckbox}/>
              </td>
              <td>{trade.market}</td>
              <td>{trade.id}</td>
              <td>{trade.orderId}</td>
              <td>{trade.side}</td>
              <td style={alignRight}>{trade.lastLots}</td>
              <td style={alignRight}>{trade.lastPrice}</td>
              <td style={alignRight}>{trade.resd}</td>
              <td style={alignRight}>{trade.exec}</td>
              <td>{trade.role}</td>
              <td>{trade.cpty}</td>
            </tr>
        );
    }
});

var TradeTable = React.createClass({
    // Mutators.
    // DOM Events.
    onChangeCheckbox: function(event) {
        var isSelected = event.target.checked;
        // Cascade to child nodes.
        for (k in this.refs) {
            this.refs[k].setSelected(isSelected);
        }
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            isSelected: false
        };
    },
    render: function() {
        var i = 0;
        var props = this.props;
        var rows = props.trades.map(function(module, trade) {
            return (
                <TradeRow key={trade.key} ref={'row' + i++}
                          module={module} trade={trade}/>
            );
        }.bind(this, props.module));
        return (
            <table className="tradeTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>
                    <input type="checkbox" onChange={this.onChangeCheckbox}/>
                  </th>
                  <th>Market</th>
                  <th>Id</th>
                  <th>Order Id</th>
                  <th>Side</th>
                  <th style={alignRight}>Lots</th>
                  <th style={alignRight}>Price</th>
                  <th style={alignRight}>Resd</th>
                  <th style={alignRight}>Exec</th>
                  <th>Role</th>
                  <th>Cpty</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var PosnRow = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var posn = this.props.posn;
        return (
            <tr>
              <td>{posn.contr.mnem}</td>
              <td>{posn.settlDate}</td>
              <td style={alignRight}>{posn.sellLots}</td>
              <td style={alignRight}>{posn.sellPrice}</td>
              <td style={alignRight}>{posn.buyPrice}</td>
              <td style={alignRight}>{posn.buyLots}</td>
              <td style={alignRight}>{posn.netLots}</td>
              <td style={alignRight}>{posn.netPrice}</td>
            </tr>
        );
    }
});

var PosnTable = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        var rows = props.posns.map(function(posn) {
            return (
                <PosnRow key={posn.key} posn={posn}/>
            );
        });
        return (
            <table className="posnTable table table-hover table-striped">
              <thead>
                <tr>
                <th>Contr</th>
                <th>Settl Date</th>
                <th style={alignRight}>Sell Lots</th>
                <th style={alignRight}>Sell Price</th>
                <th style={alignRight}>Buy Price</th>
                <th style={alignRight}>Buy Lots</th>
                <th style={alignRight}>Net Lots</th>
                <th style={alignRight}>Net Price</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});

var QuoteRow = React.createClass({
    // Mutators.
    // DOM Events.
    onClickQuote: function(event) {
        var quote = this.props.quote;
        var market = quote.market;
        var lots = quote.resd > 0 ? quote.resd : quote.lots;
        this.props.module.onChangeFields(market, lots, undefined);
    },
    onClickConfirm: function(event) {
        var quote = this.props.quote;
        var market = quote.market;
        var quoteId = quote.id;
        var side = quote.side;
        var lots = quote.lots;
        var price = quote.price;
        this.props.module.onPostOrder(market, quoteId, side, lots, price);
    },
    // Lifecycle.
    render: function() {
        var quote = this.props.quote;
        var expiry = toTimeStr(quote.expiry);
        return (
            <tr style={{cursor: 'hand'}} onClick={this.onClickQuote}>
              <td>{quote.market}</td>
              <td>{quote.id}</td>
              <td>{quote.side}</td>
              <td style={alignRight}>{quote.lots}</td>
              <td style={alignRight}>{quote.price}</td>
              <td>{expiry}</td>
              <td>
                <div className="btn-group btn-group-xs" role="group">
                  <button type="button" className="btn btn-default"
                          onClick={this.onClickConfirm}>
                    <span className="glyphicon glyphicon-ok"></span>
                  </button>
                </div>
              </td>
            </tr>
        );
    }
});

var QuoteTable = React.createClass({
    // Mutators.
    // DOM Events.
    // Lifecycle.
    render: function() {
        var props = this.props;
        var rows = props.quotes.map(function(module, quote) {
            return (
                <QuoteRow key={quote.key} module={module} quote={quote}/>
            );
        }.bind(this, props.module));
        return (
            <table className="quoteTable table table-hover table-striped">
              <thead>
                <tr>
                  <th>Market</th>
                  <th>Id</th>
                  <th>Side</th>
                  <th style={alignRight}>Lots</th>
                  <th style={alignRight}>Price</th>
                  <th>Expiry</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
        );
    }
});
