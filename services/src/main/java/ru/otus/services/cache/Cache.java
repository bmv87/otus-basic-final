package ru.otus.services.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Cache {
    private final ConcurrentMap<String, ValueWrapper<? extends Cachable>> cacheMap;

    public Cache() {
        cacheMap = new ConcurrentHashMap<>(16);
    }

    public <V extends Cachable> V get(String key, Class<V> vClass) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        return vClass.cast(cacheMap.get(key).value());
    }

    public <V extends Cachable> void add(String key, V value) {
        cacheMap.remove(key);
        cacheMap.put(key, new ValueWrapper<V>(value));
    }

    public <V extends Cachable> V remove(String key, Class<V> vClass) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        var value = cacheMap.get(key).value();
        cacheMap.remove(key);
        return vClass.cast(value);
    }

    public record ValueWrapper<V extends Cachable>(V value) {
    }
}
