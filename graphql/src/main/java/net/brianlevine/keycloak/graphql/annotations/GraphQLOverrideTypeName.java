package net.brianlevine.keycloak.graphql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For InputTypes and Scalars. Classes annotated with GraphQLOverrideTypeName will not have a suffix ('Input' for
 * InputTypes and 'Scalar' for Scalar types) appended. The name of the class or the name given in the
 * @GraphQLInputType/@GraphQLScalar annotations will be used without modification.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GraphQLOverrideTypeName {
}
