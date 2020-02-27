package ru.dev.jmemcached.server.impl;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import ru.dev.jmemcached.common.exception.JMemcachedException;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;

@RunWith(Theories.class)
public class DefaultServerConfigGetServerPortTest extends AbstractDefaultServerConfigTest {
    @DataPoints
    public static String[][] testCases = new String[][]{
            {"-1", "jmemcached.server.port should be between 0 and 65535"},
            {"65536", "jmemcached.server.port should be between 0 and 65535"},
            {"qw", "jmemcached.server.port should be a number"}
    };
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    @Theory
    public void getServerPort(final String... testData) {
        String value = testData[0];
        String message = testData[1];
        Properties properties = new Properties();
        properties.setProperty("jmemcached.server.port", value);
        exception.expect(JMemcachedException.class);
        exception.expectMessage(is(message));
        defaultServerConfig = createDefaultServerConfigMock(properties);

        defaultServerConfig.getServerPort();
    }
}
