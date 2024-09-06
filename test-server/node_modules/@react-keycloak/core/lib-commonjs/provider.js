"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.createAuthProvider = void 0;
var React = __importStar(require("react"));
var react_fast_compare_1 = __importDefault(require("react-fast-compare"));
/**
 * Create an AuthProvider component to wrap a React app with, it will take care of common AuthClient
 * lifecycle handling (such as initialization and token refresh).
 *
 * @param AuthContext the Auth context to be used by the created AuthProvider
 *
 * @returns the AuthProvider component
 */
function createAuthProvider(AuthContext) {
    var defaultInitOptions = {
        onLoad: 'check-sso',
    };
    var initialState = {
        initialized: false,
        isAuthenticated: false,
        isLoading: true,
    };
    return /** @class */ (function (_super) {
        __extends(KeycloakProvider, _super);
        function KeycloakProvider() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.state = __assign({}, initialState);
            _this.onError = function (event) { return function (error) {
                var onEvent = _this.props.onEvent;
                // Notify Events listener
                onEvent && onEvent(event, error);
            }; };
            _this.updateState = function (event) { return function () {
                var _a = _this.props, authClient = _a.authClient, onEvent = _a.onEvent, onTokens = _a.onTokens, isLoadingCheck = _a.isLoadingCheck;
                var _b = _this.state, prevInitialized = _b.initialized, prevAuthenticated = _b.isAuthenticated, prevLoading = _b.isLoading;
                // Notify Events listener
                onEvent && onEvent(event);
                // Check Loading state
                var isLoading = isLoadingCheck ? isLoadingCheck(authClient) : false;
                // Check if user is authenticated
                var isAuthenticated = isUserAuthenticated(authClient);
                // Avoid double-refresh if state hasn't changed
                if (!prevInitialized ||
                    isAuthenticated !== prevAuthenticated ||
                    isLoading !== prevLoading) {
                    _this.setState({
                        initialized: true,
                        isAuthenticated: isAuthenticated,
                        isLoading: isLoading,
                    });
                }
                // Notify token listener, if any
                var idToken = authClient.idToken, refreshToken = authClient.refreshToken, token = authClient.token;
                onTokens &&
                    onTokens({
                        idToken: idToken,
                        refreshToken: refreshToken,
                        token: token,
                    });
            }; };
            _this.refreshToken = function (event) { return function () {
                var _a = _this.props, autoRefreshToken = _a.autoRefreshToken, authClient = _a.authClient, onEvent = _a.onEvent;
                // Notify Events listener
                onEvent && onEvent(event);
                if (autoRefreshToken !== false) {
                    // Refresh Keycloak token
                    authClient.updateToken(5);
                }
            }; };
            return _this;
        }
        KeycloakProvider.prototype.componentDidMount = function () {
            this.init();
        };
        KeycloakProvider.prototype.componentDidUpdate = function (_a) {
            var prevAuthClient = _a.authClient, prevInitOptions = _a.initOptions;
            var _b = this.props, initOptions = _b.initOptions, authClient = _b.authClient;
            if (authClient !== prevAuthClient ||
                !react_fast_compare_1.default(initOptions, prevInitOptions)) {
                // De-init previous AuthClient instance
                prevAuthClient.onReady = undefined;
                prevAuthClient.onAuthSuccess = undefined;
                prevAuthClient.onAuthError = undefined;
                prevAuthClient.onAuthRefreshSuccess = undefined;
                prevAuthClient.onAuthRefreshError = undefined;
                prevAuthClient.onAuthLogout = undefined;
                prevAuthClient.onTokenExpired = undefined;
                // Reset state
                this.setState(__assign({}, initialState));
                // Init new AuthClient instance
                this.init();
            }
        };
        KeycloakProvider.prototype.init = function () {
            var _a = this.props, initOptions = _a.initOptions, authClient = _a.authClient;
            // Attach Keycloak listeners
            authClient.onReady = this.updateState('onReady');
            authClient.onAuthSuccess = this.updateState('onAuthSuccess');
            authClient.onAuthError = this.onError('onAuthError');
            authClient.onAuthRefreshSuccess = this.updateState('onAuthRefreshSuccess');
            authClient.onAuthRefreshError = this.onError('onAuthRefreshError');
            authClient.onAuthLogout = this.updateState('onAuthLogout');
            authClient.onTokenExpired = this.refreshToken('onTokenExpired');
            authClient
                .init(__assign(__assign({}, defaultInitOptions), initOptions))
                .catch(this.onError('onInitError'));
        };
        KeycloakProvider.prototype.render = function () {
            var _a = this.props, children = _a.children, authClient = _a.authClient, LoadingComponent = _a.LoadingComponent;
            var _b = this.state, initialized = _b.initialized, isLoading = _b.isLoading;
            if (!!LoadingComponent && (!initialized || isLoading)) {
                return LoadingComponent;
            }
            return (React.createElement(AuthContext.Provider, { value: { initialized: initialized, authClient: authClient } }, children));
        };
        return KeycloakProvider;
    }(React.PureComponent));
}
exports.createAuthProvider = createAuthProvider;
function isUserAuthenticated(authClient) {
    return !!authClient.idToken && !!authClient.token;
}
exports.default = createAuthProvider;
//# sourceMappingURL=provider.js.map