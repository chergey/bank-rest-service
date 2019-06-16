package org.elcer.accounts.app;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.glassfish.jersey.message.internal.JerseyLink;

import javax.ws.rs.core.Link;
import java.io.IOException;

public class LinkDeserializer extends StdDeserializer<Link> {
    public LinkDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Link deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        String href = node.get("href").asText();
        String rel = node.get("rel").asText();
        String title = node.get("title").asText();
        Link link = JerseyLink.fromUri(href).rel(rel).title(title).build();
        return link;
    }
}