/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

function Error(num, msg) {
    this.num = num;
    this.msg = msg;
}

function internalError(msg) {
    return new Error(500, msg);
}

function parseError(val) {
    if ('responseText' in val) {
        if (val.responseText.length > 0
            && (val.getResponseHeader('Content-Type') || '')
            .indexOf('application/json') >= 0) {
            val = $.parseJSON(val.responseText);
        } else {
            val = {
                num: val.status,
                msg: val.statusText
            };
        }
    }
    return new Error(val.num, val.msg);
}
