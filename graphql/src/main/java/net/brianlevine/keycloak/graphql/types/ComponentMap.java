package net.brianlevine.keycloak.graphql.types;

import net.brianlevine.keycloak.graphql.util.PagedMap;

import java.util.List;
import java.util.Map;

public class ComponentMap extends PagedMap<String, List<ComponentType>, ComponentMap.Entry> {

    public ComponentMap(Map<String, List<ComponentType>> map, int start, int limit) {
        super(map, start, limit, ComponentMap.Entry.class);
    }

    public static class Entry extends DelegatingEntry<String, List<ComponentType>> {
        public Entry(Map.Entry<String, List<ComponentType>> delegate) {
            super(delegate);
        }

        public String getType() {
            return delegate.getKey();
        }

        public List<ComponentType> getComponents() {
            return delegate.getValue();
        }
    }
}