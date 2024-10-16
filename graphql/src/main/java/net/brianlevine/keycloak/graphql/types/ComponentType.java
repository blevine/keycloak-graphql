package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.MultivaluedMap;
import org.keycloak.representations.idm.ComponentExportRepresentation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GraphQLType
@SuppressWarnings("unused")
public class ComponentType {
    private final ComponentExportRepresentation delegate;

    public ComponentType(ComponentExportRepresentation delegate) {
        this.delegate = delegate;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getProviderId() {
        return delegate.getProviderId();
    }

    public void setProviderId(String providerId) {
        delegate.setProviderId(providerId);
    }

    public String getSubType() {
        return delegate.getSubType();
    }

    public void setSubType(String subType) {
        delegate.setSubType(subType);
    }

    @GraphQLQuery
    public MultiAttributeMap getConfig(PagingOptions options) {
        MultivaluedMap<String, String> config = delegate.getConfig();

        return new MultiAttributeMap(config, options);
    }

//    public void setConfig(MultivaluedHashMap<String, String> config) {
//        delegate.setConfig(config);
//    }

//    public void setConfig(MultivaluedHashMap<String, String> config) {
//        delegate.setConfig(config);
//    }

//    public MultivaluedHashMap<String, ComponentExportRepresentation> getSubComponents() {
//        return delegate.getSubComponents();
//    }

    @GraphQLQuery
    public ComponentMap getSubComponents(PagingOptions options) {
        MultivaluedHashMap<String, ComponentExportRepresentation> subComponents = delegate.getSubComponents();

        Map<String, List<ComponentType>> subs = subComponents != null
                ? subComponents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(ComponentType::new).toList()))
                : new MultivaluedHashMap<>();
        return new ComponentMap(subs, options);
    }

//    public void setSubComponents(MultivaluedHashMap<String, ComponentExportRepresentation> subComponents) {
//        delegate.setSubComponents(subComponents);
//    }
}
