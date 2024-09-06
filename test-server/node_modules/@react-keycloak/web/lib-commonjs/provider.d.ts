/// <reference types="react" />
export declare const ReactKeycloakProvider: {
    new (props: import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance> | Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>): {
        state: {
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        };
        componentDidMount(): void;
        componentDidUpdate({ authClient: prevAuthClient, initOptions: prevInitOptions, }: import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>): void;
        init(): void;
        onError: (event: import("@react-keycloak/core").AuthClientEvent) => (error?: import("@react-keycloak/core").AuthClientError | undefined) => void;
        updateState: (event: import("@react-keycloak/core").AuthClientEvent) => () => void;
        refreshToken: (event: import("@react-keycloak/core").AuthClientEvent) => () => void;
        render(): JSX.Element;
        context: any;
        setState<K extends "initialized" | "isAuthenticated" | "isLoading">(state: {
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        } | Pick<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }, K> | ((prevState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, props: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>) => {
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        } | Pick<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }, K> | null) | null, callback?: (() => void) | undefined): void;
        forceUpdate(callback?: (() => void) | undefined): void;
        readonly props: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>> & Readonly<{
            children?: import("react").ReactNode;
        }>;
        refs: {
            [key: string]: import("react").ReactInstance;
        };
        shouldComponentUpdate?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, nextContext: any): boolean;
        componentWillUnmount?(): void;
        componentDidCatch?(error: Error, errorInfo: import("react").ErrorInfo): void;
        getSnapshotBeforeUpdate?(prevProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, prevState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>): any;
        componentWillMount?(): void;
        UNSAFE_componentWillMount?(): void;
        componentWillReceiveProps?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextContext: any): void;
        UNSAFE_componentWillReceiveProps?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextContext: any): void;
        componentWillUpdate?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, nextContext: any): void;
        UNSAFE_componentWillUpdate?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, nextContext: any): void;
    };
    new (props: import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>, context: any): {
        state: {
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        };
        componentDidMount(): void;
        componentDidUpdate({ authClient: prevAuthClient, initOptions: prevInitOptions, }: import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>): void;
        init(): void;
        onError: (event: import("@react-keycloak/core").AuthClientEvent) => (error?: import("@react-keycloak/core").AuthClientError | undefined) => void;
        updateState: (event: import("@react-keycloak/core").AuthClientEvent) => () => void;
        refreshToken: (event: import("@react-keycloak/core").AuthClientEvent) => () => void;
        render(): JSX.Element;
        context: any;
        setState<K_1 extends "initialized" | "isAuthenticated" | "isLoading">(state: {
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        } | Pick<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }, K_1> | ((prevState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, props: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>) => {
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        } | Pick<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }, K_1> | null) | null, callback?: (() => void) | undefined): void;
        forceUpdate(callback?: (() => void) | undefined): void;
        readonly props: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>> & Readonly<{
            children?: import("react").ReactNode;
        }>;
        refs: {
            [key: string]: import("react").ReactInstance;
        };
        shouldComponentUpdate?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, nextContext: any): boolean;
        componentWillUnmount?(): void;
        componentDidCatch?(error: Error, errorInfo: import("react").ErrorInfo): void;
        getSnapshotBeforeUpdate?(prevProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, prevState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>): any;
        componentWillMount?(): void;
        UNSAFE_componentWillMount?(): void;
        componentWillReceiveProps?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextContext: any): void;
        UNSAFE_componentWillReceiveProps?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextContext: any): void;
        componentWillUpdate?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, nextContext: any): void;
        UNSAFE_componentWillUpdate?(nextProps: Readonly<import("@react-keycloak/core").AuthProviderProps<import("keycloak-js").KeycloakInstance>>, nextState: Readonly<{
            initialized: boolean;
            isAuthenticated: boolean;
            isLoading: boolean;
        }>, nextContext: any): void;
    };
    contextType?: import("react").Context<any> | undefined;
};
