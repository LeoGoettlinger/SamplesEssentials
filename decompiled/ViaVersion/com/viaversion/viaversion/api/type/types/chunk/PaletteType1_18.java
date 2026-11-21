package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.util.CompactArrayUtil;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;

public class PaletteType1_18 extends PaletteTypeBase {
   protected final int globalPaletteBits;
   protected final PaletteType type;

   public PaletteType1_18(PaletteType type, int globalPaletteBits) {
      this.globalPaletteBits = globalPaletteBits;
      this.type = type;
   }

   public DataPalette read(ByteBuf buffer) {
      int bitsPerValue = buffer.readByte();
      if (bitsPerValue == 0) {
         DataPaletteImpl palette = new DataPaletteImpl(this.type.size(), 1);
         palette.addId(Types.VAR_INT.readPrimitive(buffer));
         this.readValues(buffer, 0, palette);
         return palette;
      } else {
         if (bitsPerValue < 0 || bitsPerValue > this.type.highestBitsPerValue()) {
            bitsPerValue = this.globalPaletteBits;
         } else if (this.type == PaletteType.BLOCKS && bitsPerValue < 4) {
            bitsPerValue = 4;
         }

         DataPaletteImpl palette;
         if (bitsPerValue != this.globalPaletteBits) {
            int paletteLength = Types.VAR_INT.readPrimitive(buffer);
            palette = new DataPaletteImpl(this.type.size(), paletteLength);

            for (int i = 0; i < paletteLength; i++) {
               palette.addId(Types.VAR_INT.readPrimitive(buffer));
            }
         } else {
            palette = new DataPaletteImpl(this.type.size());
         }

         this.readValues(buffer, bitsPerValue, palette);
         return palette;
      }
   }

   protected void readValues(ByteBuf buffer, int bitsPerValue, DataPaletteImpl palette) {
      long[] values = Types.LONG_ARRAY_PRIMITIVE.read(buffer);
      if (values.length != 0 && bitsPerValue != 0) {
         int valuesPerLong = (char)(64 / bitsPerValue);
         int expectedLength = (this.type.size() + valuesPerLong - 1) / valuesPerLong;
         if (values.length == expectedLength) {
            if (bitsPerValue == this.globalPaletteBits) {
               CompactArrayUtil.iterateCompactArrayWithPadding(bitsPerValue, this.type.size(), values, palette::setIdAt);
            } else {
               palette.setPaletteIndexes(values, bitsPerValue, valuesPerLong);
            }
         }
      }
   }

   public void write(ByteBuf buffer, DataPalette palette) {
      int size = palette.size();
      if (size == 1) {
         buffer.writeByte(0);
         Types.VAR_INT.writePrimitive(buffer, palette.idByIndex(0));
         this.writeValues(buffer, palette, 0);
      } else {
         int bitsPerValue = this.bitsPerValue(size);
         buffer.writeByte(bitsPerValue);
         if (bitsPerValue != this.globalPaletteBits) {
            Types.VAR_INT.writePrimitive(buffer, size);

            for (int i = 0; i < size; i++) {
               Types.VAR_INT.writePrimitive(buffer, palette.idByIndex(i));
            }
         }

         this.writeValues(buffer, palette, bitsPerValue);
      }
   }

   protected void writeValues(ByteBuf buffer, DataPalette palette, int bitsPerValue) {
      if (bitsPerValue == 0) {
         Types.VAR_INT.writePrimitive(buffer, 0);
      } else {
         long[] values = palette.createPackedValues(bitsPerValue, this.type.size(), bitsPerValue == this.globalPaletteBits);
         Types.LONG_ARRAY_PRIMITIVE.write(buffer, values);
      }
   }

   private int bitsPerValue(int size) {
      int min = this.type == PaletteType.BLOCKS ? 4 : 1;
      int bitsPerValue = Math.max(min, MathUtil.ceilLog2(size));
      if (bitsPerValue > this.type.highestBitsPerValue()) {
         bitsPerValue = this.globalPaletteBits;
      }

      return bitsPerValue;
   }

   @Override
   public int serializedSize(DataPalette palette) {
      int size = palette.size();
      int bitsPerValue = this.bitsPerValue(size);
      int serializedTypesSize = 0;
      int serializedValuesSize;
      if (size == 1) {
         serializedTypesSize = VarIntType.varIntLength(palette.idByIndex(0));
         serializedValuesSize = this.serializedValuesSize(0);
      } else {
         if (bitsPerValue != this.globalPaletteBits) {
            serializedTypesSize = VarIntType.varIntLength(size);

            for (int i = 0; i < size; i++) {
               serializedTypesSize += VarIntType.varIntLength(palette.idByIndex(i));
            }
         }

         int valuesPerLong = 64 / bitsPerValue;
         int values = (this.type.size() + valuesPerLong - 1) / valuesPerLong;
         serializedValuesSize = this.serializedValuesSize(values);
      }

      return 1 + serializedTypesSize + serializedValuesSize;
   }

   protected int serializedValuesSize(int values) {
      return VarIntType.varIntLength(values) + 8 * values;
   }
}
