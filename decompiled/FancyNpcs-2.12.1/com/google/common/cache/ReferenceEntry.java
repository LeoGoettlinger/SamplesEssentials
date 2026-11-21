package com.google.common.cache;

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtIncompatible
interface ReferenceEntry<K, V> {
   @CheckForNull
   LocalCache.ValueReference<K, V> getValueReference();

   void setValueReference(LocalCache.ValueReference<K, V> valueReference);

   @CheckForNull
   ReferenceEntry<K, V> getNext();

   int getHash();

   @CheckForNull
   K getKey();

   long getAccessTime();

   void setAccessTime(long time);

   ReferenceEntry<K, V> getNextInAccessQueue();

   void setNextInAccessQueue(ReferenceEntry<K, V> next);

   ReferenceEntry<K, V> getPreviousInAccessQueue();

   void setPreviousInAccessQueue(ReferenceEntry<K, V> previous);

   long getWriteTime();

   void setWriteTime(long time);

   ReferenceEntry<K, V> getNextInWriteQueue();

   void setNextInWriteQueue(ReferenceEntry<K, V> next);

   ReferenceEntry<K, V> getPreviousInWriteQueue();

   void setPreviousInWriteQueue(ReferenceEntry<K, V> previous);
}
