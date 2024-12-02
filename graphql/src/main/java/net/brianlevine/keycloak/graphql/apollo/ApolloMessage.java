package net.brianlevine.keycloak.graphql.apollo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = ApolloMessageDeserializer.class)
@JsonSerialize(using = ApolloMessageSerializer.class)
public class ApolloMessage {
    private final MessageType type;
    private final String id;
    private Object payload;

    private ApolloMessage() {
        type = null;
        id = null;
        payload = null;
    }

    public ApolloMessage(@Nonnull MessageType type, @Nonnull String id) {
        this(type, id, null);
    }

    public ApolloMessage(@Nonnull MessageType type, @Nonnull Object payload) {
        this(type, null, payload);
    }

    public ApolloMessage(@Nonnull MessageType type, @Nullable String id, @Nullable Object payload) {
        this.type = type;
        this.id = id;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static ApolloMessage fromJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApolloMessage message =  objectMapper.readValue(json, ApolloMessage.class);

        return message;
    }

}
