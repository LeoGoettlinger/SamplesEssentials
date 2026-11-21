package com.github.jikoo.openinv.lib.planarwrappers.util.version;

import java.util.regex.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Version extends Comparable<Version> {
   default boolean lessThan(@NotNull Version other) {
      return this.compareTo(other) < 0;
   }

   default boolean lessThanOrEqual(@NotNull Version other) {
      return this.compareTo(other) <= 0;
   }

   default boolean greaterThan(@NotNull Version other) {
      return this.compareTo(other) > 0;
   }

   default boolean greaterThanOrEqual(@NotNull Version other) {
      return this.compareTo(other) >= 0;
   }

   @Contract("_ -> new")
   @NotNull
   static Version of(int... data) {
      return new IntVersion(data);
   }

   @Contract("_ -> new")
   @NotNull
   static Version of(@NotNull String data) {
      if (!Pattern.compile("^(\\d+\\.)*+\\d+$").matcher(data).matches()) {
         return new StringVersion(data);
      } else {
         String[] raw = data.split("\\.");
         int[] parsed = new int[raw.length];

         for (int index = 0; index < raw.length; index++) {
            parsed[index] = Integer.parseInt(raw[index]);
         }

         return new IntVersion(parsed);
      }
   }
}
