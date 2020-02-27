package ru.dev.jmemcached.server.impl;

import org.junit.Test;
import org.slf4j.Logger;
import ru.dev.jmemcached.TestUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class ClearExpiredDataJodTest {
    private Logger logger;
    private DefaultStorage.ClearExpiredDataJod clearExpiredDataJod;
    private Map<String, DefaultStorage.StorageItem> map;
    private Set<Map.Entry<String, DefaultStorage.StorageItem>> set;
    private Iterator<Map.Entry<String, DefaultStorage.StorageItem>> iterator;
    private int clearDataIntervalInMs = 10000;

    public void before() throws IllegalAccessException {
        logger = mock(Logger.class);
        map = mock(Map.class);
        set = mock(Set.class);
        iterator = mock(Iterator.class);
        when(map.entrySet()).thenReturn(set);
        when(set.iterator()).thenReturn(iterator);
        clearExpiredDataJod = spy(new DefaultStorage.ClearExpiredDataJod(map, clearDataIntervalInMs) {
            private boolean stop = true;

            @Override
            protected boolean interrupted() {
                stop = !stop;
                return stop;
            }

            @Override
            protected void sleepClearExpiredDataJod() throws InterruptedException {
                //do nothing
            }
        });
        TestUtils.setLoggerMockViaReflection(DefaultStorage.class, logger);
    }

    @Test
    public void verifyWhenMapIsEmpty() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(false);

        clearExpiredDataJod.run();

        verifyCommonOperations();
    }

    @Test
    public void verifyWhenMapEntryIsNotExpired() throws InterruptedException {
        Map.Entry<String, DefaultStorage.StorageItem> entry = mock(Map.Entry.class);
        DefaultStorage.StorageItem item = mock(DefaultStorage.StorageItem.class);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(entry);
        when(entry.getValue()).thenReturn(item);
        when(item.isExpired()).thenReturn(false);

        clearExpiredDataJod.run();

        verifyCommonOperations();
        verify(map, never()).remove(anyString());
        verify(logger, never()).debug("Removed expired storage item={}", item);
    }

    @Test
    public void verifyWhenMapEntryIsExpired() throws InterruptedException {
        Map.Entry<String, DefaultStorage.StorageItem> entry = mock(Map.Entry.class);
        DefaultStorage.StorageItem item = mock(DefaultStorage.StorageItem.class);
        String key = "key";
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(entry);
        when(entry.getKey()).thenReturn(key);
        when(map.remove(key)).thenReturn(item);
        when(entry.getValue()).thenReturn(item);
        when(item.isExpired()).thenReturn(true);

        clearExpiredDataJod.run();

        verifyCommonOperations();
        verify(map).remove(key);
        verify(logger).debug("Removed expired storage item={}", item);
    }

    @Test
    public void verifyWhenInterruptedException() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(false);
        clearExpiredDataJod = spy(new DefaultStorage.ClearExpiredDataJod(map, clearDataIntervalInMs) {
            @Override
            protected void sleepClearExpiredDataJod() throws InterruptedException {
                throw new InterruptedException("InterruptedException");
            }
        });

        clearExpiredDataJod.run();

        verify(logger).debug("clearExpiredDataJodThread started with interval {} ms", clearDataIntervalInMs);
        verify(logger).trace("Invoke clear job");
        verify(clearExpiredDataJod).sleepClearExpiredDataJod();
        verify(clearExpiredDataJod, times(1)).interrupted();
    }

    private void verifyCommonOperations() throws InterruptedException {
        verify(logger).debug("clearExpiredDataJodThread started with interval {} ms", clearDataIntervalInMs);
        verify(logger).trace("Invoke clear job");
        verify(clearExpiredDataJod).sleepClearExpiredDataJod();
        verify(clearExpiredDataJod, times(2)).interrupted();
    }
}
