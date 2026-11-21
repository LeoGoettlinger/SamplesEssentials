package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;

@ApiStatus.Experimental
public record SettingRecord(SettingScope scope, String scopeKey, String key, byte[] value, long updatedEpochMs) {
   public SettingRecord(SettingScope scope, String scopeKey, String key, byte[] value, long updatedEpochMs) {
      if (scope == null) {
         throw new IllegalArgumentException("scope");
      } else if (scopeKey == null) {
         throw new IllegalArgumentException("scopeKey");
      } else if (key == null) {
         throw new IllegalArgumentException("key");
      } else if (value == null) {
         throw new IllegalArgumentException("value");
      } else {
         this.scope = scope;
         this.scopeKey = scopeKey;
         this.key = key;
         this.value = value;
         this.updatedEpochMs = updatedEpochMs;
      }
   }

   @Nullable
   public String asString() {
      return this.value == null ? null : new String(this.value, StandardCharsets.UTF_8);
   }
}
