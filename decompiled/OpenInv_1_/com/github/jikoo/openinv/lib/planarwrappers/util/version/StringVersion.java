package com.github.jikoo.openinv.lib.planarwrappers.util.version;

import com.github.jikoo.openinv.lib.planarwrappers.util.AlphanumComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class StringVersion implements Version {
   static final AlphanumComparator COMPARATOR = new AlphanumComparator() {
      @Override
      protected int compareTail(AlphanumComparator.CompareData data1, AlphanumComparator.CompareData data2) {
         if (data1.getMarker() < data1.getLength()) {
            return this.compareSnapshot(data1, data2);
         } else {
            return data2.getMarker() < data2.getLength() ? -this.compareSnapshot(data2, data1) : 0;
         }
      }

      private int compareSnapshot(AlphanumComparator.CompareData longer, AlphanumComparator.CompareData shorter) {
         if (shorter.getLength() == 0) {
            return 1;
         } else if (this.isDigit(shorter.getString().charAt(shorter.getLength() - 1)) && shorter.getString().indexOf(45) == -1) {
            char nextChar = longer.getString().charAt(longer.getLength() - 1);
            return !this.isDigit(nextChar) && nextChar != 46 ? -1 : 1;
         } else {
            return 1;
         }
      }
   };
   @NotNull
   private final String versionText;

   StringVersion(@NotNull String version) {
      this.versionText = version;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      return this == obj || obj instanceof Version that && this.compareTo(that) == 0;
   }

   @Override
   public int hashCode() {
      return this.versionText.hashCode();
   }

   @Override
   public String toString() {
      return this.versionText;
   }

   public int compareTo(@NotNull Version that) {
      return COMPARATOR.compare(this.versionText, that.toString());
   }
}
