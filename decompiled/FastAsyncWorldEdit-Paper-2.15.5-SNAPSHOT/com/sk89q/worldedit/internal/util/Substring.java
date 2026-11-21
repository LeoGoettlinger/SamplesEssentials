package com.sk89q.worldedit.internal.util;

import com.google.common.base.Preconditions;
import java.util.Objects;

public final class Substring {
   private final String substring;
   private final int start;
   private final int end;

   public static Substring from(String original, int start) {
      return new Substring(original.substring(start), start, original.length());
   }

   public static Substring from(String original, int start, int end) {
      return new Substring(original.substring(start, end), start, end);
   }

   public static Substring wrap(String substring, int start, int end) {
      Preconditions.checkArgument(0 <= start, "Start must be greater than or equal to zero");
      Preconditions.checkArgument(start <= end, "End must be greater than or equal to start");
      return new Substring(substring, start, end);
   }

   private Substring(String substring, int start, int end) {
      this.substring = substring;
      this.start = start;
      this.end = end;
   }

   public String getSubstring() {
      return this.substring;
   }

   public int getStart() {
      return this.start;
   }

   public int getEnd() {
      return this.end;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Substring substring1 = (Substring)o;
         return this.start == substring1.start && this.end == substring1.end && this.substring.equals(substring1.substring);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.substring, this.start, this.end);
   }

   @Override
   public String toString() {
      return "Substring{substring='" + this.substring + "',start=" + this.start + ",end=" + this.end + "}";
   }
}
