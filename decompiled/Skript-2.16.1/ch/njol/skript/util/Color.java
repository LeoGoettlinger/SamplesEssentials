package ch.njol.skript.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface Color extends YggdrasilSerializable.YggdrasilExtendedSerializable {
   org.bukkit.Color asBukkitColor();

   int getAlpha();

   int getRed();

   int getGreen();

   int getBlue();

   @Nullable
   DyeColor asDyeColor();

   String getName();

   default int asARGB() {
      return this.asBukkitColor().asARGB();
   }

   default String toHexString() {
      return String.format("%02X%02X%02X", this.getRed(), this.getGreen(), this.getBlue());
   }
}
