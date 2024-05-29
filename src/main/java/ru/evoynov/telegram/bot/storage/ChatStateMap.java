package ru.evoynov.telegram.bot.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ChatStateMap extends ConcurrentHashMap<Long, ChatState> {

    private static final String FILE = "chat_state.json";

    private static boolean restoreActive = false;

    @Override
    public synchronized ChatState put(Long key, ChatState value) {
        super.put(key, value);
        if (!restoreActive) {
            store();
        }
        return value;
    }

    protected void store() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(FILE), this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized static ChatStateMap restore() {
        restoreActive = true;
        try {
            var file = new File(FILE);
            if (!file.exists()) {
                return new ChatStateMap();
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                var map = mapper.readValue(file, new TypeReference<ChatStateMap>() {});
                map.values().forEach(s -> s.setStorage(map));
                return map;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            restoreActive = false;
        }
    }
}
