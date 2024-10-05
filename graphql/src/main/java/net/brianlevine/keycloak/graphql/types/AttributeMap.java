package net.brianlevine.keycloak.graphql.types;

import net.brianlevine.keycloak.graphql.util.PagedMap;

import java.util.Map;

public class AttributeMap extends PagedMap<String, String, AttributeMap.Entry> {

    public AttributeMap(Map<String, String> map, int start, int limit) {
        super(map, start, limit, AttributeMap.Entry.class);
    }

    public static class Entry extends PagedMap.DelegatingEntry<String, String> implements Map.Entry<String, String> {
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