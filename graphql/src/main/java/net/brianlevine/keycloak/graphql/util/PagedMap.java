package net.brianlevine.keycloak.graphql.util;

import java.util.Map;
import java.util.stream.Collectors;

public class PagedMap<K,V> extends Page<Map.Entry<K,V>> {
    public PagedMap(Map<K,V> map, int start, int limit) {
        super(map.size(), limit, map.entrySet().stream().skip(start).limit(limit).collect(Collectors.toSet()));
    }
}
