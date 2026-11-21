package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public class KeyValueEvent<S, V> {
   @Nullable
   public S scope;
   @Nullable
   public String scopeKey;
   @Nullable
   public String key;
   @Nullable
   public V value;
   public long updatedEpochMs;
   public boolean remove;

   public void clear() {
      this.scope = null;
      this.scopeKey = null;
      this.key = null;
      this.value = null;
      this.updatedEpochMs = 0L;
      this.remove = false;
   }
}
