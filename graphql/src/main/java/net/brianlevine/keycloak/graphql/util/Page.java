package net.brianlevine.keycloak.graphql.util;


import net.brianlevine.keycloak.graphql.types.PagingOptions;

import java.lang.reflect.InvocationTargetException;
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

    public interface RepresentationGetter<R> {
        List<R> getRepresentations();
    }

    /**
     * Given a List of Keycloak *Representation objects, returns a Page of GraphQL *Type objects
     * that wrap the *Representation objects.
     *
     * @param options the paging Options
     * @param typeClass the GraphQL *Type class to convert to
     * @param repClass the Keycloak *Representation class to convert from
     * @param getter a function that implements RepresentationGetter that returns the list of representations
     * @return a Page of *Type objects
     * @param <T> the type of the *Type class
     * @param <R> the type of the *Representation class
     *
     */
    public static <T, R> Page<T> toPagedType(PagingOptions options, Class<T> typeClass, Class<R> repClass, RepresentationGetter<R> getter)  {
        Page<T> ret = Page.emptyPage();
        List<R> reps = getter.getRepresentations();

        if (reps != null) {
            options = options != null ? options : new PagingOptions();

            List<T> idps = reps.stream()
                    .skip(options.start)
                    .limit(options.limit)
                    .map( t -> {
                        try {
                            return typeClass.getDeclaredConstructor(repClass).newInstance(t);
                        } catch (InstantiationException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
            ret = new Page<>(reps.size(), options.limit, idps);
        }

        return ret;
    }
}
