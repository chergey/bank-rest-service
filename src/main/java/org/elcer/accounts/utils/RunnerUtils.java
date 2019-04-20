package org.elcer.accounts.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.elcer.accounts.app.AppConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

@UtilityClass
@Slf4j
public class RunnerUtils {

    public static final int DEFAULT_PORT = 8082;

    private static final String PORT_PARAM = "port";


    public static void startServer(int port) {
        startServer(port, true);
    }

    @SuppressWarnings("UnusedReturnValue")
    @SneakyThrows
    public static Server startServer(int port, boolean wait) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        Server jettyServer = new Server(port);
        jettyServer.setHandler(context);
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, AppConfig.class.getName());

        try {
            jettyServer.start();
            if (wait)
                jettyServer.join();
        } catch (Exception e) {
            log.error("Error in server", e);
            throw e;
        }

        return jettyServer;
    }


    public static void parseAndRun(String[] args) {
        Options options = new Options();

        Option input = new Option(PORT_PARAM, PORT_PARAM, true, "port to use");
        input.setRequired(false);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.error("Error while parsing options", e);
            formatter.printHelp("Bank service", options);
            System.exit(1);
            return;
        }

        int port = Integer.parseInt(cmd.getOptionValue(PORT_PARAM, Integer.toString(DEFAULT_PORT)));
        startServer(port);

    }
}
