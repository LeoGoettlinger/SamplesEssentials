package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface NumberBinaryTag extends BinaryTag {
   @NotNull
   @Override
   BinaryTagType<? extends NumberBinaryTag> type();

   byte byteValue();

   double doubleValue();

   float floatValue();

   int intValue();

   long longValue();

   short shortValue();

   @NotNull
   Number numberValue();
}
