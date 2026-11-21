package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs;

import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Version implements Comparable<Version> {
   private final Pattern pattern;
   private final int[] cursors;
   private String id;

   Version(@Nullable String id, @NotNull Pattern pattern, int[] cursors) {
      this.id = id;
      this.pattern = pattern;
      this.cursors = cursors;
      if (id == null) {
         this.buildID();
      }
   }

   public int compareTo(Version o) {
      if (!this.pattern.equals(o.pattern)) {
         throw new ClassCastException("Compared versions are not defined by the same pattern!");
      } else {
         for (int index = 0; index < this.cursors.length; index++) {
            int compared = Integer.compare(this.cursors[index], o.cursors[index]);
            if (compared != 0) {
               return compared;
            }
         }

         return 0;
      }
   }

   public int getCursor(int index) {
      return this.cursors[index];
   }

   public void next() {
      for (int index = this.cursors.length - 1; index >= 0; index--) {
         int cursor = this.cursors[index];
         if (cursor + 1 < this.pattern.getSegment(index).length()) {
            this.cursors[index] = cursor + 1;
            break;
         }

         this.cursors[index] = 0;
      }

      this.buildID();
   }

   private void buildID() {
      StringBuilder builder = new StringBuilder();

      for (int index = 0; index < this.cursors.length; index++) {
         builder.append(this.pattern.getSegment(index).getElement(this.cursors[index]));
      }

      this.id = builder.toString();
   }

   public String asID() {
      return this.id;
   }

   public Version copy() {
      return new Version(this.id, this.pattern, Arrays.copyOf(this.cursors, this.cursors.length));
   }

   public Pattern getPattern() {
      return this.pattern;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Version)) {
         return false;
      } else {
         Version version = (Version)o;
         return this.pattern.equals(version.pattern) && Arrays.equals(this.cursors, version.cursors);
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(this.pattern);
      return 31 * result + Arrays.hashCode(this.cursors);
   }

   @Override
   public String toString() {
      return "Version{pattern=" + this.pattern + ", cursors=" + Arrays.toString(this.cursors) + ", id='" + this.id + '\'' + '}';
   }
}
