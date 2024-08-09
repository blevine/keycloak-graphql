package net.brianlevine.keycloak.graphql.util;


import java.util.List;

public class Page<T> {
    private int totalItems;
    private int totalPages;
    private List<T> items;

    public Page(int totalItems, int totalPages, List<T> items) {
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.items = items;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
