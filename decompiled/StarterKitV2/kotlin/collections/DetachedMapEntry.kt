package kotlin.collections

import java.util.Map.Entry
import kotlin.jvm.internal.markers.KMappedMarker

private class DetachedMapEntry<K, V>(key: Any, value: Any) : Entry<K, V>, KMappedMarker {
   public open val key: Any
   public open val value: Any

   init {
      this.key = (K)key;
      this.value = (V)value;
   }

   public override operator fun equals(other: Any?): Boolean {
      return AbstractMap.Companion.entryEquals$kotlin_stdlib(this, other);
   }

   public override fun hashCode(): Int {
      return AbstractMap.Companion.entryHashCode$kotlin_stdlib(this);
   }

   public override fun toString(): String {
      return AbstractMap.Companion.entryToString$kotlin_stdlib(this);
   }

   override fun setValue(newValue: V): V {
      throw new UnsupportedOperationException("Operation is not supported for read-only collection");
   }
}
