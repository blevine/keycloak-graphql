package net.brianlevine.keycloak.graphql.util;

import io.leangen.graphql.metadata.messages.MessageBundle;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeInfoGenerator;
import net.brianlevine.keycloak.graphql.annotations.GraphQLOverrideTypeName;

import java.lang.reflect.AnnotatedType;

/**
 * Looks for a GraphQLOverrideName annotation on Input and Scalar types. If present the GraphQL type name
 * is generated without a suffix. Useful if you don't want the "Input" suffix to be appended to InputTypes
 * and/or the "Scalar" suffix to be appended to Scalars.
 */
public class OverrideTypeInfoGenerator extends DefaultTypeInfoGenerator {


    @Override
    public String generateInputTypeName(AnnotatedType type, MessageBundle messageBundle) {
        if (type.getAnnotation(GraphQLOverrideTypeName.class) != null) {
            return generateTypeName(type, messageBundle);
        }
        return super.generateInputTypeName(type, messageBundle);
    }

    @Override
    public String generateScalarTypeName(AnnotatedType type, MessageBundle messageBundle) {
        if (type.getAnnotation(GraphQLOverrideTypeName.class) != null) {
            return generateTypeName(type, messageBundle);
        }
        return super.generateScalarTypeName(type, messageBundle);
    }
}
