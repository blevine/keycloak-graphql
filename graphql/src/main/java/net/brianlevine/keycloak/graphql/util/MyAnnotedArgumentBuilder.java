package net.brianlevine.keycloak.graphql.util;

import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.metadata.DefaultValue;
import io.leangen.graphql.metadata.strategy.query.AnnotatedArgumentBuilder;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;

public class MyAnnotedArgumentBuilder extends AnnotatedArgumentBuilder {
    @Override
    protected DefaultValue defaultValue(Parameter parameter, AnnotatedType parameterType, GlobalEnvironment environment) {
        return super.defaultValue(parameter, parameterType, environment);
    }
}
