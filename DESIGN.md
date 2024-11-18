# Design notes/issues
## Subscriptions
Subscription support currently consists of the following:
- `SubscriptionServer`. Sets up an HTTP/WebSockets server using Vert.x's support for WebSockets and GraphQL.
I'd actually like to find a way to integrate WebSockets more directly with the Keycloak server so I don't have to
start a second server on a different port. However, I haven't figured out how to do this.
- `MulticastEventListenerProvider(Factory)`. This is a Keycloak EventListener that hooks Events and AdminEvents
and publishes them via an rxjava3 MulticastProcessor.
- The "events" subscription (EventsSubscription class using graphql-spqr) that returns the MulticastProcessor 
as its Publisher which publishes `KeycloakEventTypes`. Subscriptions need to return an instance of Publisher to which clients subscribe.
- `KeycloakEventType`. A wrapper around Keycloak Events and Admin Events.

### Problems with Subscriptions
- As mentioned above, **I'd like to find a better way to integrate WebSockets into the Keycloak server** without requiring
a separate server on a different port.
- **How to get "context" information to various resolvers**. If you look at KeycloakEventType, you'll see certain
fields implemented as "subqueries." For example, the realm field is implemented like so:

      @GraphQLQuery
      public RealmType getRealm(@GraphQLEnvironment ResolutionEnvironment env) {
          GraphQLContext ctx = env.dataFetchingEnvironment.getGraphQlContext();
          String realmId = event != null ? event.getRealmId() : adminEvent.getRealmId();
          return new RealmQuery().getRealmById(realmId, ctx);
      }

You can see that a GraphQL context is passed to the getRealmById() method. These query methods expect the
context to be populated with a KeycloakSession and HTTP headers. The headers need to include the Authorization
header with the OIDC access token as its value. If the caller includes the "realm" field in the subscription query,
the getRealm() method will be called each time the event fires. So this invocation is actually coming from the server-
side as opposed to an HTTP request which would be the case if this method were called as the result of a query
coming from a client of the API. The getRealmByID() method needs the KeycloakSession and access_token in order
to perform access control on the RealmType and its fields. To deal with some of this, I created a custom
SubscriptionExecutionStrategy (KeycloakSubscriptionExecutionStrategy) which creates a new KeycloakSession prior
to executing the subscription like so:

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

This was somewhat successful although I'm seeing warnings that the transaction was completed in multiple threads. This
is probably due to the fact that I don't completely understand Vert.x async Java programming all that well.

