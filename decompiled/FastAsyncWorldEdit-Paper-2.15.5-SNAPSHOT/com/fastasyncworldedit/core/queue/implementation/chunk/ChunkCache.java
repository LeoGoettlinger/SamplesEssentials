package com.fastasyncworldedit.core.queue.implementation.chunk;

import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.Trimable;
import com.fastasyncworldedit.core.util.MathMan;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.ref.WeakReference;

public class ChunkCache<T extends Trimable> implements IChunkCache<T> {
   protected final Long2ObjectLinkedOpenHashMap<WeakReference<T>> getCache = new Long2ObjectLinkedOpenHashMap();
   private final IChunkCache<T> delegate;

   public ChunkCache(IChunkCache<T> delegate) {
      this.delegate = delegate;
   }

   public synchronized T get(int x, int z) {
      long pair = MathMan.pairInt(x, z);
      WeakReference<T> ref = (WeakReference<T>)this.getCache.get(pair);
      if (ref != null) {
         T blocks = ref.get();
         if (blocks != null) {
            return blocks;
         }
      }

      T blocks = this.newChunk(x, z);
      this.getCache.put(pair, new WeakReference(blocks));
      return blocks;
   }

   public T newChunk(int chunkX, int chunkZ) {
      return this.delegate.get(chunkX, chunkZ);
   }

   @Override
   public synchronized boolean trim(boolean aggressive) {
      if (this.getCache.isEmpty()) {
         return true;
      } else {
         boolean result = true;
         ObjectIterator<Entry<WeakReference<T>>> iter = this.getCache.long2ObjectEntrySet().fastIterator();

         while (iter.hasNext()) {
            Entry<WeakReference<T>> entry = (Entry<WeakReference<T>>)iter.next();
            WeakReference<T> value = (WeakReference<T>)entry.getValue();
            T igb = value.get();
            if (igb == null) {
               iter.remove();
            } else {
               result = false;
               if (!aggressive) {
                  return false;
               }

               igb.trim(true);
            }
         }

         return result;
      }
   }
}
