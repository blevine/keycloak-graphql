package net.brianlevine.keycloak.graphql.types;

import net.brianlevine.keycloak.graphql.util.PagedMap;

import java.util.Map;

public class AccessMap extends PagedMap<String, Boolean, AccessMap.Entry> {

    public AccessMap(Map<String, Boolean> map, PagingOptions options) {
        this(map, options != null ? options.start : PagingOptions.DEFAULT.start, options != null ? options.limit : PagingOptions.DEFAULT.limit);
    }

    public AccessMap(Map<String, Boolean> map, int start, int limit) {
        super(map, start, limit, AccessMap.Entry.class);
    }

    public static class Entry extends DelegatingEntry<String, Boolean> {
        public Entry(Map.Entry<String, Boolean> delegate) {
            super(delegate);
        }

        public String getName() {
            return delegate.getKey();
        }

        public Boolean getAccess() {
            return delegate.getValue();
        }
    }
}