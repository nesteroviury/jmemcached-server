package ru.dev.jmemcached.server.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import ru.dev.jmemcached.TestUtils;
import ru.dev.jmemcached.server.ServerConfig;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultServerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DefaultServer defaultServer;
    private Logger logger;
    private ServerConfig serverConfig;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Thread mainServerThread;

    @Before
    public void before() throws IllegalAccessException {
        logger = mock(Logger.class);
        serverConfig = mock(ServerConfig.class);
        serverSocket = mock(ServerSocket.class);
        executorService = mock(ExecutorService.class);
        mainServerThread = mock(Thread.class);
        when(serverConfig.toString()).thenReturn("serverConfig");

        TestUtils.setLoggerMockViaReflection(DefaultServer.class, logger);
    }

    @Test
    public void createMainServerThread() {
        defaultServer = new DefaultServer(serverConfig) {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }

            @Override
            protected ServerSocket createServerSocket() {
                return serverSocket;
            }
        };
        Thread thread = defaultServer.createMainServerThread(mock(Runnable.class));
        assertEquals(Thread.MAX_PRIORITY, thread.getPriority());
        assertEquals("Main Server Thread", thread.getName());
        assertFalse(thread.isDaemon());
        assertFalse(thread.isAlive());
    }
}
