package ru.dev.jmemcached.server.impl;

import ru.dev.jmemcached.common.exception.JMemcachedException;
import ru.dev.jmemcached.common.protocol.model.Request;
import ru.dev.jmemcached.common.protocol.model.Response;
import ru.dev.jmemcached.common.protocol.model.Status;
import ru.dev.jmemcached.server.CommandHandler;
import ru.dev.jmemcached.server.ServerConfig;
import ru.dev.jmemcached.server.Storage;

import static ru.dev.jmemcached.common.protocol.model.Command.*;

class DefaultCommandHandler implements CommandHandler {
    private final Storage storage;

    DefaultCommandHandler(ServerConfig serverConfig) {
        this.storage = serverConfig.getStorage();
    }

    @Override
    public Response handle(Request request) {
        Status status;
        byte[] data = null;
        if (CLEAR.equals(request.getCommand())) {
            status = storage.clear();
        } else if (PUT.equals(request.getCommand())) {
            status = storage.put(request.getKey(), request.getTtl(), request.getData());
        } else if (REMOVE.equals(request.getCommand())) {
            status = storage.remove(request.getKey());
        } else if (GET.equals(request.getCommand())) {
            data = storage.get(request.getKey());
            status = data == null ? Status.NOT_FOUND : Status.GOTTEN;
        } else {
            throw new JMemcachedException("Unsupported command: " + request.getCommand());
        }
        return new Response(status, data);
    }
}
