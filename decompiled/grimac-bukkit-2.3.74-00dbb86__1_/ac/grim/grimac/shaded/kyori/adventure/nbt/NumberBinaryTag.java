package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

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
