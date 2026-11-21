package net.coreprotect.thread;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.config.ConfigHandler;
import org.bukkit.Location;

public class CacheHandler implements Runnable {
   public static Map<String, Object[]> lookupCache = Collections.synchronizedMap(new HashMap<>());
   public static Map<String, Object[]> breakCache = Collections.synchronizedMap(new HashMap<>());
   public static Map<String, Object[]> interactCache = Collections.synchronizedMap(new HashMap<>());
   public static Map<String, Object[]> entityCache = Collections.synchronizedMap(new HashMap<>());
   public static ConcurrentHashMap<String, Object[]> pistonCache = new ConcurrentHashMap<>(16, 0.75F, 2);
   public static ConcurrentHashMap<String, Object[]> spreadCache = new ConcurrentHashMap<>(16, 0.75F, 2);
   public static ConcurrentHashMap<Location, Object[]> redstoneCache = new ConcurrentHashMap<>(16, 0.75F, 2);

   @Override
   public void run() {
      while (ConfigHandler.serverRunning) {
         try {
            for (int id = 0; id < 9; id++) {
               Thread.sleep(1000L);
               int scanTime = 30;
               Map cache = lookupCache;
               switch (id) {
                  case 1:
                     cache = breakCache;
                     break;
                  case 2:
                     cache = pistonCache;
                     scanTime = 900;
                     break;
                  case 3:
                     cache = spreadCache;
                     scanTime = 1800;
                     break;
                  case 4:
                     cache = interactCache;
                     scanTime = 5;
                     break;
                  case 5:
                     cache = redstoneCache;
                     scanTime = 1;
                     break;
                  case 6:
                     cache = entityCache;
                     scanTime = 3600;
                     break;
                  case 7:
                     cache = ConfigHandler.entityBlockMapper;
                     scanTime = 5;
                     break;
                  case 8:
                     this.cleanupDispenserCache();
                     continue;
               }

               int timestamp = (int)(System.currentTimeMillis() / 1000L) - scanTime;
               Iterator<Entry> iterator = cache.entrySet().iterator();

               while (iterator.hasNext()) {
                  try {
                     Entry entry = iterator.next();
                     Object[] data = (Object[])entry.getValue();
                     int time = data[0] instanceof Long ? (int)((Long)data[0] / 1000L) : (Integer)data[0];
                     if (time < timestamp) {
                        try {
                           iterator.remove();
                        } catch (Exception var10) {
                        }
                     }
                  } catch (Exception var11) {
                     break;
                  }
               }
            }
         } catch (Exception var12) {
            var12.printStackTrace();
         }
      }
   }

   private void cleanupDispenserCache() {
      try {
         long currentTime = System.currentTimeMillis();
         long expiryTime = 5000L;
         Iterator<Entry<String, ConcurrentHashMap<String, Long>>> locationIterator = ConfigHandler.dispenserNoChange.entrySet().iterator();

         while (locationIterator.hasNext()) {
            Entry<String, ConcurrentHashMap<String, Long>> locationEntry = locationIterator.next();
            ConcurrentHashMap<String, Long> eventMap = locationEntry.getValue();
            if (eventMap.isEmpty()) {
               locationIterator.remove();
            } else {
               Iterator<Entry<String, Long>> eventIterator = eventMap.entrySet().iterator();

               while (eventIterator.hasNext()) {
                  Entry<String, Long> eventEntry = eventIterator.next();
                  if (currentTime - eventEntry.getValue() > expiryTime) {
                     eventIterator.remove();
                  }
               }

               if (eventMap.isEmpty()) {
                  locationIterator.remove();
               }
            }
         }

         Iterator<Entry<String, Object[]>> pendingIterator = ConfigHandler.dispenserPending.entrySet().iterator();

         while (pendingIterator.hasNext()) {
            Entry<String, Object[]> entry = pendingIterator.next();
            Object[] data = entry.getValue();
            if (data != null && data.length > 1) {
               long timestamp = (Long)data[1];
               if (currentTime - timestamp > expiryTime) {
                  pendingIterator.remove();
               }
            }
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }
   }
}
