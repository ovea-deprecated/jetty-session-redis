package com.ovea.jetty.session.serializer.jboss.serial.util;

import java.util.*;

/**
 * This is a WeakHashMap divided into partitions, using a simple algorithm of using the hashCode of a Key % numberOfPartitions to determine what partition to use.
 * This is intended to minimize synchroniozation affects
 *
 * @author Clebert Suconic
 */
public class PartitionedWeakHashMap extends AbstractMap {

    boolean issynchronized;
    Map[] partitionMaps;

    public PartitionedWeakHashMap() {
        this(false);
    }

    public PartitionedWeakHashMap(boolean issynchronized) {
        super();
        this.issynchronized = issynchronized;
        partitionMaps = new Map[PARTITION_SIZE];
        for (int i = 0; i < PARTITION_SIZE; i++) {
            if (issynchronized) {
                partitionMaps[i] = Collections.synchronizedMap(new WeakHashMap());
            } else {
                partitionMaps[i] = new WeakHashMap();
            }
        }
    }


    public Map getMap(Object obj) {
        int hash = obj.hashCode();
        if (hash < 0) hash *= -1;
        hash = hash % PARTITION_SIZE;
        return partitionMaps[hash];
    }


    private static final int PARTITION_SIZE = 10;


    public Set entrySet() {
        throw new RuntimeException("method not supported");
    }

    public void clear() {
        for (int i = 0; i < partitionMaps.length; i++) {
            partitionMaps[i].clear();
        }
    }

    protected Object clone() throws CloneNotSupportedException {
        throw new RuntimeException("Clone not supported");
    }

    public boolean containsKey(Object key) {
        return getMap(key).containsKey(key);
    }

    public boolean containsValue(Object value) {
        throw new RuntimeException("method not supported");
    }

    public boolean equals(Object o) {
        throw new RuntimeException("method not supported");
    }

    public Object get(Object key) {
        return getMap(key).get(key);
    }

    public boolean isEmpty() {
        throw new RuntimeException("method not supported");
    }

    public Set keySet() {
        HashSet hashSet = new HashSet();

        for (int i = 0; i < PARTITION_SIZE; i++) {
            hashSet.addAll(partitionMaps[i].keySet());
        }

        return hashSet;
    }

    public Object put(Object key, Object value) {

        // if the maps are not synchronized, the put at leas needs to be
        if (!issynchronized) {
            Map map = getMap(key);
            synchronized (map) {
                return map.put(key, value);
            }

        } else {
            return getMap(key).put(key, value);
        }
    }

    public void putAll(Map elementsToAdd) {
        Iterator iter = elementsToAdd.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    public Object remove(Object key) {
        return getMap(key).remove(key);
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < PARTITION_SIZE; i++) {
            size += partitionMaps[i].size();
        }
        return size;
    }

    public Collection values() {
        ArrayList values = new ArrayList();
        for (int i = 0; i < PARTITION_SIZE; i++) {
            values.addAll(partitionMaps[i].values());
        }
        return values;

    }


}
