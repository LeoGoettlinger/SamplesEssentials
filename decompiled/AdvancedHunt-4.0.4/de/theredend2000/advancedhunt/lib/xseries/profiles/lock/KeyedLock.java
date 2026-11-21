package de.theredend2000.advancedhunt.lib.xseries.profiles.lock;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Internal
public interface KeyedLock<K, V> extends AutoCloseable {
   V getOrRetryValue();

   @OverrideOnly
   void lock();

   void unlock();

   @Override
   void close();
}
