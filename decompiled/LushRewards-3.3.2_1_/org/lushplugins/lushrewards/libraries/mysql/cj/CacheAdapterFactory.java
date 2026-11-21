package org.lushplugins.lushrewards.libraries.mysql.cj;

public interface CacheAdapterFactory<K, V> {
   CacheAdapter<K, V> getInstance(Object var1, String var2, int var3, int var4);
}
