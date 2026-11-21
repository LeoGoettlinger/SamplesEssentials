package com.fastasyncworldedit.core.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Metadatable {
   void setMeta(String key, Object value);

   <T> T getAndSetMeta(String key, T value);

   boolean hasMeta();

   <V> V getMeta(String key);

   @Nonnull
   default <V> V getMeta(String key, @Nonnull V defaultValue) {
      V value = this.getMeta(key);
      return value == null ? defaultValue : value;
   }

   @Nullable
   <V> V deleteMeta(String key);

   public static final class METADATA_KEYS {
      public static final String ANVIL_CLIPBOARD = "anvil-clipboard";
      public static final String ROLLBACK = "rollback";
   }
}
