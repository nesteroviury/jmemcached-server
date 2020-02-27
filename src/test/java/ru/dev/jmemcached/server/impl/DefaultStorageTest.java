package ru.dev.jmemcached.server.impl;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import ru.dev.jmemcached.common.protocol.model.Status;
import ru.dev.jmemcached.server.ServerConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultStorageTest {
    private ExecutorService executorService;
    private ServerConfig serverConfig;
    private DefaultStorage defaultStorage;

    @Before
    public void before() {
        executorService = mock(ExecutorService.class);
        serverConfig = mock(ServerConfig.class);
        when(serverConfig.getClearDataIntervalInMs()).thenReturn(10);
        defaultStorage = new DefaultStorage(serverConfig) {
            @Override
            protected ExecutorService createClearExpiredDataExecutorService() {
                return executorService;
            }
        };
        defaultStorage.put("test", TimeUnit.SECONDS.toMillis(1), new byte[]{5, 6, 7});
    }

    @Test
    public void startClearExpiredDataExecutorService() throws IllegalAccessException {
        Runnable clearExpiredDataJob = (Runnable) FieldUtils.getDeclaredField(DefaultStorage.class,
                "clearExpiredDataJob", true).get(defaultStorage);

        verify(executorService).submit(clearExpiredDataJob);
    }

    @Test
    public void close() throws Exception {
        defaultStorage.close();

        verify(executorService, never()).shutdown();
        verify(executorService, never()).shutdownNow();
    }

    @Test
    public void createClearExpiredDataThreadFactory() {
        ThreadFactory threadFactory = defaultStorage.createClearExpiredDataThreadFactory();
        Thread thread = threadFactory.newThread(mock(Runnable.class));

        assertTrue(thread.isDaemon());
        assertEquals(Thread.MIN_PRIORITY, thread.getPriority());
        assertEquals("clearExpiredDataJodThread", thread.getName());
    }

    @Test
    public void putAdded() {
        Status status = defaultStorage.put("key", null, new byte[]{1, 2, 3});

        assertEquals(Status.ADDED, status);
    }

    @Test
    public void putReplaced() {
        Status status = defaultStorage.put("test", null, new byte[]{1, 2, 3});

        assertEquals(Status.REPLACED, status);
    }

    @Test
    public void getSuccess() {
        byte[] data = defaultStorage.get("test");

        assertArrayEquals(new byte[]{5, 6, 7}, data);
    }

    @Test
    public void getNotFound() {
        byte[] data = defaultStorage.get("not_found");

        assertNull(data);
    }

    @Test
    public void getExpired() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1100);
        byte[] data = defaultStorage.get("test");

        assertNull(data);
    }

    @Test
    public void removeSuccess() {
        Status status = defaultStorage.remove("test");

        assertEquals(Status.REMOVED, status);
    }

    @Test
    public void removeNotFound() {
        Status status = defaultStorage.remove("not_found");

        assertEquals(Status.NOT_FOUND, status);
    }

    @Test
    public void removeExpired() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1100);
        Status status = defaultStorage.remove("test");

        assertEquals(Status.NOT_FOUND, status);
    }

    @Test
    public void clear() {
        Status status = defaultStorage.clear();

        assertEquals(Status.CLEARED, status);
        assertNull(defaultStorage.get("test"));
    }
}
