package com.viaversion.viaversion.libs.fastutil.longs;

public interface LongHash {
   public interface Strategy {
      int hashCode(long var1);

      boolean equals(long var1, long var3);
   }
}
