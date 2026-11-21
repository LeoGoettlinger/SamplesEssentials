package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import java.util.Arrays;

final class HolderSetImpl {
   record Ids(int[] ids) implements HolderSet {
      @Override
      public boolean hasTagKey() {
         return false;
      }

      @Override
      public String tagKey() {
         throw new IllegalArgumentException("This holder set has direct ids");
      }

      @Override
      public boolean hasIds() {
         return true;
      }

      @Override
      public HolderSet rewrite(Int2IntFunction idRewriter) {
         int[] mappedIds = new int[this.ids.length];
         int validCount = 0;

         for (int id : this.ids) {
            int mappedId = idRewriter.applyAsInt(id);
            if (mappedId != -1) {
               mappedIds[validCount++] = mappedId;
            }
         }

         return new HolderSetImpl.Ids(validCount == this.ids.length ? mappedIds : Arrays.copyOf(mappedIds, validCount));
      }

      @Override
      public String toString() {
         return "Ids{ids=" + Arrays.toString(this.ids) + "}";
      }
   }

   record Tag(String tagKey) implements HolderSet {
      @Override
      public boolean hasTagKey() {
         return true;
      }

      @Override
      public int[] ids() {
         throw new IllegalArgumentException("This holder set has a tag key");
      }

      @Override
      public boolean hasIds() {
         return false;
      }

      @Override
      public HolderSet rewrite(Int2IntFunction idRewriter) {
         return this;
      }
   }
}
