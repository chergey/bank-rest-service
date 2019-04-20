package org.elcer.accounts.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.ws.rs.core.Link;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;


@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule()
                    .addSerializer(new LinkSerializer(Link.class))
                    .addDeserializer(Link.class, new LinkDeserializer(Link.class))
            );

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return OBJECT_MAPPER;
    }
}
