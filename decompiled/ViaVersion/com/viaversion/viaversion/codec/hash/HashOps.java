package com.viaversion.viaversion.codec.hash;

import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.codec.ThrowingOps;
import com.viaversion.viaversion.api.minecraft.codec.hash.Hasher;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.codec.OpsBase;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class HashOps extends OpsBase implements Hasher {
   private static final byte TAG_MAP_START = 2;
   private static final byte TAG_MAP_END = 3;
   private static final byte TAG_LIST_START = 4;
   private static final byte TAG_LIST_END = 5;
   private static final byte TAG_BYTE = 6;
   private static final byte TAG_SHORT = 7;
   private static final byte TAG_INT = 8;
   private static final byte TAG_LONG = 9;
   private static final byte TAG_FLOAT = 10;
   private static final byte TAG_DOUBLE = 11;
   private static final byte TAG_STRING = 12;
   private static final byte TAG_BOOLEAN = 13;
   private static final byte TAG_BYTE_ARRAY_START = 14;
   private static final byte TAG_BYTE_ARRAY_END = 15;
   private static final byte TAG_INT_ARRAY_START = 16;
   private static final byte TAG_INT_ARRAY_END = 17;
   private static final byte TAG_LONG_ARRAY_START = 18;
   private static final byte TAG_LONG_ARRAY_END = 19;
   static final byte[] FALSE = new byte[]{13, 0};
   static final byte[] TRUE = new byte[]{13, 1};
   static final byte[] EMPTY_LIST = new byte[]{4, 5};
   static final byte[] EMPTY_MAP = new byte[]{2, 3};
   private final HashBuilder hashBuilder;

   public HashOps(CodecContext context, HashFunction hashFunction) {
      super(context);
      this.hashBuilder = new HashBuilder(hashFunction);
   }

   @Override
   public void writeByte(byte b) {
      this.hashBuilder.preSize(2).writeByte((byte)6).writeByte(b);
   }

   @Override
   public void writeBytes(byte[] array) {
      this.hashBuilder.preSize(array.length + 2).writeByte((byte)14).writeBytes(array).writeByte((byte)15);
   }

   @Override
   public void writeBoolean(boolean b) {
      this.hashBuilder.writeBytesDirect(b ? TRUE : FALSE);
   }

   @Override
   public void writeShort(short s) {
      this.hashBuilder.preSize(3).writeByte((byte)7).writeShort(s);
   }

   @Override
   public void writeString(CharSequence sequence) {
      this.hashBuilder.preSize(sequence.length() * 2 + 1 + 4).writeByte((byte)12).writeInt(sequence.length()).writeString(sequence);
   }

   @Override
   public void writeInt(int i) {
      this.hashBuilder.preSize(5).writeByte((byte)8).writeInt(i);
   }

   @Override
   public void writeLong(long l) {
      this.hashBuilder.preSize(9).writeByte((byte)9).writeLong(l);
   }

   @Override
   public void writeFloat(float f) {
      this.hashBuilder.preSize(5).writeByte((byte)10).writeFloat(f);
   }

   @Override
   public void writeDouble(double d) {
      this.hashBuilder.preSize(9).writeByte((byte)11).writeDouble(d);
   }

   @Override
   public void writeInts(int[] array) {
      this.hashBuilder.preSize(array.length * 4 + 2);
      this.hashBuilder.writeByte((byte)16);

      for (int i : array) {
         this.hashBuilder.writeInt(i);
      }

      this.hashBuilder.writeByte((byte)17);
   }

   @Override
   public void writeLongs(long[] array) {
      this.hashBuilder.preSize(array.length * 8 + 2);
      this.hashBuilder.writeByte((byte)18);

      for (long l : array) {
         this.hashBuilder.writeLong(l);
      }

      this.hashBuilder.writeByte((byte)19);
   }

   @Override
   public void writeList(Consumer<Ops.ListSerializer> consumer) {
      HashOps.ListHashBuilder listHasher = new HashOps.ListHashBuilder();
      consumer.accept(listHasher);
      listHasher.applyHashToParent();
   }

   @Override
   public void writeMap(Consumer<Ops.MapSerializer> consumer) {
      HashOps.MapHashBuilder mapHasher = new HashOps.MapHashBuilder();
      consumer.accept(mapHasher);
      mapHasher.applyHashToParent();
   }

   @Override
   public int hash() {
      return this.hashBuilder.hash();
   }

   @Override
   public void reset() {
      this.hashBuilder.reset();
   }

   private abstract class CollectionHashBuilder {
      protected final HashOps hasher = new HashOps(HashOps.this.context(), HashOps.this.hashBuilder.function());

      protected int listHash(Consumer<Ops.ListSerializer> consumer) {
         this.hasher.reset();
         HashOps.ListHashBuilder listHasher = this.hasher.new ListHashBuilder();
         consumer.accept(listHasher);
         listHasher.applyHashToParent();
         return this.hasher.hash();
      }

      protected int mapHash(Consumer<Ops.MapSerializer> consumer) {
         this.hasher.reset();
         HashOps.MapHashBuilder mapHasher = this.hasher.new MapHashBuilder();
         consumer.accept(mapHasher);
         mapHasher.applyHashToParent();
         return this.hasher.hash();
      }

      protected <V> int hash(Type<V> type, V value) {
         this.hasher.reset();
         type.write(this.hasher, value);
         return this.hasher.hash();
      }
   }

   private final class ListHashBuilder extends HashOps.CollectionHashBuilder implements Ops.ListSerializer {
      private final IntList entries = new IntArrayList();

      @Override
      public <T> Ops.ListSerializer write(Type<T> valueType, T value) {
         this.entries.add(this.hash(valueType, value));
         return this;
      }

      @Override
      public Ops.ListSerializer writeList(Consumer<Ops.ListSerializer> consumer) {
         this.entries.add(this.listHash(consumer));
         return this;
      }

      @Override
      public Ops.ListSerializer writeMap(Consumer<Ops.MapSerializer> consumer) {
         this.entries.add(this.mapHash(consumer));
         return this;
      }

      public void applyHashToParent() {
         if (this.entries.isEmpty()) {
            HashOps.this.hashBuilder.writeBytesDirect(HashOps.EMPTY_LIST);
         } else {
            int size = this.entries.size();
            HashOps.this.hashBuilder.preSize(size * 4 + 2);
            HashOps.this.hashBuilder.writeByte((byte)4);

            for (int i = 0; i < size; i++) {
               HashOps.this.hashBuilder.writeInt(this.entries.getInt(i));
            }

            HashOps.this.hashBuilder.writeByte((byte)5);
         }
      }
   }

   private final class MapHashBuilder extends HashOps.CollectionHashBuilder implements Ops.MapSerializer {
      private final List<HashOps.MapHashBuilder.Entry> entries = new ArrayList<>();

      @Override
      public <K, T> Ops.MapSerializer write(Type<K> keyType, K key, Type<T> valueType, T value) {
         this.entries.add(new HashOps.MapHashBuilder.Entry(this.hash(keyType, key), this.hash(valueType, value)));
         return this;
      }

      @Override
      public Ops.MapSerializer writeList(String key, Consumer<Ops.ListSerializer> consumer) {
         this.entries.add(new HashOps.MapHashBuilder.Entry(this.hash(Types.STRING, key), this.listHash(consumer)));
         return this;
      }

      @Override
      public Ops.MapSerializer writeMap(String key, Consumer<Ops.MapSerializer> consumer) {
         this.entries.add(new HashOps.MapHashBuilder.Entry(this.hash(Types.STRING, key), this.mapHash(consumer)));
         return this;
      }

      @Override
      public <T> Ops.MapSerializer writeInlinedMap(Type<T> valueType, T value) {
         valueType.write(new ThrowingOps() {
            @Override
            public void writeMap(Consumer<Ops.MapSerializer> consumer) {
               consumer.accept(MapHashBuilder.this);
            }
         }, value);
         return this;
      }

      public void applyHashToParent() {
         if (this.entries.isEmpty()) {
            HashOps.this.hashBuilder.writeBytesDirect(HashOps.EMPTY_MAP);
         } else {
            int size = this.entries.size();
            HashOps.this.hashBuilder.preSize(size * 4 * 2 + 2);
            HashOps.this.hashBuilder.writeByte((byte)2);
            this.entries.sort(Comparator.naturalOrder());

            for (int i = 0; i < size; i++) {
               HashOps.MapHashBuilder.Entry entry = this.entries.get(i);
               HashOps.this.hashBuilder.writeInt(entry.key);
               HashOps.this.hashBuilder.writeInt(entry.value);
            }

            HashOps.this.hashBuilder.writeByte((byte)3);
         }
      }

      private record Entry(int key, int value) implements Comparable<HashOps.MapHashBuilder.Entry> {
         public int compareTo(HashOps.MapHashBuilder.Entry o) {
            return this.key != o.key ? Long.compare(padToLong(this.key), padToLong(o.key)) : Long.compare(padToLong(this.value), padToLong(o.value));
         }

         private static long padToLong(int value) {
            return value & 4294967295L;
         }
      }
   }
}
