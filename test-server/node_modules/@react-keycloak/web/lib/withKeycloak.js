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
import * as React from 'react';
import { useKeycloak } from './useKeycloak';
/**
 * An HOC which injects the `keycloak` instance and the `keycloakInitialized` flag as props.
 *
 * @deprecated Please migrate to useKeycloak hook where/when possible.
 */
export function withKeycloak(Component) {
    return function WrappedComponent(props) {
        var _a = useKeycloak(), keycloak = _a.keycloak, initialized = _a.initialized;
        return (React.createElement(Component, __assign({}, props, { keycloakInitialized: initialized, keycloak: keycloak })));
    };
}
//# sourceMappingURL=withKeycloak.js.map