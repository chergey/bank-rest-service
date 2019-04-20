package org.elcer.accounts.app;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.ws.rs.core.Link;
import java.io.IOException;


public class LinkSerializer extends StdSerializer<Link> {
    LinkSerializer(Class<Link> t) {
        super(t);
    }

    @Override
    public void serialize(Link value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("href", value.getUri().toASCIIString());
        gen.writeStringField("rel", value.getRel());
        gen.writeStringField("title", value.getTitle());
        gen.writeEndObject();
    }
}
