package net.brianlevine.keycloak.graphql.util;

import graphql.GraphQLException;
import io.leangen.graphql.annotations.GraphQLIgnore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A Map that supports paging through its entries.
 *
 * @param <K> The type of the keys in the Map
 * @param <V> The type of the values in the Map
 * @param <E> The type of the DelegatingEntry implementation. The DelegatingEntry
 *           Hides the 'key' and 'value' fields allowing you to specify 'alias' methods for those fields.
 */
@SuppressWarnings("unused")
public class PagedMap<K,V,E extends PagedMap.DelegatingEntry<K,V>> {
    private final Page<E> page;

    @SuppressWarnings("rawtypes")
    private final static PagedMap EMPTY_PAGED_MAP = new PagedMap<>(Collections.emptyMap(), 0, 0, DefaultDelegatingEntry.class);

    public PagedMap(Map<K,V> map, int start, int limit, Class<E> entryClass) {

            Set<E> entrySet = map.entrySet().stream().skip(start).limit(limit).map(e -> {
                try {
                    Constructor<E> c =  entryClass.getDeclaredConstructor(Map.Entry.class);
                    return c.newInstance(e);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException ex) {
                    throw new GraphQLException(ex);
                }
            }).collect(Collectors.toSet());

        page = new Page<>(map.size(), limit, entrySet);
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public int getTotalItems() {
        return page.getTotalItems();
    }

    public Collection<E> getItems() {
        return page.getItems();
    }

    @SuppressWarnings("rawtypes")
    public static PagedMap emptyPagedMap() {
        return EMPTY_PAGED_MAP;
    }

    /**
     * This wrapper class hides the getKey() and getValue() methods so the GraphQL fields 'key' and 'value' do not
     * appear in the schema. Subclasses of this class can specify their own methods that delegate to
     * getKey() and getValue() to create semantically correct GraphQL fields in the schema.
     *
     * @param <K> The type of the key in each entry
     * @param <V> The type of the value in each entry
     */
    public static class DelegatingEntry<K,V> implements Map.Entry<K,V> {

        protected final Map.Entry<K,V> delegate;

        public DelegatingEntry(Map.Entry<K,V> delegate) {
            this.delegate = delegate;
        }

        @Override
        @GraphQLIgnore
        public K getKey() {
            return delegate.getKey();
        }

        @Override
        @GraphQLIgnore
        public V getValue() {
            return delegate.getValue();
        }

        @Override
        @GraphQLIgnore
        public V setValue(V value) {
            return delegate.setValue(value);
        }
    }

    public static class DefaultDelegatingEntry extends DelegatingEntry<String, String>  {
        public DefaultDelegatingEntry(Map.Entry<String,String> delegate) {
            super(delegate);
        }
    }
}
