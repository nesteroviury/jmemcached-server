package ru.dev.jmemcached.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dev.jmemcached.server.impl.JmemcachedServerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);
    private static final List<String> QUIT_CMDS = Collections.unmodifiableList(Arrays.asList("q", "quit", "exit"));

    public static void main(String[] args) {
        Thread.currentThread().setName("CLI-main thread");
        try {
            Server server = JmemcachedServerFactory.buildNewServer(null);
            server.start();
            waitForStopCommand(server);
        } catch (Throwable throwable) {
            LOGGER.error("Can't execute command: " + throwable.getMessage(), throwable);
        }
    }

    private static void waitForStopCommand(Server server) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (true) {
                String cmd = scanner.nextLine();
                if (QUIT_CMDS.contains(cmd)) {
                    server.stop();
                    break;
                } else {
                    LOGGER.error("undefined command: " + cmd + "! To shutdown server please type: q");
                }
            }
        }
    }
}
