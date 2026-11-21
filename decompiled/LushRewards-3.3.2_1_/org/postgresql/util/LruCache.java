package org.postgresql.util;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.jdbc.ResourceLock;

public class LruCache<Key, Value extends CanEstimateSize> implements Gettable<Key, Value> {
   @Nullable
   private final LruCache.EvictAction<Value> onEvict;
   @Nullable
   private final LruCache.CreateAction<Key, Value> createAction;
   private final int maxSizeEntries;
   private final long maxSizeBytes;
   private long currentSize;
   private final Map<Key, Value> cache;
   private final ResourceLock lock = new ResourceLock();

   private void evictValue(Value value) {
      try {
         if (this.onEvict != null) {
            this.onEvict.evict(value);
         }
      } catch (SQLException var3) {
      }
   }

   public LruCache(int maxSizeEntries, long maxSizeBytes, boolean accessOrder) {
      this(maxSizeEntries, maxSizeBytes, accessOrder, null, null);
   }

   public LruCache(
      int maxSizeEntries,
      long maxSizeBytes,
      boolean accessOrder,
      @Nullable LruCache.CreateAction<Key, Value> createAction,
      @Nullable LruCache.EvictAction<Value> onEvict
   ) {
      this.maxSizeEntries = maxSizeEntries;
      this.maxSizeBytes = maxSizeBytes;
      this.createAction = createAction;
      this.onEvict = onEvict;
      this.cache = new LruCache.LimitedMap(16, 0.75F, accessOrder);
   }

   @Nullable
   public Value get(Key key) {
      ResourceLock ignore = this.lock.obtain();

      CanEstimateSize var3;
      try {
         var3 = this.cache.get(key);
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }

      return (Value)var3;
   }

   public Value borrow(Key key) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      CanEstimateSize var7;
      label47: {
         try {
            Value value = this.cache.remove(key);
            if (value == null) {
               if (this.createAction == null) {
                  throw new UnsupportedOperationException("createAction == null, so can't create object");
               }

               var7 = this.createAction.create(key);
               break label47;
            }

            this.currentSize = this.currentSize - value.getSize();
            var7 = value;
         } catch (Throwable var6) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ignore != null) {
            ignore.close();
         }

         return (Value)var7;
      }

      if (ignore != null) {
         ignore.close();
      }

      return (Value)var7;
   }

   public void put(Key key, Value value) {
      ResourceLock ignore = this.lock.obtain();

      label61: {
         label62: {
            try {
               long valueSize = value.getSize();
               if (this.maxSizeBytes != 0L && this.maxSizeEntries != 0 && valueSize * 2L <= this.maxSizeBytes) {
                  this.currentSize += valueSize;
                  Value prev = this.cache.put(key, value);
                  if (prev == null) {
                     break label61;
                  }

                  this.currentSize = this.currentSize - prev.getSize();
                  if (prev != value) {
                     this.evictValue(prev);
                  }
                  break label62;
               }

               this.evictValue(value);
            } catch (Throwable var8) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (ignore != null) {
               ignore.close();
            }

            return;
         }

         if (ignore != null) {
            ignore.close();
         }

         return;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   public void putAll(Map<Key, Value> m) {
      ResourceLock ignore = this.lock.obtain();

      try {
         for (Entry<Key, Value> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
         }
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   public interface CreateAction<Key, Value> {
      Value create(Key var1) throws SQLException;
   }

   public interface EvictAction<Value> {
      void evict(Value var1) throws SQLException;
   }

   private class LimitedMap extends LinkedHashMap<Key, Value> {
      LimitedMap(int initialCapacity, float loadFactor, boolean accessOrder) {
         super(initialCapacity, loadFactor, accessOrder);
      }

      @Override
      protected boolean removeEldestEntry(Entry<Key, Value> eldest) {
         if (this.size() <= LruCache.this.maxSizeEntries && LruCache.this.currentSize <= LruCache.this.maxSizeBytes) {
            return false;
         } else {
            for (Iterator<Entry<Key, Value>> it = this.entrySet().iterator(); it.hasNext(); it.remove()) {
               if (this.size() <= LruCache.this.maxSizeEntries && LruCache.this.currentSize <= LruCache.this.maxSizeBytes) {
                  return false;
               }

               Entry<Key, Value> entry = it.next();
               LruCache.this.evictValue(entry.getValue());
               long valueSize = entry.getValue().getSize();
               if (valueSize > 0L) {
                  LruCache.this.currentSize -= valueSize;
               }
            }

            return false;
         }
      }
   }
}
