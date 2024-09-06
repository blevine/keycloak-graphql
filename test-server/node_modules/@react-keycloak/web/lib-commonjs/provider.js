"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ReactKeycloakProvider = void 0;
var core_1 = require("@react-keycloak/core");
var context_1 = require("./context");
exports.ReactKeycloakProvider = core_1.createAuthProvider(context_1.reactKeycloakWebContext);
//# sourceMappingURL=provider.js.map