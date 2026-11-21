package net.thenextlvl.protect.flag;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public interface Flag<T> extends Comparable<Flag<?>> {
   @Contract(
      pure = true
   )
   @NonNull
   Key key();

   @Contract(
      pure = true
   )
   @NonNull
   Class<? extends T> type();

   @Contract(
      pure = true
   )
   T defaultValue();
}
