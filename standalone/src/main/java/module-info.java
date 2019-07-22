module standalone {
    requires commons.cli;
    requires org.eclipse.jetty.plus;
    requires org.eclipse.jetty.webapp;
    requires jersey.container.servlet.core;
    requires java.persistence;
    requires lombok;
    requires shiro.web;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires bank.rest.service.jersey;
    requires jetty.servlet.api;

    requires org.mapstruct.processor;

    requires org.slf4j;

}