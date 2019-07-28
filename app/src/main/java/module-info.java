module bank.rest.service.jersey {

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
    requires java.logging;


    exports org.elcer.accounts.app;
    exports org.elcer.accounts.db;
    exports org.elcer.accounts.db.cache;
    exports org.elcer.accounts.model;
    opens org.elcer.accounts.services;
    opens org.elcer.accounts.hk2;

    opens org.elcer.accounts.resource;
    opens org.elcer.accounts.model to org.eclipse.persistence.core;
    opens org.elcer.accounts.exceptions.mappers to hk2.locator;

    exports org.elcer.accounts.resource to jersey.server;
    exports org.elcer.accounts.services to hk2.locator;
    exports org.elcer.accounts.services.synchronizers to hk2.locator;
    exports org.elcer.accounts.hk2 to hk2.locator;

    uses org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;


}