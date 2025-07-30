package com.example.mypixel.model;

import java.util.LinkedHashMap;

public class ParamsMap {
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> of(Object... keyVals) {
        if (keyVals.length % 2 != 0)
            throw new IllegalArgumentException("Must provide even number of arguments (key/value pairs)");
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keyVals.length; i += 2) {
            K key = (K) keyVals[i];
            V value = (V) keyVals[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
