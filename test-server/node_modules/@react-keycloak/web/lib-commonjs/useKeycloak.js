"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useKeycloak = void 0;
var react_1 = require("react");
var context_1 = require("./context");
function useKeycloak() {
    var ctx = react_1.useContext(context_1.reactKeycloakWebContext);
    if (!ctx) {
        throw new Error('useKeycloak hook must be used inside ReactKeycloakProvider context');
    }
    if (!ctx.authClient) {
        throw new Error('authClient has not been assigned to ReactKeycloakProvider');
    }
    var authClient = ctx.authClient, initialized = ctx.initialized;
    return {
        initialized: initialized,
        keycloak: authClient,
    };
}
exports.useKeycloak = useKeycloak;
//# sourceMappingURL=useKeycloak.js.map