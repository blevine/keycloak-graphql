package net.brianlevine.keycloak.graphql.types;

import net.brianlevine.keycloak.graphql.util.PagedMap;

import java.util.Map;

public class HeaderMap extends PagedMap<String, String, HeaderMap.Entry> {

    public HeaderMap(Map<String, String> map, int start, int limit) {
        super(map, start, limit, HeaderMap.Entry.class);
    }

    public HeaderMap(Map<String, String> map, PagingOptions options) {
        super(map, options, HeaderMap.Entry.class);
    }

    public static class Entry extends DelegatingEntry<String, String> implements Map.Entry<String, String> {
        public Entry(Map.Entry<String, String> delegate) {
            super(delegate);
        }

        public String getName() {
            return delegate.getKey();
        }

        public String getValue() {
            return delegate.getValue();
        }
    }
}