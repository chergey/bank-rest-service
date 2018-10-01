package org.elcer.accounts.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.elcer.accounts.app.AppConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class RunnerUtils {

    public static final int DEFAULT_PORT = 8082;

    private static final Logger logger = LoggerFactory.getLogger(RunnerUtils.class);

    public static void startServer(int port) {
        startServer(port, true);
    }

    @SuppressWarnings("UnusedReturnValue")
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
            logger.error("Error in server", e);
            ExceptionUtils.sneakyThrow(e);
        }

        return jettyServer;
    }


    public static void parseAndRun(String[] args) {
        Options options = new Options();

        Option input = new Option("port", "port", true, "port");
        input.setRequired(false);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("bank service", options);
            System.exit(1);
            return;
        }

        int port = Integer.parseInt(cmd.getOptionValue("port", Integer.toString(DEFAULT_PORT)));
        startServer(port);

    }
}
