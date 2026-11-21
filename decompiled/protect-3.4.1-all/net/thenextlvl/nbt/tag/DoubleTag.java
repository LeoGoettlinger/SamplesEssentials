package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface DoubleTag extends NumberTag<Double> permits DoubleTagImpl {
   byte ID = 6;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static DoubleTag of(double value) {
      return new DoubleTagImpl(value);
   }
}
