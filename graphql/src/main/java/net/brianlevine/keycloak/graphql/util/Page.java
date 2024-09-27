package net.brianlevine.keycloak.graphql.util;


import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Page<T> {
    private final int totalItems;
    private final int totalPages;
    private final Collection<T> items;

    public Page(int totalItems, int pageSize, Collection<T> items) {
        this.totalItems = totalItems;
        this.totalPages = (int) ((double)totalItems / (double)pageSize);
        this.items = items;
    }

    public int getTotalPages() {
        return totalPages;
    }


    public int getTotalItems() {
        return totalItems;
    }

    public Collection<T> getItems() {
        return items;
    }

    public static <T> Page<T> emptyPage() {
        return new Page<>(0, 0, Collections.emptyList());
    }
}
