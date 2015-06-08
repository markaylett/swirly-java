/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

var SignupModule = React.createClass({
    // Mutators.
    postTrader: function(mnem, display) {
        console.debug('signup: mnem=' + mnem + ', display=' + display);
        if (!isSpecified(mnem)) {
            this.onReportError(internalError('mnem not specified'));
            return;
        }
        if (!isSpecified(display)) {
            this.onReportError(internalError('display not specified'));
            return;
        }
        $.ajax({
            type: 'post',
            url: '/api/rec/trader/',
            data: JSON.stringify({
                mnem: mnem,
                display: display
            })
        }).done(function(unused) {
            window.location.reload();
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
    onPostTrader: function(mnem, display) {
        this.postTrader(mnem, display);
    },
    // Lifecycle.
    getInitialState: function() {
        return {
            module: {
                onClearErrors: this.onClearErrors,
                onReportError: this.onReportError,
                onPostTrader: this.onPostTrader
            },
            errors: []
        };
    },
    render: function() {
        var state = this.state;
        var module = state.module;
        var errors = state.errors;
        return (
            <div className="signupModule">
              <MultiAlertWidget module={module} errors={errors}/>
              <SignupForm module={module}/>
            </div>
        );
    },
    staging: {
        errors: new Tail(5)
    }
});
