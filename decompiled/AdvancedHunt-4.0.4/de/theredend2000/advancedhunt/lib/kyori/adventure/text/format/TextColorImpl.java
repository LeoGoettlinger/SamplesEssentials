package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import de.theredend2000.advancedhunt.lib.kyori.adventure.util.HSVLike;
import java.util.HexFormat;
import org.jetbrains.annotations.Debug.Renderer;

@Renderer(
   text = "asHexString()"
)
record TextColorImpl(int value) implements TextColor {
   private static final HexFormat HEX_FORMATTER = HexFormat.of().withUpperCase();

   @Override
   public String toString() {
      return this.asHexString();
   }

   static float distance(final HSVLike self, final HSVLike other) {
      float hueDistance = 3.0F * Math.min(Math.abs(self.h() - other.h()), 1.0F - Math.abs(self.h() - other.h()));
      float saturationDiff = self.s() - other.s();
      float valueDiff = self.v() - other.v();
      return hueDistance * hueDistance + saturationDiff * saturationDiff + valueDiff * valueDiff;
   }

   public static String hexString(final int value) {
      return "#" + HEX_FORMATTER.toHexDigits(value, 6);
   }
}
