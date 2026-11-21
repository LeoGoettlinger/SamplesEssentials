package com.viaversion.viaversion.codec.nbt;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.codec.ThrowingOps;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.codec.OpsBase;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NbtOps extends OpsBase {
   private Tag result;

   public NbtOps(CodecContext context) {
      super(context);
   }

   public static <V> Tag serialize(CodecContext context, Type<V> type, V value) {
      NbtOps ops = new NbtOps(context);
      type.write(ops, value);
      return ops.complete();
   }

   public Tag complete() {
      Tag tag = this.result;
      Preconditions.checkNotNull(tag, "No value written");
      this.result = null;
      return tag;
   }

   @Override
   public void writeByte(byte b) {
      this.result = new ByteTag(b);
   }

   @Override
   public void writeBytes(byte[] array) {
      this.result = new ByteArrayTag(array);
   }

   @Override
   public void writeBoolean(boolean b) {
      this.result = new ByteTag(b);
   }

   @Override
   public void writeShort(short s) {
      this.result = new ShortTag(s);
   }

   @Override
   public void writeString(CharSequence sequence) {
      this.result = new StringTag(sequence.toString());
   }

   @Override
   public void writeInt(int i) {
      this.result = new IntTag(i);
   }

   @Override
   public void writeLong(long l) {
      this.result = new LongTag(l);
   }

   @Override
   public void writeFloat(float f) {
      this.result = new FloatTag(f);
   }

   @Override
   public void writeDouble(double d) {
      this.result = new DoubleTag(d);
   }

   @Override
   public void writeInts(int[] array) {
      this.result = new IntArrayTag(array);
   }

   @Override
   public void writeLongs(long[] array) {
      this.result = new LongArrayTag(array);
   }

   @Override
   public void writeList(Consumer<Ops.ListSerializer> consumer) {
      NbtOps.ListNbtSerializer serializer = new NbtOps.ListNbtSerializer();
      consumer.accept(serializer);
      this.result = ListTag.of(serializer.entries);
   }

   @Override
   public void writeMap(Consumer<Ops.MapSerializer> consumer) {
      NbtOps.MapNbtSerializer serializer = new NbtOps.MapNbtSerializer();
      consumer.accept(serializer);
      this.result = serializer.tag;
   }

   private <V> Tag serialize(Type<V> type, V value) {
      type.write(this, value);
      return this.complete();
   }

   private Tag serializeList(Consumer<Ops.ListSerializer> consumer) {
      this.writeList(consumer);
      return this.complete();
   }

   private Tag serializeMap(Consumer<Ops.MapSerializer> consumer) {
      this.writeMap(consumer);
      return this.complete();
   }

   private final class ListNbtSerializer implements Ops.ListSerializer {
      private final List<Tag> entries = new ArrayList<>();

      @Override
      public <V> Ops.ListSerializer write(Type<V> valueType, V value) {
         this.entries.add(NbtOps.this.serialize(valueType, value));
         return this;
      }

      @Override
      public Ops.ListSerializer writeList(Consumer<Ops.ListSerializer> consumer) {
         this.entries.add(NbtOps.this.serializeList(consumer));
         return this;
      }

      @Override
      public Ops.ListSerializer writeMap(Consumer<Ops.MapSerializer> consumer) {
         this.entries.add(NbtOps.this.serializeMap(consumer));
         return this;
      }
   }

   private final class MapNbtSerializer implements Ops.MapSerializer {
      private final CompoundTag tag = new CompoundTag();

      @Override
      public <K, V> Ops.MapSerializer write(Type<K> keyType, K key, Type<V> valueType, V value) {
         this.tag.put(this.serializeKey(keyType, key), NbtOps.this.serialize(valueType, value));
         return this;
      }

      @Override
      public Ops.MapSerializer writeList(String key, Consumer<Ops.ListSerializer> consumer) {
         this.tag.put(key, NbtOps.this.serializeList(consumer));
         return this;
      }

      @Override
      public Ops.MapSerializer writeMap(String key, Consumer<Ops.MapSerializer> consumer) {
         this.tag.put(key, NbtOps.this.serializeMap(consumer));
         return this;
      }

      @Override
      public <T> Ops.MapSerializer writeInlinedMap(Type<T> valueType, T value) {
         valueType.write(new ThrowingOps() {
            @Override
            public CodecContext context() {
               return NbtOps.this.context();
            }

            @Override
            public void writeMap(Consumer<Ops.MapSerializer> consumer) {
               consumer.accept(MapNbtSerializer.this);
            }
         }, value);
         return this;
      }

      private <K> String serializeKey(Type<K> keyType, K key) {
         Tag keyTag = NbtOps.this.serialize(keyType, key);
         Preconditions.checkArgument(keyTag instanceof StringTag, "Map keys have to be strings, got %s from %s", new Object[]{keyTag, keyType.getTypeName()});
         return ((StringTag)keyTag).getValue();
      }
   }
}
