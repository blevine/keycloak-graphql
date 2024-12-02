package net.brianlevine.keycloak.graphql.apollo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ApolloMessageSerializer extends StdSerializer<ApolloMessage> {
    public ApolloMessageSerializer() {
        this(null);
    }

    protected ApolloMessageSerializer(Class<ApolloMessage> t) {
        super(t);
    }

    @Override
    public void serialize(ApolloMessage value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        if (value.getId() != null) {
            gen.writeStringField("id", value.getId());
        }

        gen.writeStringField("type", value.getType().toString());

        if (value.getPayload() != null) {
            gen.writeObjectField("payload", value.getPayload());
        }

        gen.writeEndObject();
    }

}
