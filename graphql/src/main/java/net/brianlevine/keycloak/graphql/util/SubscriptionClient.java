package net.brianlevine.keycloak.graphql.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.web.handler.graphql.ApolloWSMessageType;


public class SubscriptionClient extends AbstractVerticle {
    private HttpClient httpClient;

    public static void main(String[] args) {
        Launcher.executeCommand("run", SubscriptionClient.class.getName());
    }

    @Override
    public void start() {
        vertx.exceptionHandler((t) -> {
            System.err.println("VERTX Exception occurred: " + t);

            if (t instanceof RestartException) {
                System.out.println("Reissuing subscription...");
                setupAndSend();
            }
        });

        setupAndSend();
    }

    private void setupAndSend() {
        if (httpClient != null) {
            httpClient.close();
        }

        httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8081));

        String sub1 = """
                subscription {
                	events {
                      id
                      details
                      type
                      resourceType
                      resourcePath
                      realm {
                        name
                      }
                    }
                }
                """;


        sendSubscription(httpClient, sub1);
    }

    private void sendSubscription(HttpClient httpClient, String query)  {
        KeycloakAuth.discover(
                        vertx,
                        new OAuth2Options()
                                .setClientId("keycloak-websocket")
                                .setClientSecret("bKggQcsaUsC2WkS5rhrhgdDB91sjgkXf")
                                .setSite("http://localhost:8080/realms/master")
                                .setTenant("master")
                                .setFlow(OAuth2FlowType.PASSWORD))
                .onSuccess(oauth2 -> {
                    JsonObject tokenConfig = new JsonObject()
                            .put("username", "admin")
                            .put("password", "admin");

                    oauth2.authenticate(tokenConfig)
                            .onSuccess(user -> {
                                // Get the access token object
                                // (the authorization code is given from the previous step).

                                // you can now make requests using the
                                // `Authorization` header and the value:
                                String accessToken = user.principal().getString("access_token");
                                String refreshToken = user.principal().getString("refresh_token");

                                System.out.println("Access token: " + accessToken);

                                WebSocketConnectOptions options = new WebSocketConnectOptions();
                                options.setURI("/graphql");
                                options.addHeader("Authorization", "Bearer " + accessToken);
                                options.addHeader("refreshToken", refreshToken);

                                httpClient.webSocket(options, websocketRes -> {
                                    if (websocketRes.succeeded()) {
                                        WebSocket webSocket = websocketRes.result();
                                        
                                        webSocket.handler(message -> {
                                            System.out.println(message.toJsonObject().encodePrettily());
                                            JsonObject payload = message.toJsonObject().getJsonObject("payload");

                                            if (isError(payload)) {
                                                String code = getErrorCode(payload);

                                                System.out.println("Error code: " + code);

                                                if (isTokenRefreshRequired(code)) {
                                                    System.out.println("Token refresh required");
                                                    webSocket.close();
                                                    throw new RestartException("Token refresh required");
                                                }
                                            }
                                        }).exceptionHandler(error -> {
                                            System.out.println("Error (websocket exception handler): " + error.getMessage());
                                        });

                                        JsonObject request = new JsonObject()
                                                .put("id", "1")
                                                .put("type", ApolloWSMessageType.CONNECTION_INIT.getText());

                                        webSocket.write(request.toBuffer());

                                        request = new JsonObject()
                                                .put("id", "1")
                                                .put("type", ApolloWSMessageType.START.getText())
                                                .put("payload", new JsonObject()
                                                        .put("query", query));
                                        webSocket.write(request.toBuffer());
                                    } else {
                                        System.out.println("Websocket FAILED: " + websocketRes.cause().getMessage());
                                        websocketRes.cause().printStackTrace();
                                    }
                                });

                            })
                            .onFailure(err -> {
                                System.err.println("Access Token Error: " + err.getMessage());
                            });
                })
                .onFailure(err -> {
                    System.err.println("Discover Error: " + err.getMessage());
                });

    }


    private boolean isTokenRefreshRequired(String code) {
        return "NotAuthorizedException".equals(code);
    }

    private boolean isError(JsonObject payload) {
        return payload != null && payload.containsKey("errors");
    }

    //payload.getJsonArray("errors").getJsonObject(0).getJsonObject("extensions").getString("code")
    private String getErrorCode(JsonObject payload) {
        String code = null;

        if (payload != null) {
            JsonArray errors = payload.getJsonArray("errors");
            if (errors != null) {
                JsonObject extensions = errors.getJsonObject(0).getJsonObject("extensions");

                if (extensions != null) {
                    code = extensions.getString("code");
                }
            }
        }

        return code;
    }

    private static class RestartException extends RuntimeException {
        RestartException(String message) {
            super(message);
        }
    }
}
