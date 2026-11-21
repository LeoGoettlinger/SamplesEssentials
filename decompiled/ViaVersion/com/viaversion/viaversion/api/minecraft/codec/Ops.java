package com.viaversion.viaversion.api.minecraft.codec;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Ops {
   CodecContext context();

   void writeByte(byte var1);

   void writeBytes(byte[] var1);

   void writeBoolean(boolean var1);

   void writeShort(short var1);

   void writeString(CharSequence var1);

   void writeInt(int var1);

   void writeLong(long var1);

   void writeFloat(float var1);

   void writeDouble(double var1);

   void writeInts(int[] var1);

   void writeLongs(long[] var1);

   void writeList(Consumer<Ops.ListSerializer> var1);

   void writeMap(Consumer<Ops.MapSerializer> var1);

   default <V> void write(Type<V> type, V value) {
      type.write(this, value);
   }

   public interface ListSerializer {
      <V> Ops.ListSerializer write(Type<V> var1, V var2);

      Ops.ListSerializer writeList(Consumer<Ops.ListSerializer> var1);

      Ops.ListSerializer writeMap(Consumer<Ops.MapSerializer> var1);
   }

   public interface MapSerializer {
      <K, V> Ops.MapSerializer write(Type<K> var1, K var2, Type<V> var3, V var4);

      default <V> Ops.MapSerializer write(String key, Type<V> type, V value) {
         this.write(Types.STRING, key, type, value);
         return this;
      }

      default <V> Ops.MapSerializer write(String key, Type<V> type, @Nullable V value, V def) {
         this.write(Types.STRING, key, type, value != null ? value : def);
         return this;
      }

      default <V> Ops.MapSerializer writeOptional(String key, Type<V> type, @Nullable V value) {
         if (value != null) {
            this.write(Types.STRING, key, type, value);
         }

         return this;
      }

      default <V> Ops.MapSerializer writeOptional(String key, Type<V> type, @Nullable V value, V def) {
         if (value != null && !Objects.deepEquals(value, def)) {
            this.write(Types.STRING, key, type, value);
         }

         return this;
      }

      Ops.MapSerializer writeList(String var1, Consumer<Ops.ListSerializer> var2);

      Ops.MapSerializer writeMap(String var1, Consumer<Ops.MapSerializer> var2);

      <T> Ops.MapSerializer writeInlinedMap(Type<T> var1, T var2);
   }
}
