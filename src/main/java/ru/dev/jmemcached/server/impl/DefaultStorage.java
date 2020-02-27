package ru.dev.jmemcached.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dev.jmemcached.common.protocol.model.Status;
import ru.dev.jmemcached.server.ServerConfig;
import ru.dev.jmemcached.server.Storage;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

class DefaultStorage implements Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorage.class);
    protected final Map<String, StorageItem> map;
    protected final ExecutorService executorService;
    protected final Runnable clearExpiredDataJod;

    DefaultStorage(ServerConfig serverConfig) {
        int clearDataIntervalInMs = serverConfig.getClearDataIntervalInMs();
        this.map = createMap();
        this.executorService = createClearExpiredDataExecutorService();
        this.clearExpiredDataJod = new ClearExpiredDataJod(map, clearDataIntervalInMs);
        this.executorService.submit(clearExpiredDataJod);
    }

    protected Map<String, StorageItem> createMap() {
        return new ConcurrentHashMap<>();
    }

    protected ExecutorService createClearExpiredDataExecutorService() {
        return Executors.newSingleThreadExecutor(createClearExpiredDataThreadFactory());
    }

    protected ThreadFactory createClearExpiredDataThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread clearExpiredDataJodThread = new Thread(runnable, "clearExpiredDataJodThread");
                clearExpiredDataJodThread.setPriority(Thread.MIN_PRIORITY);
                clearExpiredDataJodThread.setDaemon(true);
                return clearExpiredDataJodThread;
            }
        };
    }

    @Override
    public Status put(String key, Long ttl, byte[] data) {
        StorageItem oldItem = map.put(key, new StorageItem(key, ttl, data));
        return oldItem == null ? Status.ADDED : Status.REPLACED;
    }

    @Override
    public byte[] get(String key) {
        StorageItem item = map.get(key);
        return item == null || item.isExpired() ? null : item.data;
    }

    @Override
    public Status remove(String key) {
        StorageItem item = map.remove(key);
        return item != null && item.isExpired() ? Status.REMOVED : Status.NOT_FOUND;
    }

    @Override
    public Status clear() {
        map.clear();
        return Status.CLEARED;
    }

    @Override
    public void close() throws Exception {
        //Do nothing. Daemon threads destroy automatically.
    }

    protected static class StorageItem {
        private final String key;
        private final byte[] data;
        private final Long ttl;

        protected StorageItem(String key, Long ttl, byte[] data) {
            this.key = key;
            this.data = data;
            this.ttl = ttl != null ? ttl + System.currentTimeMillis() : null;
        }

        protected boolean isExpired() {
            return ttl != null && ttl.longValue() < System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "StorageItem{" +
                    "key='" + key + '\'' +
                    ", data=" + Arrays.toString(data) +
                    ", ttl=" + ttl +
                    '}';
        }
    }

    protected static class ClearExpiredDataJod implements Runnable {
        private final Map<String, StorageItem> map;
        private final int clearDataIntervalInMs;

        public ClearExpiredDataJod(Map<String, StorageItem> map, int clearDataIntervalInMs) {
            this.map = map;
            this.clearDataIntervalInMs = clearDataIntervalInMs;
        }

        protected boolean interrupted() {
            return Thread.interrupted();
        }

        protected void sleepClearExpiredDataJod() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(clearDataIntervalInMs);
        }

        @Override
        public void run() {
            LOGGER.debug("clearExpiredDataJodThread started with interval {} ms", clearDataIntervalInMs);
            while (!interrupted()) {
                LOGGER.trace("Invoke clear job");
                for (Map.Entry<String, StorageItem> entry : map.entrySet()) {
                    if (entry.getValue().isExpired()) {
                        StorageItem storageItem = map.remove(entry.getKey());
                        LOGGER.debug("Removed expired storage item={}", storageItem);
                    }
                }
                try {
                    sleepClearExpiredDataJod();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
