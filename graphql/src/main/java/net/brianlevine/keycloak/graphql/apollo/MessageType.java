package net.brianlevine.keycloak.graphql.apollo;

public enum MessageType {
    ConnectionInit("connection_init"),
    ConnectionAck("connection_ack"),
    Ping("ping"),
    Pong("pong"),
    Subscribe("subscribe"),
    Next("next"),
    Error("error"),
    Complete("complete"),
    Unknown("unknown");

    private final String name;

    MessageType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MessageType fromString(String name) {
        for (MessageType type : MessageType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }

        return Unknown;
    }
}
