package ru.dev.jmemcached.server.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.dev.jmemcached.common.exception.JMemcachedException;
import ru.dev.jmemcached.common.protocol.impl.DefaultRequestConverter;
import ru.dev.jmemcached.common.protocol.impl.DefaultResponseConverter;
import ru.dev.jmemcached.server.ClientSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ThreadFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultServerConfigTest extends AbstractDefaultServerConfigTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    @Before
    public void before() {
        defaultServerConfig = createDefaultServerConfigMock(null);
    }

    @Test
    public void testDefaultInitState() throws Exception {
        try (DefaultServerConfig defaultServerConfig = new DefaultServerConfig(null)) {
            assertEquals(DefaultRequestConverter.class, defaultServerConfig.getRequestConverter().getClass());
            assertEquals(DefaultResponseConverter.class, defaultServerConfig.getResponseConverter().getClass());
            assertEquals(DefaultStorage.class, defaultServerConfig.getStorage().getClass());
            assertEquals(DefaultCommandHandler.class, defaultServerConfig.getCommandHandler().getClass());

            assertEquals(9010, defaultServerConfig.getServerPort());
            assertEquals(1, defaultServerConfig.getInitThreadCount());
            assertEquals(10, defaultServerConfig.getMaxThreadCount());
            assertEquals(10000, defaultServerConfig.getClearDataIntervalInMs());
        }
    }

    @Test
    public void getWorkerThreadFactory() {
        ThreadFactory threadFactory = defaultServerConfig.getWorkerThreadFactory();
        Thread thread = threadFactory.newThread(mock(Runnable.class));

        assertTrue(thread.isDaemon());
        assertEquals("Worker-0", thread.getName());

    }

    @Test
    public void close() throws Exception {
        defaultServerConfig.close();

        verify(storage).close();
    }

    @Test
    public void buildNewClientSocketHandler() {
        ClientSocketHandler clientSocketHandler = defaultServerConfig.buildNewClientSocketHandler(mock(Socket.class));

        assertEquals(DefaultClientSocketHandler.class, clientSocketHandler.getClass());
    }

    @Test
    public void verifyToString() {
        assertEquals("DefaultServerConfig: port=9010, initThreadCount=1, maxThreadCount=10, clearDataIntervalInMs=10000 ms",
                defaultServerConfig.toString());
    }

    @Test
    public void loadApplicationPropertiesNotFound() {
        exception.expect(JMemcachedException.class);
        exception.expectMessage(is("Classpath resource not found: not_found.properties"));

        defaultServerConfig.loadApplicationProperties("not_found.properties");
    }

    @Test
    public void loadApplicationPropertiesIOException() {
        final IOException ioException = new IOException("IO");
        exception.expect(JMemcachedException.class);
        exception.expectMessage(is("Can't load application properties from classpath: server.properties"));
        exception.expectCause(is(ioException));

        defaultServerConfig = new DefaultServerConfig(null) {
            @Override
            protected InputStream getClassPathResourceInputStream(String classPathResource) {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw ioException;
                    }
                };
            }
        };
    }
}
