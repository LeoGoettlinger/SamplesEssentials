package de.theredend2000.advancedhunt.lib.kyori.adventure.inventory;

import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface BookLike {
   @Contract(
      pure = true
   )
   Book asBook();
}
