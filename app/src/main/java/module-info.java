module bank.rest.service.jersey {
    exports org.elcer.accounts.app;
    requires lombok;
    requires org.apache.commons.lang3;
    requires hk2.api;
    requires hk2.locator;
    requires javax.inject;
    requires java.persistence;
    requires java.validation;
    requires com.fasterxml.jackson.databind;
    requires java.ws.rs;
    requires com.fasterxml.jackson.annotation;
    requires org.eclipse.persistence.core;
    requires annotation.detector;
    requires HK2Utilities;
    requires shiro.jaxrs;
    requires jersey.media.json.jackson;
    requires shiro.core;
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires java.annotation;
    requires org.apache.commons.collections4;
    requires jersey.common;
    requires jersey.server;
    requires jersey.hk2;
    requires cache;
    requires java.naming;
    requires org.mapstruct.processor;
    requires java.logging;

    uses org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;


}