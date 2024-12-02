package net.brianlevine.keycloak.graphql.apollo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class SubscribePayloadDeserializer extends StdDeserializer<SubscribePayload> {
    protected SubscribePayloadDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SubscribePayload deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JacksonException {
        return null;
    }
}
