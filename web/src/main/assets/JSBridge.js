
function onDOMReady(callback) {
    var readyRE = /complete|loaded|interactive/;
    if (readyRE.test(document.readyState)) {
        setTimeout(function() {
                   callback();
                   }, 1);
    } else {
        document.defaultView.addEventListener('DOMContentLoaded', function () {
            callback();
        }, false);
    }
}

var JSBridge = {
    call: function(funcName, params, callBack) {
        var message;
        var plugin = 'ZLJSCoreBridge';
        var CallBackID = plugin + '_' + funcName + '_' + 'CallBack';
        if (callBack) {
            if (!JSBridgeEvent._listeners[CallBackID]) {
                JSBridgeEvent.addEvent(CallBackID, function(data) {
                    callBack(data);
                });
            }
        }
        if (callBack)
        {
            message =
            {
                'plugin': plugin,
                'func': funcName,
                'params': params,
                'callBackID': CallBackID,
            };
        }
        else
        {
            message =
            {
                'plugin': plugin,
                'func': funcName,
                'params': params
            };
        }
        ymyJSBridge.postMessage(funcName,JSON.stringify(params));
    },
    
    trigger: function (name, data) {
                console.log('bridge.trigger ' + name);
        if (name) {
            var triggerEvent = function (name, data) {
                var callbackId;
                if (data && data.callbackId) {
                    callbackId = data.callbackId;
                    data.callbackId = null;
                }
                var evt = document.createEvent("Events");
                evt.initEvent(name, false, true);
                evt.syncJsApis = [];

                if (data) {
                    if (data.__pull__) {
                        delete data.__pull__;
                        for (var k in data) {
                            evt[k] = data[k];
                        }
                    } else {
                        evt.data = data;
                    }
                }
                var canceled = !document.dispatchEvent(evt);
                if (callbackId) {
                    var callbackData = {};
                    callbackData.callbackId = callbackId;
                    callbackData[name + 'EventCanceled'] = canceled;
                    callbackData['syncJsApis'] = evt.syncJsApis;
                    JSBridge.call('__nofunc__', callbackData);
                }
            };
            setTimeout(function () {
                console.log('bridge.trigger name:' + name + " data:" + data);
                triggerEvent(name, data);
            }, 1);
        }
    },
    
    init: function () {
        // dont call me any more
        //JSAPI.init = null;


        console.log('JSBridge.trigger init');
        var readyEvent = document.createEvent('Events');
        readyEvent.initEvent('JSBridgeReady', false, false);

        // 处理ready事件发生以后才addEventListener的情况
        var docAddEventListener = document.addEventListener;
        document.addEventListener = function (name, func) {
            if (name === readyEvent.type) {
                // 保持func执行的异步性
                setTimeout(function () {
                    func(readyEvent);
                }, 1);
            } else {
                docAddEventListener.apply(document, arguments);
            }
        };

        document.dispatchEvent(readyEvent);
        
        JSBridge.call('init',{})

//        setInterval(function(){
//            console.log('local JSBridge.trigger alive');
//        }, 2000);
//        var receivedMessages = receiveMessageQueue;
//        receiveMessageQueue = null;
//        for (var i = 0; i < receivedMessages.length; i++) {
//            JSAPI._invokeJS(receivedMessages[i]);
//        }
    },
    
    callBack: function(callBackID, data) {
        JSBridgeEvent.fireEvent(callBackID, data);
    },

    removeAllCallBacks: function(data) {
        JSBridgeEvent._listeners = {};
    }
};

JSBridge.startupParams = window.ALIPAYH5STARTUPPARAMS || {};

function setStartupParams (data) {
    console.log('JSBridge.trigger setStartupParams' + JSON.stringify(data));
    JSBridge.startupParams = data;
}

function setEnvs (data) {
    window.envs = data;
}

var JSBridgeEvent = {

    _listeners: {},


    addEvent: function(type, fn) {
        if (typeof this._listeners[type] === "undefined") {
            this._listeners[type] = [];
        }
        if (typeof fn === "function") {
            this._listeners[type].push(fn);
        }

        return this;
    },


    fireEvent: function(type, param) {
        var arrayEvent = this._listeners[type];
        if (arrayEvent instanceof Array) {
            for (var i = 0, length = arrayEvent.length; i < length; i += 1) {
                if (typeof arrayEvent[i] === "function") {
                    arrayEvent[i](param);
                }
            }
        }

        return this;
    },

    removeEvent: function(type, fn) {
        var arrayEvent = this._listeners[type];
        if (typeof type === "string" && arrayEvent instanceof Array) {
            if (typeof fn === "function") {
                for (var i = 0, length = arrayEvent.length; i < length; i += 1) {
                    if (arrayEvent[i] === fn) {
                        this._listeners[type].splice(i, 1);
                        break;
                    }
                }
            } else {
                delete this._listeners[type];
            }
        }

        return this;
    }
};

onDOMReady(JSBridge.init);
