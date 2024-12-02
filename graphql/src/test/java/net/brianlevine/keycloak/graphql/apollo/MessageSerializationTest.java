package net.brianlevine.keycloak.graphql.apollo;


import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MessageSerializationTest {
    @Test
    public void shouldSerializeApolloMessage() throws Exception {
        ApolloMessage message = new ApolloMessage(MessageType.Ping, null, null);
        String json = message.toJson();
        String expected = "{\"type\":\"ping\"}";
        assertEquals(expected, json);

    }

    @Test
    public void shouldDeserializeApolloMessage() throws Exception {
        String json = "{\"type\":\"ping\"}";
        ApolloMessage message = ApolloMessage.fromJson(json);
        assertNotNull(message);
        assertEquals(MessageType.Ping, message.getType());
    }

    @Test
    public void shouldSerializeApolloMessageWithPayload() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("payload", "payload");
        ApolloMessage message = new ApolloMessage(MessageType.ConnectionAck, null, payload);
        String json = message.toJson();

        String expected = "{\"type\":\"connection_ack\",\"payload\":{\"payload\":\"payload\"}}";

        Assert.equals(expected, json);
    }

    @Test
    public void shouldDeserializeApolloMessageWithPayload() throws Exception {
        String json = "{\"type\":\"connection_ack\",\"payload\":{\"payload\":\"payload\"}}";
        ApolloMessage message = ApolloMessage.fromJson(json);
        assertNotNull(message);
        assertEquals(MessageType.ConnectionAck, message.getType());

        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("payload", "payload");
        assertEquals(expectedPayload, message.getPayload());
    }

    @Test
    public void shouldSerializeSubscribeMessage() throws Exception {
        String opName = "testOp";
        String query = "testQuery";
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "val1");
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("ext1", "val2");

        SubscribePayload payload = new SubscribePayload(opName, query, variables, extensions);
        ApolloMessage message = new ApolloMessage(MessageType.Subscribe, "123", payload);

        String expected = "{\"id\":\"123\",\"type\":\"subscribe\",\"payload\":{\"operationName\":\"testOp\",\"query\":\"testQuery\",\"variables\":{\"var1\":\"val1\"},\"extensions\":{\"ext1\":\"val2\"}}}";

        String json = message.toJson();
        assertEquals(expected, json);
    }

    @Test
    public void shouldDeserializeSubscribeMessage() throws Exception {
        String json = "{\"type\":\"subscribe\",\"id\":\"123\",\"payload\":{\"operationName\":\"testOp\",\"query\":\"testQuery\",\"variables\":{\"var1\":\"val1\"},\"extensions\":{\"ext1\":\"val2\"}}}";

        ApolloMessage message = ApolloMessage.fromJson(json);
        assertNotNull(message);
        assertEquals(MessageType.Subscribe, message.getType());
        assertEquals("123", message.getId());

        String opName = "testOp";
        String query = "testQuery";
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "val1");
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("ext1", "val2");

        SubscribePayload payload = new SubscribePayload(opName, query, variables, extensions);

        assertEquals(payload, (SubscribePayload) message.getPayload());
    }
}
