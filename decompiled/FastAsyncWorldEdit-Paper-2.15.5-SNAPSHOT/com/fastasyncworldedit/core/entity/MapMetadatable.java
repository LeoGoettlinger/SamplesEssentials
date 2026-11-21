package com.fastasyncworldedit.core.entity;

import java.util.Map;
import javax.annotation.Nonnull;

public interface MapMetadatable extends Metadatable {
   Map<String, Object> getRawMeta();

   @Override
   default void setMeta(String key, Object value) {
      this.getRawMeta().put(key, value);
   }

   @Override
   default <T> T getAndSetMeta(String key, T value) {
      return (T)this.getRawMeta().put(key, value);
   }

   @Override
   default boolean hasMeta() {
      return !this.getRawMeta().isEmpty();
   }

   default Object putIfAbsent(String key, Object value) {
      return this.getRawMeta().putIfAbsent(key, value);
   }

   @Override
   default <V> V getMeta(String key) {
      return (V)this.getRawMeta().get(key);
   }

   @Nonnull
   @Override
   default <V> V getMeta(String key, @Nonnull V def) {
      V value = (V)this.getRawMeta().get(key);
      return value == null ? def : value;
   }

   @Override
   default <V> V deleteMeta(String key) {
      return (V)this.getRawMeta().remove(key);
   }
}
