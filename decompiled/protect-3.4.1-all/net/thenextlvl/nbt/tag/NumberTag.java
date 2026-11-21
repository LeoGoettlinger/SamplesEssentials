package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public interface NumberTag<T extends Number> extends ValueTag<T> {
   @Contract(
      value = "_ -> new",
      pure = true
   )
   static NumberTag<?> of(Number number) {
      return (NumberTag<?>)(switch (number) {
         case Integer value -> IntTag.of(value);
         case Float valuex -> FloatTag.of(valuex);
         case Short valuexx -> ShortTag.of(valuexx);
         case Long valuexxx -> LongTag.of(valuexxx);
         case Byte valuexxxx -> ByteTag.of(valuexxxx);
         default -> DoubleTag.of(number.doubleValue());
      });
   }
}
