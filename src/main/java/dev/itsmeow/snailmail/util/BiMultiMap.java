package dev.itsmeow.snailmail.util;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

public class BiMultiMap<K, V> {
    private final SetMultimap<K, V> keysToValues = HashMultimap.create();

    private final SetMultimap<V, K> valuesToKeys = HashMultimap.create();

    public Set<V> getValues(K key) {
        return keysToValues.get(key);
    }

    public Set<K> getKeys(V value) {
        return valuesToKeys.get(value);
    }

    public boolean put(K key, V value) {
        return keysToValues.put(key, value) && valuesToKeys.put(value, key);
    }

    public boolean putAll(K key, Iterable<? extends V> values) {
        boolean changed = false;
        for(V value : values) {
            changed |= put(key, value);
        }
        return changed;
    }

    public boolean removeKeyFromAll(K key) {
        Set<V> values = keysToValues.removeAll(key);
        values.forEach(value -> valuesToKeys.remove(value, key));
        return !values.isEmpty();
    }

    public boolean removeValueFromAll(V value) {
        Set<K> keys = valuesToKeys.removeAll(value);
        keys.forEach(key -> keysToValues.remove(key, value));
        return !keys.isEmpty();
    }

    public boolean remove(K key, V value) {
        return this.keysToValues.remove(key, value) && this.valuesToKeys.remove(value, key);
    }

    public ImmutableSetMultimap<K, V> getKeysToValues() {
        return ImmutableSetMultimap.copyOf(keysToValues);
    }

    public ImmutableSetMultimap<V, K> getValuesToKeys() {
        return ImmutableSetMultimap.copyOf(valuesToKeys);
    }

    public void clear() {
        keysToValues.clear();
        valuesToKeys.clear();
    }

    public void putIfAbsent(K key, V value) {
        if(!keysToValues.containsKey(key) || !valuesToKeys.containsEntry(value, key)) {
            this.put(key, value);
        }
    }

}
