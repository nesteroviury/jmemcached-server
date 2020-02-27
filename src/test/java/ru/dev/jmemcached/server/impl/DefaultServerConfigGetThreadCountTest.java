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
public class DefaultServerConfigGetThreadCountTest extends AbstractDefaultServerConfigTest {
    @DataPoints
    public static String[][] testCases = new String[][]{
            {"0", " should be >= 1"},
            {"qw", " should be a number"}
    };
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    private void setUpDefaultServerConfig(String property, String... testData) {
        String value = testData[0];
        String message = testData[1];
        Properties properties = new Properties();
        properties.setProperty(property, value);
        exception.expect(JMemcachedException.class);
        exception.expectMessage(is(property + message));

        defaultServerConfig = createDefaultServerConfigMock(properties);
    }

    @Theory
    public void getInitThreadCount(final String... testData) {
        setUpDefaultServerConfig("jmemcached.server.init.thread.count", testData);
        defaultServerConfig.getInitThreadCount();
    }

    @Theory
    public void getMaxThreadCount(final String... testData) {
        setUpDefaultServerConfig("jmemcached.server.max.thread.count", testData);
        defaultServerConfig.getMaxThreadCount();
    }
}
