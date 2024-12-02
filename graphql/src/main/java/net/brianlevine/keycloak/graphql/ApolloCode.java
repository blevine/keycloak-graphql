package net.brianlevine.keycloak.graphql;

import jakarta.websocket.CloseReason;

public enum ApolloCode implements CloseReason.CloseCode {
    InternalServerError(4500),
    InternalClientError(4005),
    BadRequest(4400),
    BadResponse(4004),
    Unauthorized(4401),
    Forbidden(4403),
    SubprotocolNotAcceptable(4406),
    ConnectionInitialisationTimeout(4408),
    ConnectionAcknowledgementTimeout(4504),
    SubscriberAlreadyExists(4409),
    TooManyInitialisationRequests(4429);

    private final int code;

    ApolloCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
