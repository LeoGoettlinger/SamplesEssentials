package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

import org.jetbrains.annotations.Contract;

public interface Buildable<B> {
   @Contract(
      value = "-> new",
      pure = true
   )
   B toBuilder();
}
