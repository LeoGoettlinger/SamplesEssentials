package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.api.storage.kind.KeyValueEvent;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public final class SettingEvent extends KeyValueEvent<SettingScope, byte[]> {
   @NotNull
   public SettingScope scope() {
      if (this.scope == null) {
         throw new IllegalStateException("SettingEvent.scope not set");
      } else {
         return this.scope;
      }
   }

   @NotNull
   public SettingEvent scope(@NotNull SettingScope v) {
      this.scope = v;
      return this;
   }

   @NotNull
   public String scopeKey() {
      if (this.scopeKey == null) {
         throw new IllegalStateException("SettingEvent.scopeKey not set");
      } else {
         return this.scopeKey;
      }
   }

   @NotNull
   public SettingEvent scopeKey(@NotNull String v) {
      this.scopeKey = v;
      return this;
   }

   @NotNull
   public String key() {
      if (this.key == null) {
         throw new IllegalStateException("SettingEvent.key not set");
      } else {
         return this.key;
      }
   }

   @NotNull
   public SettingEvent key(@NotNull String v) {
      this.key = v;
      return this;
   }

   @NotNull
   public byte[] value() {
      if (this.value == null) {
         throw new IllegalStateException("SettingEvent.value not set");
      } else {
         return this.value;
      }
   }

   @NotNull
   public SettingEvent value(@NotNull byte[] v) {
      this.value = v;
      return this;
   }

   public long updatedEpochMs() {
      return this.updatedEpochMs;
   }

   @NotNull
   public SettingEvent updatedEpochMs(long v) {
      this.updatedEpochMs = v;
      return this;
   }

   public void reset() {
      this.clear();
   }
}
