import { useContext } from 'react';
import { reactKeycloakWebContext } from './context';
export function useKeycloak() {
    var ctx = useContext(reactKeycloakWebContext);
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
//# sourceMappingURL=useKeycloak.js.map