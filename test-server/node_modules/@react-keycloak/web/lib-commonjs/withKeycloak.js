"use strict";
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
Object.defineProperty(exports, "__esModule", { value: true });
exports.withKeycloak = void 0;
var React = __importStar(require("react"));
var useKeycloak_1 = require("./useKeycloak");
/**
 * An HOC which injects the `keycloak` instance and the `keycloakInitialized` flag as props.
 *
 * @deprecated Please migrate to useKeycloak hook where/when possible.
 */
function withKeycloak(Component) {
    return function WrappedComponent(props) {
        var _a = useKeycloak_1.useKeycloak(), keycloak = _a.keycloak, initialized = _a.initialized;
        return (React.createElement(Component, __assign({}, props, { keycloakInitialized: initialized, keycloak: keycloak })));
    };
}
exports.withKeycloak = withKeycloak;
//# sourceMappingURL=withKeycloak.js.map