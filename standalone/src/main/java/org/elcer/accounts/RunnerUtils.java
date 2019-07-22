package org.elcer.accounts;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.elcer.accounts.app.AppConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

@UtilityClass
@Slf4j
public class RunnerUtils {

    public static final int DEFAULT_PORT = 8082;

    private static final String PORT_PARAM = "port";

    private static final String ALL_PATHS = "/*";

    private static final String ACCOUNTS_UNIT = "accounts";

    @SuppressWarnings("UnusedReturnValue")
    @SneakyThrows
    public static Server startServer(int port) {
        //  ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        Server server = new Server(port);

        EntityManagerFactory accounts = Persistence.createEntityManagerFactory(ACCOUNTS_UNIT);


//        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
//        classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
//                "org.eclipse.jetty.plus.webapp.EnvConfiguration",
//                "org.eclipse.jetty.plus.webapp.PlusConfiguration");


        var context = new WebAppContext();
        context.setResourceBase("/");
        context.setContextPath("/");

        new EnvEntry(context, ACCOUNTS_UNIT, accounts, true);

        server.setHandler(context);
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, ALL_PATHS);
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, AppConfig.class.getName());
        jerseyServlet.setInitParameter("unit:accounts", ACCOUNTS_UNIT);
        context.addEventListener(new EnvironmentLoaderListener());


        context.addFilter(ShiroFilter.class, ALL_PATHS, EnumSet.of(DispatcherType.FORWARD,
                DispatcherType.INCLUDE, DispatcherType.REQUEST, DispatcherType.ERROR));
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("Error in server", e);
            throw e;
        }

        return server;
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
