package ru.dev.jmemcached.server.impl;

import ru.dev.jmemcached.common.exception.JMemcachedConfigException;
import ru.dev.jmemcached.common.protocol.RequestConverter;
import ru.dev.jmemcached.common.protocol.ResponseConverter;
import ru.dev.jmemcached.common.protocol.impl.DefaultRequestConverter;
import ru.dev.jmemcached.common.protocol.impl.DefaultResponseConverter;
import ru.dev.jmemcached.server.ClientSocketHandler;
import ru.dev.jmemcached.server.CommandHandler;
import ru.dev.jmemcached.server.ServerConfig;
import ru.dev.jmemcached.server.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

class DefaultServerConfig implements ServerConfig {
    private final Properties applicationProperties = new Properties();
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final Storage storage;
    private final CommandHandler commandHandler;

    DefaultServerConfig(Properties overrideApplicationProperties) {
        loadApplicationProperties("server.properties");
        if (overrideApplicationProperties != null) {
            applicationProperties.putAll(overrideApplicationProperties);
        }
        requestConverter = createRequestConverter();
        responseConverter = createResponseConverter();
        storage = createStorage();
        commandHandler = createCommandHandler();
    }

    protected InputStream getClassPathResourceInputStream(String classPathResource) {
        return getClass().getClassLoader().getResourceAsStream(classPathResource);
    }

    protected void loadApplicationProperties(String classPathResource) {
        try (InputStream inputStream = getClassPathResourceInputStream(classPathResource)) {
            if (inputStream == null) {
                throw new JMemcachedConfigException("Classpath resource not found: " + classPathResource);
            }
            applicationProperties.load(inputStream);
        } catch (IOException e) {
            throw new JMemcachedConfigException("Can't load application properties from classpath: " + classPathResource, e);
        }
    }

    protected CommandHandler createCommandHandler() {
        return new DefaultCommandHandler(this);
    }

    protected Storage createStorage() {
        return new DefaultStorage(this);
    }

    protected ResponseConverter createResponseConverter() {
        return new DefaultResponseConverter();
    }

    protected RequestConverter createRequestConverter() {
        return new DefaultRequestConverter();
    }

    @Override
    public RequestConverter getRequestConverter() {
        return requestConverter;
    }

    @Override
    public ResponseConverter getResponseConverter() {
        return responseConverter;
    }

    @Override
    public ThreadFactory getWorkerThreadFactory() {
        return new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread("Worker-" + threadCount);
                threadCount++;
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    @Override
    public int getServerPort() {
        try {
            int port = Integer.parseInt(applicationProperties.getProperty("jmemcached.server.port"));
            if (port < 0 || port > 65535) {
                throw new JMemcachedConfigException("jmemcached.server.port should be between 0 and 65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException("jmemcached.server.port should be a number", e);
        }
    }

    @Override
    public int getInitThreadCount() {
        return getThreadCount("jmemcached.server.init.thread.count");
    }

    @Override
    public int getMaxThreadCount() {
        return getThreadCount("jmemcached.server.max.thread.count");
    }

    @Override
    public int getClearDataIntervalInMs() {
        try {
            int interval = Integer.parseInt(applicationProperties.getProperty("jmemcached.storage.clear.data.interval.ms"));
            if (interval < 1000) {
                throw new JMemcachedConfigException("jmemcached.storage.clear.data.interval.ms should be >= 1000 ms");
            }
            return interval;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException("jmemcached.storage.clear.data.interval.ms should be a number", e);
        }
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket) {
        return new DefaultClientSocketHandler(clientSocket, this);
    }

    @Override
    public void close() throws Exception {
        storage.close();
    }

    protected int getThreadCount(String propertyName) {
        try {
            int threadCount = Integer.parseInt(applicationProperties.getProperty(propertyName));
            if (threadCount < 1) {
                throw new JMemcachedConfigException(propertyName + " should be >= 1");
            }
            return threadCount;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException(propertyName + " should be a number", e);
        }
    }

    @Override
    public String toString() {
        return String.format("DefaultServerConfig: port=%s, initThreadCount=%s, maxThreadCount=%s, clearDataIntervalInMs=%s ms",
                getServerPort(), getInitThreadCount(), getMaxThreadCount(), getClearDataIntervalInMs());
    }
}
