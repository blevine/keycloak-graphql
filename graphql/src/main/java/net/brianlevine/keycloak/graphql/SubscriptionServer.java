package net.brianlevine.keycloak.graphql;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class SubscriptionServer extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionServer.class);
  private static final int DEFAULT_PORT = 8081;

  @Override
  public void start() {

    String sPort = System.getenv("SUBSCRIPTION_PORT");
    int port = (sPort != null) ? Integer.parseInt(sPort) : DEFAULT_PORT;

    LOGGER.info("Starting SubscriptionServer on port {}...", port);

    Router router = Router.router(vertx);
    Map<String, Object> context = new HashMap<>();
    context.put("vertex", getVertx());

    //noinspection deprecation
    ApolloWSHandler h = ApolloWSHandler.create(GraphQLController.getSchema()).beforeExecute((a) -> {
      a.builder().graphQLContext(context);
    });
    router.route("/graphql").handler(h);

    HttpServerOptions httpServerOptions = new HttpServerOptions()
            .addWebSocketSubProtocol("graphql-ws")
            .setLogActivity(true);

    vertx.createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(port, "0.0.0.0")
            .onComplete(
                    (e) -> LOGGER.info("SubscriptionServer started on port {}", e.actualPort()),
                    (t) -> LOGGER.error("SubscriptionServer FAILED to start: ", t)
            ).onFailure((e) -> LOGGER.error("SubscriptionServer FAILED to start: ", e));
  }

//  private GraphQL createGraphQL() {
//    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();
//
//    SchemaParser schemaParser = new SchemaParser();
//    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
//
//    RuntimeWiring runtimeWiring = newRuntimeWiring()
//            .type("Subscription", builder -> builder.dataFetcher("links", this::linksFetcher))
//            .build();
//
//    SchemaGenerator schemaGenerator = new SchemaGenerator();
//    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
//
//    return GraphQL.newGraphQL(graphQLSchema)
//            .build();
//  }




//  private Publisher<Link> linksFetcher(DataFetchingEnvironment env) {
//    return Flowable.interval(1, TimeUnit.SECONDS) // Ticks
//      .zipWith(Flowable.fromIterable(links), (tick, link) -> link) // Emit link on each tick
//      .observeOn(RxHelper.scheduler(context)); // Observe on the verticle context thread
//  }
}
