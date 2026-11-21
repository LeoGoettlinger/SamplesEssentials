package com.github.jikoo.openinv.lib.planarwrappers.util.version;

import org.jetbrains.annotations.NotNull;

class IntVersion implements Version {
   private final int[] data;
   private String toString;

   IntVersion(@NotNull int... data) {
      this.data = data;
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj || obj instanceof Version that && this.compareTo(that) == 0;
   }

   @Override
   public int hashCode() {
      int hash = 1;

      for (int index = this.data.length - 1; index >= 0; index--) {
         if (hash != 1 || this.data[index] != 0) {
            hash = hash * 67 + this.data[index];
         }
      }

      return hash;
   }

   @Override
   public String toString() {
      if (this.toString != null) {
         return this.toString;
      } else {
         int maxLength = this.data.length - 1;
         StringBuilder builder = new StringBuilder();

         for (int index = 0; index <= maxLength; index++) {
            builder.append(this.data[index]);
            if (index != maxLength) {
               builder.append('.');
            }
         }

         this.toString = builder.toString();
         return this.toString;
      }
   }

   public int compareTo(@NotNull Version other) {
      if (other instanceof IntVersion that) {
         int compareLen = Math.max(this.data.length, that.data.length);

         for (int index = 0; index < compareLen; index++) {
            int thisData = this.data.length > index ? this.data[index] : 0;
            int thatData = that.data.length > index ? that.data[index] : 0;
            int compare = thisData - thatData;
            if (compare != 0) {
               return compare;
            }
         }

         return 0;
      } else {
         return StringVersion.COMPARATOR.compare(this.toString(), other.toString());
      }
   }
}
