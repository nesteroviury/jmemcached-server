package ru.dev.jmemcached.server;

import ru.dev.jmemcached.common.protocol.model.Request;
import ru.dev.jmemcached.common.protocol.model.Response;

public interface CommandHandler {
    Response handle(Request request);
}
