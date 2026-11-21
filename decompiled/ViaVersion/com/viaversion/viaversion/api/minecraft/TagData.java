package com.viaversion.viaversion.api.minecraft;

import java.util.Arrays;

public record TagData(String identifier, int[] entries) {
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TagData tagData = (TagData)o;
         return !this.identifier.equals(tagData.identifier) ? false : Arrays.equals(this.entries, tagData.entries);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.identifier.hashCode();
      return 31 * result + Arrays.hashCode(this.entries);
   }
}
