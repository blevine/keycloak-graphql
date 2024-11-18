package net.brianlevine.keycloak.graphql;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLContext;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.FetchedValue;
import graphql.execution.FieldValueInfo;
import graphql.execution.MergedField;
import graphql.execution.MergedSelectionSet;
import graphql.execution.NonNullableFieldWasNullException;
import graphql.execution.ResultPath;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.execution.instrumentation.ExecutionStrategyInstrumentationContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters;
import graphql.execution.reactive.SubscriptionPublisher;
import graphql.language.Field;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import org.keycloak.models.KeycloakSession;

import org.keycloak.quarkus.runtime.integration.cdi.KeycloakBeanProducer;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static graphql.Assert.assertTrue;
import static graphql.execution.instrumentation.SimpleInstrumentationContext.nonNullCtx;
import static java.util.Collections.singletonMap;

/**
 * A subscription execution strategy that lets us interpose before and after the subscription event is
 * executed. Based on SubscriptionExecutionStrategy and unfortunately had to clone a number of private
 * methods because the executeSubscriptionEvent method is private.
 */
public class KeycloakSubscriptionExecutionStrategy extends SubscriptionExecutionStrategy {
    public KeycloakSubscriptionExecutionStrategy() {
        super();
    }

    public KeycloakSubscriptionExecutionStrategy(DataFetcherExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }


    @Override
    public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext, ExecutionStrategyParameters parameters) throws NonNullableFieldWasNullException {

        Instrumentation instrumentation = executionContext.getInstrumentation();
        InstrumentationExecutionStrategyParameters instrumentationParameters = new InstrumentationExecutionStrategyParameters(executionContext, parameters);
        ExecutionStrategyInstrumentationContext executionStrategyCtx = ExecutionStrategyInstrumentationContext.nonNullCtx(instrumentation.beginExecutionStrategy(
                instrumentationParameters,
                executionContext.getInstrumentationState()
        ));

        CompletableFuture<Publisher<Object>> sourceEventStream = createSourceEventStream(executionContext, parameters);

        //
        // when the upstream source event stream completes, subscribe to it and wire in our adapter
        CompletableFuture<ExecutionResult> overallResult = sourceEventStream.thenApply((publisher) -> {
            if (publisher == null) {
                return new ExecutionResultImpl(null, executionContext.getErrors());
            }

            Function<Object, CompletionStage<ExecutionResult>> mapperFunction = (eventPayload) -> {
                System.out.println("BEFORE executeSubscriptionEvent");

                KeycloakSession kcSession = new KeycloakBeanProducer().getKeycloakSession();
                kcSession.getTransactionManager().begin();

                GraphQLContext ctx = executionContext.getGraphQLContext();
                ctx.put("keycloak.session", kcSession);

                // Resolvers can use this to determine that the execution is happening as a result of a
                // subscription event.
                ctx.put("isSubscription", true);

                CompletableFuture<ExecutionResult> f =  executeSubscriptionEvent(executionContext, parameters, eventPayload);
                return f.thenApply((a) -> {
                    System.out.println("After executeSubscriptionEvent");
                    kcSession.close();
                    return a;
                });
            };

            SubscriptionPublisher mapSourceToResponse = new SubscriptionPublisher(publisher, mapperFunction);
            return new ExecutionResultImpl(mapSourceToResponse, executionContext.getErrors());
        });

        // dispatched the subscription query
        executionStrategyCtx.onDispatched(overallResult);
        overallResult.whenComplete(executionStrategyCtx::onCompleted);

        return overallResult;
    }

    private CompletableFuture<Publisher<Object>> createSourceEventStream(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {
        ExecutionStrategyParameters newParameters = firstFieldOfSubscriptionSelection(parameters);

        CompletableFuture<FetchedValue> fieldFetched = fetchField(executionContext, newParameters);
        return fieldFetched.thenApply(fetchedValue -> {
            Object publisher = fetchedValue.getFetchedValue();
            if (publisher != null) {
                assertTrue(publisher instanceof Publisher, () -> "Your data fetcher must return a Publisher of events when using graphql subscriptions");
            }
            //noinspection unchecked
            return (Publisher<Object>) publisher;
        });
    }

    private CompletableFuture<ExecutionResult> executeSubscriptionEvent(ExecutionContext executionContext, ExecutionStrategyParameters parameters, Object eventPayload) {
        Instrumentation instrumentation = executionContext.getInstrumentation();

        ExecutionContext newExecutionContext = executionContext.transform(builder -> builder
                .root(eventPayload)
                .resetErrors()
        );
        ExecutionStrategyParameters newParameters = firstFieldOfSubscriptionSelection(parameters);
        ExecutionStepInfo subscribedFieldStepInfo = createSubscribedFieldStepInfo(executionContext, newParameters);

        InstrumentationFieldParameters i13nFieldParameters = new InstrumentationFieldParameters(executionContext, () -> subscribedFieldStepInfo);
        InstrumentationContext<ExecutionResult> subscribedFieldCtx = nonNullCtx(instrumentation.beginSubscribedFieldEvent(
                i13nFieldParameters, executionContext.getInstrumentationState()
        ));

        FetchedValue fetchedValue = unboxPossibleDataFetcherResult(newExecutionContext, parameters, eventPayload);
        FieldValueInfo fieldValueInfo = completeField(newExecutionContext, newParameters, fetchedValue);
        CompletableFuture<ExecutionResult> overallResult = fieldValueInfo
                .getFieldValue()
                .thenApply(executionResult -> wrapWithRootFieldName(newParameters, executionResult));

        // dispatch instrumentation so they can know about each subscription event
        subscribedFieldCtx.onDispatched(overallResult);
        overallResult.whenComplete(subscribedFieldCtx::onCompleted);

        // allow them to instrument each ER should they want to
        InstrumentationExecutionParameters i13nExecutionParameters = new InstrumentationExecutionParameters(
                executionContext.getExecutionInput(), executionContext.getGraphQLSchema(), executionContext.getInstrumentationState());

        overallResult = overallResult.thenCompose(executionResult -> instrumentation.instrumentExecutionResult(executionResult, i13nExecutionParameters, executionContext.getInstrumentationState()));
        return overallResult;
    }

    private ExecutionStrategyParameters firstFieldOfSubscriptionSelection(ExecutionStrategyParameters parameters) {
        MergedSelectionSet fields = parameters.getFields();
        MergedField firstField = fields.getSubField(fields.getKeys().get(0));

        ResultPath fieldPath = parameters.getPath().segment(mkNameForPath(firstField.getSingleField()));
        return parameters.transform(builder -> builder.field(firstField).path(fieldPath));
    }

    private ExecutionStepInfo createSubscribedFieldStepInfo(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {
        Field field = parameters.getField().getSingleField();
        GraphQLObjectType parentType = (GraphQLObjectType) parameters.getExecutionStepInfo().getUnwrappedNonNullType();
        GraphQLFieldDefinition fieldDef = getFieldDef(executionContext.getGraphQLSchema(), parentType, field);
        return createExecutionStepInfo(executionContext, parameters, fieldDef, parentType);
    }

    private ExecutionResult wrapWithRootFieldName(ExecutionStrategyParameters parameters, ExecutionResult executionResult) {
        String rootFieldName = getRootFieldName(parameters);
        return new ExecutionResultImpl(
                singletonMap(rootFieldName, executionResult.getData()),
                executionResult.getErrors()
        );
    }

    private String getRootFieldName(ExecutionStrategyParameters parameters) {
        Field rootField = parameters.getField().getSingleField();
        return rootField.getResultKey();
    }
}
