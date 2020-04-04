package ru.dev.jmemcached.server;

import ru.dev.jmemcached.server.impl.JmemcachedServerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServiceWrapper {
    private static final Server SERVER = createServer();

    private static Server createServer() {
        return JmemcachedServerFactory.buildNewServer(getServerProperties());

    }

    private static Properties getServerProperties() {
        Properties properties = new Properties();
        String pathToServerProperties = System.getProperty("server-prop");
        try (InputStream inputStream = new FileInputStream(pathToServerProperties)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args) {
        if ("start".equals(args[0])) {
            start(args);
        } else if ("stop".equals(args[0])) {
            stop(args);
        }
    }

    public static void stop(String[] args) {
        SERVER.stop();
    }

    public static void start(String[] args) {
        SERVER.start();
    }

}
