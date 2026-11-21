package net.thenextlvl.version;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface Version extends Comparable<Version> {
   @Contract(
      pure = true
   )
   int major();

   @Contract(
      pure = true
   )
   int minor();

   @Contract(
      pure = true
   )
   int patch();

   @Contract(
      pure = true
   )
   @Nullable
   String preRelease();
}
