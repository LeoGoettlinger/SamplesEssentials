package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

import org.jetbrains.annotations.Range;

public interface HSVLike {
   static HSVLike hsvLike(final float h, final float s, final float v) {
      return new HSVLikeImpl(h, s, v);
   }

   static HSVLike fromRGB(@Range(from = 0L,to = 255L) final int red, @Range(from = 0L,to = 255L) final int green, @Range(from = 0L,to = 255L) final int blue) {
      float r = red / 255.0F;
      float g = green / 255.0F;
      float b = blue / 255.0F;
      float min = Math.min(r, Math.min(g, b));
      float max = Math.max(r, Math.max(g, b));
      float delta = max - min;
      float s;
      if (max != 0.0F) {
         s = delta / max;
      } else {
         s = 0.0F;
      }

      if (s == 0.0F) {
         return new HSVLikeImpl(0.0F, s, max);
      } else {
         float h;
         if (r == max) {
            h = (g - b) / delta;
         } else if (g == max) {
            h = 2.0F + (b - r) / delta;
         } else {
            h = 4.0F + (r - g) / delta;
         }

         h *= 60.0F;
         if (h < 0.0F) {
            h += 360.0F;
         }

         return new HSVLikeImpl(h / 360.0F, s, max);
      }
   }

   float h();

   float s();

   float v();
}
