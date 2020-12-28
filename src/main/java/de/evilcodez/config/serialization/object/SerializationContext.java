package de.evilcodez.config.serialization.object;

import java.util.HashMap;
import java.util.Map;

public class SerializationContext {

    private final Map<Object, Object> storage;
    private final boolean allowReferences;

    public SerializationContext(boolean allowReferences) {
        this.storage = new HashMap<>();
        this.allowReferences = allowReferences;
    }

    public boolean has(Object key) {
        return storage.containsKey(key);
    }

    public <T> T get(Object key) {
        return (T) storage.get(key);
    }

    public void put(Object key, Object value) {
        this.storage.put(key, value);
    }

    public void remove(Object key) {
        this.storage.remove(key);
    }

    public Map<Object, Object> getStorage() {
        return storage;
    }

    public boolean isAllowReferences() {
        return allowReferences;
    }
}
