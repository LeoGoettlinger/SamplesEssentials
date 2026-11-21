package com.artillexstudios.axrankmenu.libs.axapi.utils;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.Iterator;
import org.jspecify.annotations.NonNull;

public final class NonSynchronizedCooldown<T> implements Cooldown<T> {
   private final Object2LongMap<T> cooldowns = new Object2LongLinkedOpenHashMap();
   private long closestTime = Long.MAX_VALUE;

   @Override
   public void addCooldown(T key, long time) {
      this.doHouseKeeping();
      long newTime = System.currentTimeMillis() + time;
      this.closestTime = Math.min(this.closestTime, newTime);
      this.cooldowns.put(key, newTime);
   }

   @Override
   public long getRemaining(T key) {
      this.doHouseKeeping();
      return this.cooldowns.getOrDefault(key, System.currentTimeMillis()) - System.currentTimeMillis();
   }

   @Override
   public int size() {
      this.doHouseKeeping();
      return this.cooldowns.size();
   }

   @Override
   public void remove(T key) {
      this.doHouseKeeping();
      this.cooldowns.removeLong(key);
   }

   @Override
   public void clear() {
      this.cooldowns.clear();
   }

   @NonNull
   @Override
   public Iterator<T> iterator() {
      this.doHouseKeeping();
      return this.cooldowns.keySet().iterator();
   }

   private boolean doHouseKeeping() {
      long time = System.currentTimeMillis();
      if (time < this.closestTime) {
         return false;
      } else {
         boolean didHouseKeeping = false;
         ObjectIterator<Entry<T>> iterator = this.cooldowns.object2LongEntrySet().iterator();
         long closestTime = Long.MAX_VALUE;

         while (iterator.hasNext()) {
            Entry<T> entry = (Entry<T>)iterator.next();
            long value = entry.getLongValue();
            if (time >= value) {
               iterator.remove();
               didHouseKeeping = true;
            } else if (value < closestTime) {
               closestTime = value;
            }
         }

         this.closestTime = closestTime;
         return didHouseKeeping;
      }
   }
}
