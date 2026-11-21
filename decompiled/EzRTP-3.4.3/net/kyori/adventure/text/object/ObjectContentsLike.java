package net.kyori.adventure.text.object;

import org.jetbrains.annotations.Contract;

public interface ObjectContentsLike {
   @Contract(
      pure = true
   )
   ObjectContents asObjectContents();
}
