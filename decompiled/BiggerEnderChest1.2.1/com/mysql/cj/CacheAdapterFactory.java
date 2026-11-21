package com.mysql.cj;

import java.util.concurrent.locks.Lock;

public interface CacheAdapterFactory<K, V> {
   CacheAdapter<K, V> getInstance(Lock var1, String var2, int var3, int var4);
}
