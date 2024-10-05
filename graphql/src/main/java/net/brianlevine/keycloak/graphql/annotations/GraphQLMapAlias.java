package net.brianlevine.keycloak.graphql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TBD: Notional. Maybe use this to change the field names for Map.Entry objects.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GraphQLMapAlias {
    String keyAlias() default "";
    String valueAlias() default "";
}

