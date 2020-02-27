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
public class DefaultServerConfigGetClearDataIntervalInMsTest extends AbstractDefaultServerConfigTest {
    @DataPoints
    public static String[][] testCases = new String[][]{
            {"999", "jmemcached.storage.clear.data.interval.ms should be >= 1000 ms"},
            {"qw", "jmemcached.storage.clear.data.interval.ms should be a number"}
    };
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    @Theory
    public void getClearDataIntervalInMs(final String... testData) {
        String value = testData[0];
        String message = testData[1];
        Properties properties = new Properties();
        properties.setProperty("jmemcached.storage.clear.data.interval.ms", value);
        exception.expect(JMemcachedException.class);
        exception.expectMessage(is(message));
        defaultServerConfig = createDefaultServerConfigMock(properties);

        defaultServerConfig.getClearDataIntervalInMs();
    }

}
