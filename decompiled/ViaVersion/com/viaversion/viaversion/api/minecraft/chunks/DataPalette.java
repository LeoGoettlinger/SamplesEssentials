package com.viaversion.viaversion.api.minecraft.chunks;

import com.viaversion.viaversion.util.CompactArrayUtil;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

public interface DataPalette {
   int index(int var1, int var2, int var3);

   int idAt(int var1);

   default int idAt(int sectionX, int sectionY, int sectionZ) {
      return this.idAt(this.index(sectionX, sectionY, sectionZ));
   }

   void setIdAt(int var1, int var2);

   default void setIdAt(int sectionX, int sectionY, int sectionZ, int id) {
      this.setIdAt(this.index(sectionX, sectionY, sectionZ), id);
   }

   int idByIndex(int var1);

   void setIdByIndex(int var1, int var2);

   int paletteIndexAt(int var1);

   void setPaletteIndexAt(int var1, int var2);

   default void replaceIds(IntUnaryOperator mapper) {
      for (int i = 0; i < this.size(); i++) {
         int id = this.idByIndex(i);
         int newId = mapper.applyAsInt(id);
         if (newId != id) {
            this.setIdByIndex(i, newId);
         }
      }
   }

   void addId(int var1);

   default void replaceId(int oldId, int newId) {
      for (int i = 0; i < this.size(); i++) {
         if (this.idByIndex(i) == oldId) {
            this.setIdByIndex(i, newId);
         }
      }
   }

   void forEachMatchingCoordinate(IntPredicate var1, IntConsumer var2);

   default long[] createPackedValues(int bitsPerValue, int entries, boolean rawIds) {
      return CompactArrayUtil.createCompactArrayWithPadding(bitsPerValue, entries, rawIds ? this::idAt : this::paletteIndexAt);
   }

   int size();

   void clear();
}
