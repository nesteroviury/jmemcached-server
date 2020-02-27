package ru.dev.jmemcached;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class TestUtils {

    public static void setLoggerMockViaReflection(Class<?> clazz, Logger logger) throws IllegalAccessException {
        Field loggerField = FieldUtils.getField(clazz, "LOGGER", true);
        FieldUtils.removeFinalModifier(loggerField);
        loggerField.set(clazz, logger);
    }

}
