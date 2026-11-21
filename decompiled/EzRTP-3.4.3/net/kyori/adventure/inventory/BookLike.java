package net.kyori.adventure.inventory;

import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface BookLike {
   @Contract(
      pure = true
   )
   Book asBook();
}
