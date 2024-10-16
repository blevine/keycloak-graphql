package net.brianlevine.keycloak.graphql.types;

import net.brianlevine.keycloak.graphql.util.PagedMap;

import java.util.Map;

public class AuthenticationFlowOverrideMap extends PagedMap<String, String, AuthenticationFlowOverrideMap.Entry> {

    public AuthenticationFlowOverrideMap(Map<String, String> map, int start, int limit) {
        super(map, start, limit, AuthenticationFlowOverrideMap.Entry.class);
    }

    public AuthenticationFlowOverrideMap(Map<String, String> map, PagingOptions options) {
        super(map, options, AuthenticationFlowOverrideMap.Entry.class);
    }

    public static class Entry extends DelegatingEntry<String, String> implements Map.Entry<String, String> {
        public Entry(Map.Entry<String, String> delegate) {
            super(delegate);
        }

        public String getFlowType() {
            return delegate.getKey();
        }

        public String getFlowName() {
            return delegate.getValue();
        }
    }
}