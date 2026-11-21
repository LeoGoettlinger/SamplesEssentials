package com.viaversion.viaversion.api.minecraft.chunks;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.util.CompactArrayUtil;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

public final class DataPaletteImpl implements DataPalette {
   private static final int DEFAULT_INITIAL_SIZE = 16;
   private final IntArrayList palette;
   private final int sizeBits;
   private Int2IntMap inversePalette;
   private DataPaletteImpl.ChunkData values;
   private static final ThreadLocal<boolean[]> MATCHING_INDEXES = ThreadLocal.withInitial(() -> new boolean[4096]);

   public DataPaletteImpl(int valuesLength) {
      this(valuesLength, 16);
   }

   public DataPaletteImpl(int valuesLength, int initialSize) {
      this.values = new DataPaletteImpl.SingleChunkData(valuesLength);
      this.sizeBits = Integer.numberOfTrailingZeros(valuesLength) / 3;
      this.palette = new IntArrayList(initialSize);
   }

   private Int2IntMap inversePalette() {
      if (this.inversePalette == null) {
         this.inversePalette = new Int2IntOpenHashMap(this.palette.size());
         this.inversePalette.defaultReturnValue(-1);

         for (int i = 0; i < this.palette.size(); i++) {
            this.inversePalette.put(this.palette.getInt(i), i);
         }
      }

      return this.inversePalette;
   }

   private Int2IntMap emptyInversePalette() {
      if (this.inversePalette == null) {
         this.inversePalette = new Int2IntOpenHashMap(this.palette.size());
         this.inversePalette.defaultReturnValue(-1);
      } else {
         this.inversePalette.clear();
      }

      return this.inversePalette;
   }

   @Override
   public int index(int x, int y, int z) {
      return (y << this.sizeBits | z) << this.sizeBits | x;
   }

   @Override
   public int idAt(int sectionCoordinate) {
      int index = this.values.get(sectionCoordinate);
      return this.palette.getInt(index);
   }

   @Override
   public void setIdAt(int sectionCoordinate, int id) {
      int index = this.inversePalette().get(id);
      if (index == -1) {
         index = this.palette.size();
         this.palette.add(id);
         this.inversePalette.put(id, index);
      }

      this.values.set(sectionCoordinate, index);
   }

   @Override
   public int paletteIndexAt(int packedCoordinate) {
      return this.values.get(packedCoordinate);
   }

   @Override
   public void setPaletteIndexAt(int sectionCoordinate, int index) {
      this.values.set(sectionCoordinate, index);
   }

   public void setPaletteIndexes(long[] data, int bitsPerValue, int valuesPerLong) {
      Preconditions.checkArgument(bitsPerValue <= 8, "Cannot pack global palette");
      this.values = new DataPaletteImpl.PackedChunkData(data, bitsPerValue, valuesPerLong, this.values.size());
   }

   @Override
   public long[] createPackedValues(int bitsPerValue, int entries, boolean rawIds) {
      if (rawIds) {
         return CompactArrayUtil.createCompactArrayWithPadding(bitsPerValue, entries, this::idAt);
      } else if (this.values instanceof DataPaletteImpl.PackedChunkData packed && packed.bitsPerValue == bitsPerValue) {
         return packed.data;
      } else {
         int valuesPerLong = (char)(64 / bitsPerValue);
         long[] data = new long[(entries + valuesPerLong - 1) / valuesPerLong];
         this.values.packInto(data, bitsPerValue);
         return data;
      }
   }

   @Override
   public int size() {
      return this.palette.size();
   }

   @Override
   public int idByIndex(int index) {
      return this.palette.getInt(index);
   }

   @Override
   public void setIdByIndex(int index, int id) {
      int oldId = this.palette.set(index, id);
      if (oldId != id && this.inversePalette != null) {
         this.inversePalette.put(id, index);
         if (this.inversePalette.get(oldId) == index) {
            this.inversePalette.remove(oldId);

            for (int i = 0; i < this.palette.size(); i++) {
               if (this.palette.getInt(i) == oldId) {
                  this.inversePalette.put(oldId, i);
                  break;
               }
            }
         }
      }
   }

   @Override
   public void replaceId(int oldId, int newId) {
      int index = this.inversePalette().remove(oldId);
      if (index != -1) {
         this.inversePalette.put(newId, index);

         for (int i = 0; i < this.palette.size(); i++) {
            if (this.palette.getInt(i) == oldId) {
               this.palette.set(i, newId);
            }
         }
      }
   }

   @Override
   public void replaceIds(IntUnaryOperator mapper) {
      int oldSize = this.palette.size();
      if (oldSize != 0) {
         int writeIndex = 0;
         int[] indexRemap = null;
         int firstCollision = 0;
         int[] palette = this.palette.elements();
         Int2IntMap inversePalette = this.emptyInversePalette();

         for (int readIndex = 0; readIndex < oldSize; readIndex++) {
            int mappedId = mapper.applyAsInt(palette[readIndex]);
            int existingIndex = inversePalette.putIfAbsent(mappedId, writeIndex);
            if (existingIndex == -1) {
               palette[writeIndex] = mappedId;
               if (indexRemap != null) {
                  indexRemap[readIndex - firstCollision] = writeIndex;
               }

               writeIndex++;
            } else {
               if (indexRemap == null) {
                  indexRemap = new int[oldSize - readIndex];
                  firstCollision = readIndex;
               }

               indexRemap[readIndex - firstCollision] = existingIndex;
            }
         }

         if (indexRemap != null) {
            this.palette.removeElements(writeIndex, oldSize);
            int len = this.values.size();

            for (int i = 0; i < len; i++) {
               int oldIndex = this.values.get(i);
               if (oldIndex >= firstCollision) {
                  this.values.set(i, indexRemap[oldIndex - firstCollision]);
               }
            }
         }
      }
   }

   @Override
   public void addId(int id) {
      if (this.inversePalette != null) {
         this.inversePalette.put(id, this.palette.size());
      }

      this.palette.add(id);
   }

   @Override
   public void forEachMatchingCoordinate(IntPredicate idPredicate, IntConsumer coordinateConsumer) {
      int size = this.size();
      if (size > 4096) {
         int idx = 0;

         for (int len = this.values.size(); idx < len; idx++) {
            if (idPredicate.test(this.idAt(idx))) {
               coordinateConsumer.accept(idx);
            }
         }
      } else {
         boolean[] matching = MATCHING_INDEXES.get();
         boolean any = false;

         for (int i = 0; i < size; i++) {
            boolean match = idPredicate.test(this.idByIndex(i));
            matching[i] = match;
            any |= match;
         }

         if (any) {
            int idx = 0;

            for (int lenx = this.values.size(); idx < lenx; idx++) {
               if (matching[this.paletteIndexAt(idx)]) {
                  coordinateConsumer.accept(idx);
               }
            }
         }
      }
   }

   @Override
   public void clear() {
      this.palette.clear();
      this.inversePalette = null;
   }

   private final class ByteChunkData implements DataPaletteImpl.ChunkData {
      private final byte[] data;

      public ByteChunkData(int size) {
         this.data = new byte[size];
      }

      public ByteChunkData(byte[] data) {
         this.data = data;
      }

      @Override
      public int get(int idx) {
         return this.data[idx] & 0xFF;
      }

      @Override
      public void set(int idx, int val) {
         if (val > 255) {
            DataPaletteImpl.this.values = new DataPaletteImpl.ShortChunkData(this.data);
            DataPaletteImpl.this.values.set(idx, val);
         } else {
            this.data[idx] = (byte)val;
         }
      }

      @Override
      public int size() {
         return this.data.length;
      }

      @Override
      public void packInto(long[] out, int bitsPerValue) {
         CompactArrayUtil.packWithPadding(this.data, out, bitsPerValue);
      }
   }

   interface ChunkData {
      int get(int var1);

      void set(int var1, int var2);

      int size();

      void packInto(long[] var1, int var2);
   }

   private final class PackedChunkData implements DataPaletteImpl.ChunkData {
      private final long[] data;
      private final int bitsPerValue;
      private final int valuesPerLong;
      private final long maxEntryValue;
      private final int size;

      private PackedChunkData(long[] data, int bitsPerValue, int valuesPerLong, int size) {
         this.data = data;
         this.bitsPerValue = bitsPerValue;
         this.valuesPerLong = valuesPerLong;
         this.size = size;
         this.maxEntryValue = (1L << bitsPerValue) - 1L;
      }

      @Override
      public int get(int idx) {
         int cellIndex = idx / this.valuesPerLong;
         int bitIndex = (idx - cellIndex * this.valuesPerLong) * this.bitsPerValue;
         return (int)(this.data[cellIndex] >>> bitIndex & this.maxEntryValue);
      }

      @Override
      public void set(int idx, int val) {
         DataPaletteImpl.ChunkData byteChunkData = this.extractData();
         DataPaletteImpl.this.values = byteChunkData;
         byteChunkData.set(idx, val);
      }

      private DataPaletteImpl.ChunkData extractData() {
         byte[] unpackedData = new byte[this.size];
         CompactArrayUtil.unpackWithPadding(this.data, unpackedData, this.bitsPerValue);
         return DataPaletteImpl.this.new ByteChunkData(unpackedData);
      }

      @Override
      public int size() {
         return this.size;
      }

      @Override
      public void packInto(long[] out, int newBitsPerValue) {
         this.extractData().packInto(out, newBitsPerValue);
      }
   }

   private static final class ShortChunkData implements DataPaletteImpl.ChunkData {
      private final short[] data;

      public ShortChunkData(byte[] data) {
         this.data = new short[data.length];

         for (int i = 0; i < data.length; i++) {
            this.data[i] = (short)(data[i] & 255);
         }
      }

      @Override
      public int get(int idx) {
         return this.data[idx];
      }

      @Override
      public void set(int idx, int val) {
         this.data[idx] = (short)val;
      }

      @Override
      public int size() {
         return this.data.length;
      }

      @Override
      public void packInto(long[] out, int bitsPerValue) {
         CompactArrayUtil.packWithPadding(this.data, out, bitsPerValue);
      }
   }

   private final class SingleChunkData implements DataPaletteImpl.ChunkData {
      private final int size;

      public SingleChunkData(int size) {
         this.size = size;
      }

      @Override
      public int get(int idx) {
         return 0;
      }

      @Override
      public void set(int idx, int val) {
         if (val != 0) {
            DataPaletteImpl.this.values = DataPaletteImpl.this.new ByteChunkData(this.size);
            DataPaletteImpl.this.values.set(idx, val);
         }
      }

      @Override
      public int size() {
         return this.size;
      }

      @Override
      public void packInto(long[] data, int bitsPerValue) {
      }
   }
}
