package ru.dev.jmemcached.server;

import ru.dev.jmemcached.common.protocol.RequestConverter;
import ru.dev.jmemcached.common.protocol.ResponseConverter;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

public interface ServerConfig extends AutoCloseable{
    RequestConverter getRequestConverter();
    ResponseConverter getResponseConverter();
    ThreadFactory getWorkerThreadFactory();
    int getServerPort();
    int getInitThreadCount();
    int getMaxThreadCount();
    int getClearDataIntervalInMs();
    Storage getStorage();
    CommandHandler getCommandHandler();
    ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket);
}
