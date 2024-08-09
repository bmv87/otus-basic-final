package ru.otus.services.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;

import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    private static volatile CacheManager instance;
    public ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();

    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    public <V extends Cachable> void addCacheStore(String storeName) {
        this.caches.put(storeName, new Cache());
        logger.debug("Cache store added: {}", storeName);
    }


    private Cache getCacheStore(String name) {
        var store = this.caches.get(name);

        if (store == null) {
            throw new ResponseException("Cache store not found: " + name);
        }
        return store;
    }

    public <V extends Cachable> void addToCache(String name, String key, V value) {
        var store = getCacheStore(name);
        store.add(key, value);
    }

    public <V extends Cachable> V getFromCache(String name, String key, Class<V> vClass) {
        var store = getCacheStore(name);
        return store.get(key, vClass);
    }

    public <V extends Cachable> V removeFromCache(String name, String key, Class<V> vClass) {
        var store = getCacheStore(name);
        return store.remove(key, vClass);
    }
}
