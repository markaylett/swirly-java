/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var NewMarketDialog = React.createClass({
    // Mutators.
    reset: function()
    {
        this.setState(this.getInitialState());
    },
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
    onChangeContr: function(event) {
        this.setState({
            contr: event.target.value
        });
    },
    onChangeSettlDate: function(event) {
        this.setState({
            settlDate: event.target.value
        });
    },
    onChangeExpiryDate: function(event) {
        this.setState({
            expiryDate: event.target.value
        });
    },
    onChangeState: function(event) {
        this.setState({
            state: event.target.value
        });
    },
    onFocusExpiryDate: function(event) {
        if (!isSpecified(this.state.expiryDate)) {
            this.setState({
                expiryDate: this.state.settlDate
            });
        }
    },
    onClickSave: function(event) {
        event.preventDefault();
        var state = this.state;
        var mnem = state.mnem;
        var display = state.display;
        var contr = state.contr;
        var settlDate = state.settlDate;
        var expiryDate = state.expiryDate;
        var state = state.state;
        this.props.module.onClickNewMarket(mnem, display, contr, settlDate, expiryDate, state);
        this.reset();
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            mnem: undefined,
            display: undefined,
            contr: undefined,
            settlDate: undefined,
            expiryDate: undefined,
            state: undefined
        };
    },
    componentDidMount: function() {
        var node = this.refs.contr.getDOMNode();
        $(node).typeahead({
            items: 4,
            source: function(query, process) {
                var contrKeys = Object.keys(this.props.contrMap);
                process(contrKeys);
            }.bind(this),
            updater: function(value) {
                this.setState({
                    contr: value
                });
                return value;
            }.bind(this)
        });
    },
    render: function() {
        var state = this.state;
        var mnem = state.mnem;
        var display = state.display;
        var contr = state.contr;
        var settlDate = state.settlDate;
        var expiryDate = state.expiryDate;
        var state = state.state;
        return (
            <div id="newMarketDialog" className="modal fade" tabindex={-1}>
              <div className="modal-dialog">
                <div className="modal-content">
                  <div className="modal-header">
                    <button type="button" className="close" data-dismiss="modal"
                            onClick={this.reset}>
                      {times}
                    </button>
                    <h4 className="modal-title">New Market</h4>
                  </div>
                  <div className="modal-body">
                    <form role="form">
                      <div className="form-group">
                        <label htmlFor="mnem">Mnem:</label>
                        <input id="mnem" type="text" className="form-control"
                               value={mnem} onChange={this.onChangeMnem}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="display">Display:</label>
                        <input id="display" type="text" className="form-control"
                               value={display} onChange={this.onChangeDisplay}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="contr">Contract:</label>
                        <input id="contr" ref="contr" type="text" className="form-control"
                               value={contr} onChange={this.onChangeContr}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="settlDate">Settl Date:</label>
                        <input id="settlDate" type="date" className="form-control"
                               value={settlDate} onChange={this.onChangeSettlDate}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="expiryDate">Expiry Date:</label>
                        <input id="expiryDate" type="date" className="form-control"
                               value={expiryDate} onChange={this.onChangeExpiryDate}
                               onFocus={this.onFocusExpiryDate}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="state">State:</label>
                        <input id="state" type="number" className="form-control"
                               value={state} onChange={this.onChangeState}/>
                      </div>
                    </form>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-default" data-dismiss="modal"
                            onClick={this.reset}>
                      Cancel
                    </button>
                    <button type="button" className="btn btn-primary" data-dismiss="modal"
                            onClick={this.onClickSave}>
                      Save
                    </button>
                  </div>
                </div>
              </div>
            </div>
        );
    }
});

var NewTraderDialog = React.createClass({
    // Mutators.
    reset: function()
    {
        this.setState(this.getInitialState());
    },
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
    onChangeEmail: function(event) {
        this.setState({
            email: event.target.value
        });
    },
    onClickSave: function(event) {
        event.preventDefault();
        var state = this.state;
        var mnem = state.mnem;
        var display = state.display;
        var email = state.email;
        this.props.module.onClickNewTrader(mnem, display, email);
        this.reset();
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            mnem: undefined,
            display: undefined,
            email: undefined
        };
    },
    render: function() {
        var state = this.state;
        var mnem = state.mnem;
        var display = state.display;
        var email = state.email;
        return (
            <div id="newTraderDialog" className="modal fade" tabindex={-1}>
              <div className="modal-dialog">
                <div className="modal-content">
                  <div className="modal-header">
                    <button type="button" className="close" data-dismiss="modal"
                            onClick={this.reset}>
                      {times}
                    </button>
                    <h4 className="modal-title">New Trader</h4>
                  </div>
                  <div className="modal-body">
                    <form role="form">
                      <div className="form-group">
                        <label htmlFor="mnem">Mnem:</label>
                        <input id="mnem" type="text" className="form-control"
                               value={mnem} onChange={this.onChangeMnem}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="display">Display:</label>
                        <input id="display" type="email" className="form-control"
                               value={display} onChange={this.onChangeDisplay}/>
                      </div>
                      <div className="form-group">
                        <label htmlFor="email">Email:</label>
                        <input id="email" type="text" className="form-control"
                               value={email} onChange={this.onChangeEmail}/>
                      </div>
                    </form>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-default" data-dismiss="modal"
                            onClick={this.reset}>
                      Cancel
                    </button>
                    <button type="button" className="btn btn-primary" data-dismiss="modal"
                            onClick={this.onClickSave}>
                      Save
                    </button>
                  </div>
                </div>
              </div>
            </div>
        );
    }
});
