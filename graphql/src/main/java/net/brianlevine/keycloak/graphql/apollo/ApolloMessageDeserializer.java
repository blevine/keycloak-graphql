package net.brianlevine.keycloak.graphql.apollo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApolloMessageDeserializer extends StdDeserializer<ApolloMessage> {
    public ApolloMessageDeserializer() {
        this(null);

    };
    public ApolloMessageDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ApolloMessage deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);
        String s = node.get("type").asText();
        MessageType type = MessageType.fromString(s);

        String id = node.has("id") ? node.get("id").asText() : null;

        JsonNode p = node.get("payload");
        Object payload = null;

        if (p != null) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, Object>> tr = new TypeReference<>(){};

            if (type == MessageType.Subscribe) {
                String operationName = p.has("operationName") ?  p.get("operationName").asText() : null;
                String query = p.has("query") ? p.get("query").asText() : null;

                Map<String, Object> variables = p.has("variables") ? mapper.convertValue(p.get("variables"), tr) : null;
                Map<String, Object> extensions = p.has("extensions") ? mapper.convertValue(p.get("extensions"), tr) : null;

                payload = new SubscribePayload(operationName, query, variables, extensions);
            }
            else {
                payload = mapper.convertValue(p, tr);
            }
        }

        return new ApolloMessage(type, id, payload);
    }
}
