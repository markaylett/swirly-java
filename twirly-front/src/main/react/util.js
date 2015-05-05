/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/

function KeyArray() {
    var arr = [];
    this.forEach = function(fn) {
        arr.forEach(fn);
    }
    this.toArray = function() {
        return arr.slice();
    }
    this.toObject = function() {
        obj = {};
        for (var i = 0; i < n; ++i) {
            val = arr[i];
            obj[val.key] = val;
        }
        return obj;
    }
    this.assign = function(rhs) {
        arr = rhs;
    }
    this.clear = function() {
        arr = [];
    }
    this.delete = function(k) {
        var k = v.key;
        var n = arr.length;
        for (var i = 0; i < n; ++i) {
            if (arr[i].key === k) {
                splice(i, 1);
                break;
            }
        }
    }
    this.push = function(v) {
        arr.push(v);
    }
    this.update = function(v) {
        var k = v.key;
        var n = arr.length;
        for (var i = 0; i < n; ++i) {
            if (arr[i].key === k) {
                arr[i] = v;
                return;
            }
        }
        arr.push(v);
    }
    this.isEmpty = function() {
        return arr.length == 0;
    }
}

function Map() {
    var map = {};
    this.forEach = function(fn) {
        for (var k in map) {
            if (map.hasOwnProperty(k)) {
                fn(k, map[k]);
            }
        }
    }
    this.toArray = function() {
        arr = [];
        for (var k in map) {
            if (map.hasOwnProperty(k)) {
                arr.push(map[k]);
            }
        }
        return arr;
    }
    this.toSortedArray = function() {
        arr = [];
        var ks = Object.keys(map).sort();
        var n = ks.length;
        for (var i = 0; i < n; ++i) {
            arr.push(map[ks[i]]);
        }
        return arr;
    }
    this.toObject = function() {
        obj = {};
        for (var k in map) {
            if (map.hasOwnProperty(k)) {
                obj[k] = map[k];
            }
        }
        return obj;
    }
    this.assign = function(rhs) {
        map = rhs;
    }
    this.clear = function() {
        map = {};
    }
    this.delete = function(k) {
        delete map[k];
    }
    this.set = function(k, v) {
        map[k] = v;
    }
    this.get = function(k) {
        return map.hasOwnProperty(k)
            ? map[k] : undefined;
    }
    this.has = function(k) {
        return map.hasOwnProperty(k);
    }
    this.keys = function() {
        return Object.keys(map);
    }
    this.isEmpty = function() {
        for (var k in map) {
            if (map.hasOwnProperty(k)) {
                return false;
            }
        }
        return true;
    }
}

function Tail(limit) {
    var arr = [];
    this.forEach = function(fn) {
        arr.forEach(fn);
    }
    this.toArray = function() {
        return arr.slice();
    }
    this.assign = function(rhs) {
        arr = rhs;
    }
    this.clear = function() {
        arr = [];
    }
    this.push = function(v) {
        // Add to end of list.
        arr.push(v);
        // Pop first if limit exceeded.
        if (arr.length > limit) {
            arr.shift();
        }
    }
    this.isEmpty = function() {
        return arr.length == 0;
    }
}

// jQuery's map function ignores null elements.

function findFirst(array, pred) {
    for (var i = 0, j = array.length; i < j; ++i) {
        if (pred(arr[i])) {
            return arr[i];
        }
    }
    return null;
}

function isMapEmpty(map) {
    for (var k in map) {
        if (map.hasOwnProperty(k)) {
            return false;
        }
    }
    return true;
}

function isSpecified(x) {
    return x !== undefined && x !== null && x !== '';
}

function optional(x) {
    return x !== null ? x : '-';
}

function roundHalfAway(d) {
    if (d < 0) {
        d = Math.ceil(d - 0.5);
    } else if (d > 0) {
        d = Math.floor(d + 0.5);
    }
    return d;
}

function zeroPad(d) {
    return ('0000000000' + d).slice(-10);
}

// Multiplication sign.
var times = String.fromCharCode(215)
