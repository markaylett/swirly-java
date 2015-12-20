/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var ContrModule = React.createClass({
    // Mutators.
    refresh: function() {
        $.getJSON('/front/rec/contr', function(contrs) {
            contrs.forEach(enrichContr.bind(this));
            this.setState({
                contrs: contrs
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
            contrs: null
        };
    },
    componentDidMount: function() {
        this.refresh();
    },
    render: function() {
        var state = this.state;
        var contrs = state.contrs;
        var error = state.error;
        var body = undefined;
        if (error !== null) {
            body = (
                <AlertWidget error={error}/>
            );
        } else if (contrs !== null) {
            body = (
                <ContrTable contrs={contrs}/>
            );
        }
        var marginBottom = {
            marginBottom: 16
        };
        if (body !== undefined) {
            return (
                <div className="contrModule">
                  <div className="page-header" style={marginBottom}>
                    <h3>Contract</h3>
                  </div>
                  {body}
                </div>
            );
        }
        return null;
    }
});
