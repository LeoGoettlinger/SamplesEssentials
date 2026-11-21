package com.sk89q.jnbt;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Map.Entry;
import org.enginehub.linbus.tree.LinCompoundTag;

@Deprecated
public class CompoundTagBuilder {
   private final LinCompoundTag.Builder builder;

   CompoundTagBuilder() {
      this.builder = LinCompoundTag.builder();
   }

   CompoundTagBuilder(LinCompoundTag source) {
      Preconditions.checkNotNull(source);
      this.builder = source.toBuilder();
   }

   public CompoundTagBuilder put(String key, Tag<?, ?> value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      this.builder.put(key, value.toLinTag());
      return this;
   }

   public CompoundTagBuilder putByteArray(String key, byte[] value) {
      this.builder.putByteArray(key, value);
      return this;
   }

   public CompoundTagBuilder putByte(String key, byte value) {
      this.builder.putByte(key, value);
      return this;
   }

   public CompoundTagBuilder putDouble(String key, double value) {
      this.builder.putDouble(key, value);
      return this;
   }

   public CompoundTagBuilder putFloat(String key, float value) {
      this.builder.putFloat(key, value);
      return this;
   }

   public CompoundTagBuilder putIntArray(String key, int[] value) {
      this.builder.putIntArray(key, value);
      return this;
   }

   public CompoundTagBuilder putInt(String key, int value) {
      this.builder.putInt(key, value);
      return this;
   }

   public CompoundTagBuilder putLongArray(String key, long[] value) {
      this.builder.putLongArray(key, value);
      return this;
   }

   public CompoundTagBuilder putLong(String key, long value) {
      this.builder.putLong(key, value);
      return this;
   }

   public CompoundTagBuilder putShort(String key, short value) {
      this.builder.putShort(key, value);
      return this;
   }

   public CompoundTagBuilder putString(String key, String value) {
      this.builder.putString(key, value);
      return this;
   }

   public CompoundTagBuilder remove(String key) {
      Preconditions.checkNotNull(key);
      this.builder.remove(key);
      return this;
   }

   public CompoundTagBuilder putAll(Map<String, ? extends Tag<?, ?>> value) {
      Preconditions.checkNotNull(value);

      for (Entry<String, ? extends Tag<?, ?>> entry : value.entrySet()) {
         this.put(entry.getKey(), (Tag<?, ?>)entry.getValue());
      }

      return this;
   }

   public CompoundTag build() {
      return new CompoundTag(this.builder.build());
   }

   public static CompoundTagBuilder create() {
      return new CompoundTagBuilder();
   }
}
