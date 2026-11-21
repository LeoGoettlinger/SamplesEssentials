package net.kyori.adventure.text.format;

import java.util.HexFormat;

record ShadowColorImpl(int value) implements ShadowColor {
   static final int NONE_VALUE = 0;
   static final ShadowColorImpl NONE = new ShadowColorImpl(0);
   static final HexFormat HEX_FORMATTER = HexFormat.of().withUpperCase();

   static String asHexString(final int argb) {
      int rgba = Integer.rotateLeft(argb, 8);
      return "#" + HEX_FORMATTER.toHexDigits(rgba, 8);
   }
}
