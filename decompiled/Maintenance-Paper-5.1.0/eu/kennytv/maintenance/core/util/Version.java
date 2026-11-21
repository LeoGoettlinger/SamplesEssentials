package eu.kennytv.maintenance.core.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

public final class Version implements Comparable<Version> {
   private final int[] parts;
   private final String version;
   private final String tag;

   public Version(String version) {
      Preconditions.checkArgument(version != null && !version.isEmpty());
      int index = version.indexOf(45);
      String[] split = (index == -1 ? version : version.substring(0, index)).split("\\.");
      this.parts = new int[split.length];

      for (int i = 0; i < split.length; i++) {
         this.parts[i] = Integer.parseInt(split[i]);
      }

      this.version = version;
      this.tag = index != -1 ? version.substring(index + 1) : "";
   }

   public int compareTo(@Nullable Version version) {
      if (version == null) {
         return 0;
      } else {
         int max = Math.max(this.parts.length, version.parts.length);

         for (int i = 0; i < max; i++) {
            int partA = i < this.parts.length ? this.parts[i] : 0;
            int partB = i < version.parts.length ? version.parts[i] : 0;
            if (partA < partB) {
               return -1;
            }

            if (partA > partB) {
               return 1;
            }
         }

         if (this.tag.isEmpty() && !version.tag.isEmpty()) {
            return 1;
         } else {
            return !this.tag.isEmpty() && version.tag.isEmpty() ? -1 : 0;
         }
      }
   }

   public String getTag() {
      return this.tag;
   }

   @Override
   public String toString() {
      return this.version;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof Version other ? this.version.equals(other.version) : false;
      }
   }
}
