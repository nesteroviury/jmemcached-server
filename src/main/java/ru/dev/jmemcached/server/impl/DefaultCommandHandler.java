package ru.dev.jmemcached.server.impl;

import ru.dev.jmemcached.common.exception.JMemcachedException;
import ru.dev.jmemcached.common.protocol.model.Request;
import ru.dev.jmemcached.common.protocol.model.Response;
import ru.dev.jmemcached.common.protocol.model.Status;
import ru.dev.jmemcached.server.CommandHandler;
import ru.dev.jmemcached.server.Storage;

class DefaultCommandHandler implements CommandHandler {
    private final Storage storage;

    DefaultCommandHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Response handle(Request request) {
        Status status;
        byte[] data = null;
        switch (request.getCommand()) {
            case CLEAR:
                status = storage.clear();
                break;
            case PUT:
                status = storage.put(request.getKey(), request.getTtl(), request.getData());
                break;
            case REMOVE:
                status = storage.remove(request.getKey());
                break;
            case GET:
                data = storage.get(request.getKey());
                status = data == null ? Status.NOT_FOUND : Status.GOTTEN;
                break;
            default:
                throw new JMemcachedException("Unsupported command: " + request.getCommand());
        }
        return new Response(status, data);
    }
}
