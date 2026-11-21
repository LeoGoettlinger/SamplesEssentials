package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface FloatTag extends NumberTag<Float> permits FloatTagImpl {
   byte ID = 5;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static FloatTag of(float value) {
      return new FloatTagImpl(value);
   }
}
