package com.earth2me.essentials.utils;

import com.earth2me.essentials.libs.kyori.adventure.util.HSVLike;

public final class DownsampleUtil {
   private static final DownsampleUtil.NamedTextColor[] VALUES = new DownsampleUtil.NamedTextColor[]{
      new DownsampleUtil.NamedTextColor("0", 0),
      new DownsampleUtil.NamedTextColor("1", 170),
      new DownsampleUtil.NamedTextColor("2", 43520),
      new DownsampleUtil.NamedTextColor("3", 43690),
      new DownsampleUtil.NamedTextColor("4", 11141120),
      new DownsampleUtil.NamedTextColor("5", 11141290),
      new DownsampleUtil.NamedTextColor("6", 16755200),
      new DownsampleUtil.NamedTextColor("7", 11184810),
      new DownsampleUtil.NamedTextColor("8", 5592405),
      new DownsampleUtil.NamedTextColor("9", 5592575),
      new DownsampleUtil.NamedTextColor("a", 5635925),
      new DownsampleUtil.NamedTextColor("b", 5636095),
      new DownsampleUtil.NamedTextColor("c", 16733525),
      new DownsampleUtil.NamedTextColor("d", 16733695),
      new DownsampleUtil.NamedTextColor("e", 16777045),
      new DownsampleUtil.NamedTextColor("f", 16777215)
   };

   private DownsampleUtil() {
   }

   public static String nearestTo(final int rgb) {
      HSVLike any = HSVLike.fromRGB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
      float matchedDistance = Float.MAX_VALUE;
      DownsampleUtil.NamedTextColor match = VALUES[0];

      for (DownsampleUtil.NamedTextColor potential : VALUES) {
         float distance = distance(any, potential.hsv);
         if (distance < matchedDistance) {
            match = potential;
            matchedDistance = distance;
         }

         if (distance == 0.0F) {
            break;
         }
      }

      return match.code;
   }

   private static float distance(final HSVLike self, final HSVLike other) {
      float hueDistance = 3.0F * Math.abs(self.h() - other.h());
      float saturationDiff = self.s() - other.s();
      float valueDiff = self.v() - other.v();
      return hueDistance * hueDistance + saturationDiff * saturationDiff + valueDiff * valueDiff;
   }

   private static final class NamedTextColor {
      private final String code;
      private final int value;
      private final HSVLike hsv;

      private NamedTextColor(final String code, final int value) {
         this.code = code;
         this.value = value;
         this.hsv = HSVLike.fromRGB(this.red(), this.green(), this.blue());
      }

      private int red() {
         return this.value >> 16 & 0xFF;
      }

      private int green() {
         return this.value >> 8 & 0xFF;
      }

      private int blue() {
         return this.value & 0xFF;
      }
   }
}
