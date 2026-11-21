package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public final class CounterEvent<K> {
   @Nullable
   public K key;
   public long delta;

   public void clear() {
      this.key = null;
      this.delta = 0L;
   }
}
