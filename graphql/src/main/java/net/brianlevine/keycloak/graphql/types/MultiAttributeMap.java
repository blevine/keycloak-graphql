package net.brianlevine.keycloak.graphql.types;

import net.brianlevine.keycloak.graphql.util.PagedMap;

import java.util.List;
import java.util.Map;

public class MultiAttributeMap extends PagedMap<String, List<String>, MultiAttributeMap.Entry> {

    public MultiAttributeMap(Map<String, List<String>> map, int start, int limit) {
        super(map, start, limit, MultiAttributeMap.Entry.class);
    }

    public static class Entry extends DelegatingEntry<String, List<String>>{
        public Entry(Map.Entry<String, List<String>> delegate) {
            super(delegate);
        }

        public String getName() {
            return delegate.getKey();
        }

        public List<String> getValues() {
            return delegate.getValue();
        }
    }
}