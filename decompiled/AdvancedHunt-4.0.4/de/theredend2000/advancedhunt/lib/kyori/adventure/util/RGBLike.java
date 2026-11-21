package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

import org.jetbrains.annotations.Range;

public interface RGBLike {
   @Range(
      from = 0L,
      to = 255L
   )
   int red();

   @Range(
      from = 0L,
      to = 255L
   )
   int green();

   @Range(
      from = 0L,
      to = 255L
   )
   int blue();

   default HSVLike asHSV() {
      return HSVLike.fromRGB(this.red(), this.green(), this.blue());
   }
}
