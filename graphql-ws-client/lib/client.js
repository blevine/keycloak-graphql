"use strict";
/**
 *
 * client
 *
 */
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __exportStar = (this && this.__exportStar) || function(m, exports) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
};
var __await = (this && this.__await) || function (v) { return this instanceof __await ? (this.v = v, this) : new __await(v); }
var __asyncGenerator = (this && this.__asyncGenerator) || function (thisArg, _arguments, generator) {
    if (!Symbol.asyncIterator) throw new TypeError("Symbol.asyncIterator is not defined.");
    var g = generator.apply(thisArg, _arguments || []), i, q = [];
    return i = Object.create((typeof AsyncIterator === "function" ? AsyncIterator : Object).prototype), verb("next"), verb("throw"), verb("return", awaitReturn), i[Symbol.asyncIterator] = function () { return this; }, i;
    function awaitReturn(f) { return function (v) { return Promise.resolve(v).then(f, reject); }; }
    function verb(n, f) { if (g[n]) { i[n] = function (v) { return new Promise(function (a, b) { q.push([n, v, a, b]) > 1 || resume(n, v); }); }; if (f) i[n] = f(i[n]); } }
    function resume(n, v) { try { step(g[n](v)); } catch (e) { settle(q[0][3], e); } }
    function step(r) { r.value instanceof __await ? Promise.resolve(r.value.v).then(fulfill, reject) : settle(q[0][2], r); }
    function fulfill(value) { resume("next", value); }
    function reject(value) { resume("throw", value); }
    function settle(f, v) { if (f(v), q.shift(), q.length) resume(q[0][0], q[0][1]); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.TerminatedCloseEvent = void 0;
exports.createClient = createClient;
const common_1 = require("./common");
const utils_1 = require("./utils");
/** This file is the entry point for browsers, re-export common elements. */
__exportStar(require("./common"), exports);
/**
 * Creates a disposable GraphQL over WebSocket client.
 *
 * @category Client
 */
function createClient(options) {
    const { url, connectionParams, lazy = true, onNonLazyError = console.error, lazyCloseTimeout: lazyCloseTimeoutMs = 0, keepAlive = 0, disablePong, connectionAckWaitTimeout = 0, retryAttempts = 5, retryWait = async function randomisedExponentialBackoff(retries) {
        let retryDelay = 1000; // start with 1s delay
        for (let i = 0; i < retries; i++) {
            retryDelay *= 2;
        }
        await new Promise((resolve) => setTimeout(resolve, retryDelay +
            // add random timeout from 300ms to 3s
            Math.floor(Math.random() * (3000 - 300) + 300)));
    }, shouldRetry = isLikeCloseEvent, isFatalConnectionProblem, on, webSocketImpl, 
    /**
     * Generates a v4 UUID to be used as the ID using `Math`
     * as the random number generator. Supply your own generator
     * in case you need more uniqueness.
     *
     * Reference: https://gist.github.com/jed/982883
     */
    generateID = function generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            const r = (Math.random() * 16) | 0, v = c == 'x' ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        });
    }, jsonMessageReplacer: replacer, jsonMessageReviver: reviver, } = options;
    let ws;
    if (webSocketImpl) {
        if (!isWebSocket(webSocketImpl)) {
            throw new Error('Invalid WebSocket implementation provided');
        }
        ws = webSocketImpl;
    }
    else if (typeof WebSocket !== 'undefined') {
        ws = WebSocket;
    }
    else if (typeof global !== 'undefined') {
        ws =
            global.WebSocket ||
                // @ts-expect-error: Support more browsers
                global.MozWebSocket;
    }
    else if (typeof window !== 'undefined') {
        ws =
            window.WebSocket ||
                // @ts-expect-error: Support more browsers
                window.MozWebSocket;
    }
    if (!ws)
        throw new Error("WebSocket implementation missing; on Node you can `import WebSocket from 'ws';` and pass `webSocketImpl: WebSocket` to `createClient`");
    const WebSocketImpl = ws;
    // websocket status emitter, subscriptions are handled differently
    const emitter = (() => {
        const message = (() => {
            const listeners = {};
            return {
                on(id, listener) {
                    listeners[id] = listener;
                    return () => {
                        delete listeners[id];
                    };
                },
                emit(message) {
                    var _a;
                    if ('id' in message)
                        (_a = listeners[message.id]) === null || _a === void 0 ? void 0 : _a.call(listeners, message);
                },
            };
        })();
        const listeners = {
            connecting: (on === null || on === void 0 ? void 0 : on.connecting) ? [on.connecting] : [],
            opened: (on === null || on === void 0 ? void 0 : on.opened) ? [on.opened] : [],
            connected: (on === null || on === void 0 ? void 0 : on.connected) ? [on.connected] : [],
            ping: (on === null || on === void 0 ? void 0 : on.ping) ? [on.ping] : [],
            pong: (on === null || on === void 0 ? void 0 : on.pong) ? [on.pong] : [],
            message: (on === null || on === void 0 ? void 0 : on.message) ? [message.emit, on.message] : [message.emit],
            closed: (on === null || on === void 0 ? void 0 : on.closed) ? [on.closed] : [],
            error: (on === null || on === void 0 ? void 0 : on.error) ? [on.error] : [],
        };
        return {
            onMessage: message.on,
            on(event, listener) {
                const l = listeners[event];
                l.push(listener);
                return () => {
                    l.splice(l.indexOf(listener), 1);
                };
            },
            emit(event, ...args) {
                // we copy the listeners so that unlistens dont "pull the rug under our feet"
                for (const listener of [...listeners[event]]) {
                    // @ts-expect-error: The args should fit
                    listener(...args);
                }
            },
        };
    })();
    // invokes the callback either when an error or closed event is emitted,
    // first one that gets called prevails, other emissions are ignored
    function errorOrClosed(cb) {
        const listening = [
            // errors are fatal and more critical than close events, throw them first
            emitter.on('error', (err) => {
                listening.forEach((unlisten) => unlisten());
                cb(err);
            }),
            // closes can be graceful and not fatal, throw them second (if error didnt throw)
            emitter.on('closed', (event) => {
                listening.forEach((unlisten) => unlisten());
                cb(event);
            }),
        ];
    }
    let connecting, locks = 0, lazyCloseTimeout, retrying = false, retries = 0, disposed = false;
    async function connect() {
        // clear the lazy close timeout immediatelly so that close gets debounced
        // see: https://github.com/enisdenjo/graphql-ws/issues/388
        clearTimeout(lazyCloseTimeout);
        const [socket, throwOnClose] = await (connecting !== null && connecting !== void 0 ? connecting : (connecting = new Promise((connected, denied) => (async () => {
            if (retrying) {
                await retryWait(retries);
                // subscriptions might complete while waiting for retry
                if (!locks) {
                    connecting = undefined;
                    return denied({ code: 1000, reason: 'All Subscriptions Gone' });
                }
                retries++;
            }
            emitter.emit('connecting', retrying);
            const socket = new WebSocketImpl(typeof url === 'function' ? await url() : url, common_1.GRAPHQL_TRANSPORT_WS_PROTOCOL);
            let connectionAckTimeout, queuedPing;
            function enqueuePing() {
                if (isFinite(keepAlive) && keepAlive > 0) {
                    clearTimeout(queuedPing); // in case where a pong was received before a ping (this is valid behaviour)
                    queuedPing = setTimeout(() => {
                        if (socket.readyState === WebSocketImpl.OPEN) {
                            socket.send((0, common_1.stringifyMessage)({ type: common_1.MessageType.Ping }));
                            emitter.emit('ping', false, undefined);
                        }
                    }, keepAlive);
                }
            }
            errorOrClosed((errOrEvent) => {
                connecting = undefined;
                clearTimeout(connectionAckTimeout);
                clearTimeout(queuedPing);
                denied(errOrEvent);
                if (errOrEvent instanceof TerminatedCloseEvent) {
                    socket.close(4499, 'Terminated'); // close event is artificial and emitted manually, see `Client.terminate()` below
                    socket.onerror = null;
                    socket.onclose = null;
                }
            });
            socket.onerror = (err) => emitter.emit('error', err);
            socket.onclose = (event) => emitter.emit('closed', event);
            socket.onopen = async () => {
                try {
                    emitter.emit('opened', socket);
                    const payload = typeof connectionParams === 'function'
                        ? await connectionParams()
                        : connectionParams;
                    // connectionParams might take too long causing the server to kick off the client
                    // the necessary error/close event is already reported - simply stop execution
                    if (socket.readyState !== WebSocketImpl.OPEN)
                        return;
                    socket.send((0, common_1.stringifyMessage)(payload
                        ? {
                            type: common_1.MessageType.ConnectionInit,
                            payload,
                        }
                        : {
                            type: common_1.MessageType.ConnectionInit,
                            // payload is completely absent if not provided
                        }, replacer));
                    if (isFinite(connectionAckWaitTimeout) &&
                        connectionAckWaitTimeout > 0) {
                        connectionAckTimeout = setTimeout(() => {
                            socket.close(common_1.CloseCode.ConnectionAcknowledgementTimeout, 'Connection acknowledgement timeout');
                        }, connectionAckWaitTimeout);
                    }
                    enqueuePing(); // enqueue ping (noop if disabled)
                }
                catch (err) {
                    emitter.emit('error', err);
                    socket.close(common_1.CloseCode.InternalClientError, (0, utils_1.limitCloseReason)(err instanceof Error ? err.message : new Error(err).message, 'Internal client error'));
                }
            };
            let acknowledged = false;
            socket.onmessage = ({ data }) => {
                try {
                    const message = (0, common_1.parseMessage)(data, reviver);
                    emitter.emit('message', message);
                    if (message.type === 'ping' || message.type === 'pong') {
                        emitter.emit(message.type, true, message.payload); // received
                        if (message.type === 'pong') {
                            enqueuePing(); // enqueue next ping (noop if disabled)
                        }
                        else if (!disablePong) {
                            // respond with pong on ping
                            socket.send((0, common_1.stringifyMessage)(message.payload
                                ? {
                                    type: common_1.MessageType.Pong,
                                    payload: message.payload,
                                }
                                : {
                                    type: common_1.MessageType.Pong,
                                    // payload is completely absent if not provided
                                }));
                            emitter.emit('pong', false, message.payload);
                        }
                        return; // ping and pongs can be received whenever
                    }
                    if (acknowledged)
                        return; // already connected and acknowledged
                    if (message.type !== common_1.MessageType.ConnectionAck)
                        throw new Error(`First message cannot be of type ${message.type}`);
                    clearTimeout(connectionAckTimeout);
                    acknowledged = true;
                    emitter.emit('connected', socket, message.payload, retrying); // connected = socket opened + acknowledged
                    retrying = false; // future lazy connects are not retries
                    retries = 0; // reset the retries on connect
                    connected([
                        socket,
                        new Promise((_, reject) => errorOrClosed(reject)),
                    ]);
                }
                catch (err) {
                    socket.onmessage = null; // stop reading messages as soon as reading breaks once
                    emitter.emit('error', err);
                    socket.close(common_1.CloseCode.BadResponse, (0, utils_1.limitCloseReason)(err instanceof Error ? err.message : new Error(err).message, 'Bad response'));
                }
            };
        })())));
        // if the provided socket is in a closing state, wait for the throw on close
        if (socket.readyState === WebSocketImpl.CLOSING)
            await throwOnClose;
        let release = () => {
            // releases this connection
        };
        const released = new Promise((resolve) => (release = resolve));
        return [
            socket,
            release,
            Promise.race([
                // wait for
                released.then(() => {
                    if (!locks) {
                        // and if no more locks are present, complete the connection
                        const complete = () => socket.close(1000, 'Normal Closure');
                        if (isFinite(lazyCloseTimeoutMs) && lazyCloseTimeoutMs > 0) {
                            // if the keepalive is set, allow for the specified calmdown time and
                            // then complete if the socket is still open.
                            lazyCloseTimeout = setTimeout(() => {
                                if (socket.readyState === WebSocketImpl.OPEN)
                                    complete();
                            }, lazyCloseTimeoutMs);
                        }
                        else {
                            // otherwise complete immediately
                            complete();
                        }
                    }
                }),
                // or
                throwOnClose,
            ]),
        ];
    }
    /**
     * Checks the `connect` problem and evaluates if the client should retry.
     */
    function shouldRetryConnectOrThrow(errOrCloseEvent) {
        // some close codes are worth reporting immediately
        if (isLikeCloseEvent(errOrCloseEvent) &&
            (isFatalInternalCloseCode(errOrCloseEvent.code) ||
                [
                    common_1.CloseCode.InternalServerError,
                    common_1.CloseCode.InternalClientError,
                    common_1.CloseCode.BadRequest,
                    common_1.CloseCode.BadResponse,
                    common_1.CloseCode.Unauthorized,
                    // CloseCode.Forbidden, might grant access out after retry
                    common_1.CloseCode.SubprotocolNotAcceptable,
                    // CloseCode.ConnectionInitialisationTimeout, might not time out after retry
                    // CloseCode.ConnectionAcknowledgementTimeout, might not time out after retry
                    common_1.CloseCode.SubscriberAlreadyExists,
                    common_1.CloseCode.TooManyInitialisationRequests,
                    // 4499, // Terminated, probably because the socket froze, we want to retry
                ].includes(errOrCloseEvent.code)))
            throw errOrCloseEvent;
        // client was disposed, no retries should proceed regardless
        if (disposed)
            return false;
        // normal closure (possibly all subscriptions have completed)
        // if no locks were acquired in the meantime, shouldnt try again
        if (isLikeCloseEvent(errOrCloseEvent) && errOrCloseEvent.code === 1000)
            return locks > 0;
        // retries are not allowed or we tried to many times, report error
        if (!retryAttempts || retries >= retryAttempts)
            throw errOrCloseEvent;
        // throw non-retryable connection problems
        if (!shouldRetry(errOrCloseEvent))
            throw errOrCloseEvent;
        // @deprecated throw fatal connection problems immediately
        if (isFatalConnectionProblem === null || isFatalConnectionProblem === void 0 ? void 0 : isFatalConnectionProblem(errOrCloseEvent))
            throw errOrCloseEvent;
        // looks good, start retrying
        return (retrying = true);
    }
    // in non-lazy (hot?) mode always hold one connection lock to persist the socket
    if (!lazy) {
        (async () => {
            locks++;
            for (;;) {
                try {
                    const [, , throwOnClose] = await connect();
                    await throwOnClose; // will always throw because releaser is not used
                }
                catch (errOrCloseEvent) {
                    try {
                        if (!shouldRetryConnectOrThrow(errOrCloseEvent))
                            return;
                    }
                    catch (errOrCloseEvent) {
                        // report thrown error, no further retries
                        return onNonLazyError === null || onNonLazyError === void 0 ? void 0 : onNonLazyError(errOrCloseEvent);
                    }
                }
            }
        })();
    }
    function subscribe(payload, sink) {
        const id = generateID(payload);
        let done = false, errored = false, releaser = () => {
            // for handling completions before connect
            locks--;
            done = true;
        };
        (async () => {
            locks++;
            for (;;) {
                try {
                    const [socket, release, waitForReleaseOrThrowOnClose] = await connect();
                    // if done while waiting for connect, release the connection lock right away
                    if (done)
                        return release();
                    const unlisten = emitter.onMessage(id, (message) => {
                        switch (message.type) {
                            case common_1.MessageType.Next: {
                                // eslint-disable-next-line @typescript-eslint/no-explicit-any -- payload will fit type
                                sink.next(message.payload);
                                return;
                            }
                            case common_1.MessageType.Error: {
                                (errored = true), (done = true);
                                sink.error(message.payload);
                                releaser();
                                return;
                            }
                            case common_1.MessageType.Complete: {
                                done = true;
                                releaser(); // release completes the sink
                                return;
                            }
                        }
                    });
                    socket.send((0, common_1.stringifyMessage)({
                        id,
                        type: common_1.MessageType.Subscribe,
                        payload,
                    }, replacer));
                    releaser = () => {
                        if (!done && socket.readyState === WebSocketImpl.OPEN)
                            // if not completed already and socket is open, send complete message to server on release
                            socket.send((0, common_1.stringifyMessage)({
                                id,
                                type: common_1.MessageType.Complete,
                            }, replacer));
                        locks--;
                        done = true;
                        release();
                    };
                    // either the releaser will be called, connection completed and
                    // the promise resolved or the socket closed and the promise rejected.
                    // whatever happens though, we want to stop listening for messages
                    await waitForReleaseOrThrowOnClose.finally(unlisten);
                    return; // completed, shouldnt try again
                }
                catch (errOrCloseEvent) {
                    if (!shouldRetryConnectOrThrow(errOrCloseEvent))
                        return;
                }
            }
        })()
            .then(() => {
            // delivering either an error or a complete terminates the sequence
            if (!errored)
                sink.complete();
        }) // resolves on release or normal closure
            .catch((err) => {
            sink.error(err);
        }); // rejects on close events and errors
        return () => {
            // dispose only of active subscriptions
            if (!done)
                releaser();
        };
    }
    return {
        on: emitter.on,
        subscribe,
        iterate(request) {
            const pending = [];
            const deferred = {
                done: false,
                error: null,
                resolve: () => {
                    // noop
                },
            };
            const dispose = subscribe(request, {
                next(val) {
                    // eslint-disable-next-line @typescript-eslint/no-explicit-any -- payload will fit type
                    pending.push(val);
                    deferred.resolve();
                },
                error(err) {
                    deferred.done = true;
                    deferred.error = err;
                    deferred.resolve();
                },
                complete() {
                    deferred.done = true;
                    deferred.resolve();
                },
            });
            const iterator = (function iterator() {
                return __asyncGenerator(this, arguments, function* iterator_1() {
                    for (;;) {
                        if (!pending.length) {
                            // only wait if there are no pending messages available
                            yield __await(new Promise((resolve) => (deferred.resolve = resolve)));
                        }
                        // first flush
                        while (pending.length) {
                            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                            yield yield __await(pending.shift());
                        }
                        // then error
                        if (deferred.error) {
                            throw deferred.error;
                        }
                        // or complete
                        if (deferred.done) {
                            return yield __await(void 0);
                        }
                    }
                });
            })();
            iterator.throw = async (err) => {
                if (!deferred.done) {
                    deferred.done = true;
                    deferred.error = err;
                    deferred.resolve();
                }
                return { done: true, value: undefined };
            };
            iterator.return = async () => {
                dispose();
                return { done: true, value: undefined };
            };
            return iterator;
        },
        async dispose() {
            disposed = true;
            if (connecting) {
                // if there is a connection, close it
                const [socket] = await connecting;
                socket.close(1000, 'Normal Closure');
            }
        },
        terminate() {
            if (connecting) {
                // only if there is a connection
                emitter.emit('closed', new TerminatedCloseEvent());
            }
        },
    };
}
/**
 * A synthetic close event `4499: Terminated` is issued to the current to immediately
 * close the connection without waiting for the one coming from `WebSocket.onclose`.
 *
 * Terminating is not considered fatal and a connection retry will occur as expected.
 *
 * Useful in cases where the WebSocket is stuck and not emitting any events;
 * can happen on iOS Safari, see: https://github.com/enisdenjo/graphql-ws/discussions/290.
 */
class TerminatedCloseEvent extends Error {
    constructor() {
        super(...arguments);
        this.name = 'TerminatedCloseEvent';
        this.message = '4499: Terminated';
        this.code = 4499;
        this.reason = 'Terminated';
        this.wasClean = false;
    }
}
exports.TerminatedCloseEvent = TerminatedCloseEvent;
function isLikeCloseEvent(val) {
    return (0, utils_1.isObject)(val) && 'code' in val && 'reason' in val;
}
function isFatalInternalCloseCode(code) {
    if ([
        1000, // Normal Closure is not an erroneous close code
        1001, // Going Away
        1006, // Abnormal Closure
        1005, // No Status Received
        1012, // Service Restart
        1013, // Try Again Later
        1014, // Bad Gateway
    ].includes(code))
        return false;
    // all other internal errors are fatal
    return code >= 1000 && code <= 1999;
}
function isWebSocket(val) {
    return (typeof val === 'function' &&
        'constructor' in val &&
        'CLOSED' in val &&
        'CLOSING' in val &&
        'CONNECTING' in val &&
        'OPEN' in val);
}
