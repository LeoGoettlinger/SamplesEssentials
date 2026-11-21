package org.sethomegui.SetHomeGUI.libs.mysql.cj.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CacheAdapter;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CacheAdapterFactory;

public class PerVmServerConfigCacheFactory implements CacheAdapterFactory<String, Map<String, String>> {
   static final ConcurrentHashMap<String, Map<String, String>> serverConfigByUrl = new ConcurrentHashMap<>();
   private static final CacheAdapter<String, Map<String, String>> serverConfigCache = new CacheAdapter<String, Map<String, String>>() {
      public Map<String, String> get(String key) {
         return PerVmServerConfigCacheFactory.serverConfigByUrl.get(key);
      }

      public void put(String key, Map<String, String> value) {
         PerVmServerConfigCacheFactory.serverConfigByUrl.putIfAbsent(key, value);
      }

      public void invalidate(String key) {
         PerVmServerConfigCacheFactory.serverConfigByUrl.remove(key);
      }

      @Override
      public void invalidateAll(Set<String> keys) {
         for (String key : keys) {
            PerVmServerConfigCacheFactory.serverConfigByUrl.remove(key);
         }
      }

      @Override
      public void invalidateAll() {
         PerVmServerConfigCacheFactory.serverConfigByUrl.clear();
      }
   };

   @Override
   public CacheAdapter<String, Map<String, String>> getInstance(Lock lock, String url, int cacheMaxSize, int maxKeySize) {
      return serverConfigCache;
   }
}
