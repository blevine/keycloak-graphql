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
Object.defineProperty(exports, "__esModule", { value: true });
exports.createAuthContext = void 0;
var react_1 = require("react");
/**
 * Create a React context containing an AuthClient instance.
 *
 * @param {IAuthContextProps} initialContext initial context value.
 *
 * @returns {React.Context} the ReactKeycloak context.
 */
function createAuthContext(initialContext) {
    return react_1.createContext(__assign({ initialized: false }, initialContext));
}
exports.createAuthContext = createAuthContext;
exports.default = createAuthContext;
//# sourceMappingURL=context.js.map