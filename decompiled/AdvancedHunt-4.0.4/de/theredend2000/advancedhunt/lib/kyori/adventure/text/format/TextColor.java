package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import de.theredend2000.advancedhunt.lib.kyori.adventure.util.HSVLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.RGBLike;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public non-sealed interface TextColor extends Comparable<TextColor>, RGBLike, StyleBuilderApplicable, TextFormat {
   char HEX_CHARACTER = '#';
   String HEX_PREFIX = "#";

   static TextColor color(final int value) {
      int truncatedValue = value & 16777215;
      NamedTextColor named = NamedTextColor.namedColor(truncatedValue);
      return (TextColor)(named != null ? named : new TextColorImpl(truncatedValue));
   }

   static TextColor color(final RGBLike rgb) {
      return rgb instanceof TextColor tc ? tc : color(rgb.red(), rgb.green(), rgb.blue());
   }

   static TextColor color(final HSVLike hsv) {
      float s = hsv.s();
      float v = hsv.v();
      if (s == 0.0F) {
         return color(v, v, v);
      } else {
         float h = hsv.h() * 6.0F;
         int i = (int)Math.floor(h);
         float f = h - i;
         float p = v * (1.0F - s);
         float q = v * (1.0F - s * f);
         float t = v * (1.0F - s * (1.0F - f));
         if (i == 0) {
            return color(v, t, p);
         } else if (i == 1) {
            return color(q, v, p);
         } else if (i == 2) {
            return color(p, v, t);
         } else if (i == 3) {
            return color(p, q, v);
         } else {
            return i == 4 ? color(t, p, v) : color(v, p, q);
         }
      }
   }

   static TextColor color(@Range(from = 0L,to = 255L) final int r, @Range(from = 0L,to = 255L) final int g, @Range(from = 0L,to = 255L) final int b) {
      return color((r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF);
   }

   static TextColor color(final float r, final float g, final float b) {
      return color((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F));
   }

   @Nullable
   static TextColor fromHexString(final String string) {
      if (string.startsWith("#")) {
         try {
            int hex = Integer.parseInt(string.substring(1), 16);
            return color(hex);
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   static TextColor fromCSSHexString(final String string) {
      if (string.startsWith("#")) {
         String hexString = string.substring(1);
         if (hexString.length() != 3 && hexString.length() != 6) {
            return null;
         } else {
            int hex;
            try {
               hex = Integer.parseInt(hexString, 16);
            } catch (NumberFormatException var6) {
               return null;
            }

            if (hexString.length() == 6) {
               return color(hex);
            } else {
               int red = (hex & 3840) >> 8 | (hex & 3840) >> 4;
               int green = (hex & 240) >> 4 | hex & 240;
               int blue = (hex & 15) << 4 | hex & 15;
               return color(red, green, blue);
            }
         }
      } else {
         return null;
      }
   }

   int value();

   default String asHexString() {
      return TextColorImpl.hexString(this.value());
   }

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   default int red() {
      return this.value() >> 16 & 0xFF;
   }

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   default int green() {
      return this.value() >> 8 & 0xFF;
   }

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   default int blue() {
      return this.value() & 0xFF;
   }

   static TextColor lerp(final float t, final RGBLike a, final RGBLike b) {
      float clampedT = Math.min(1.0F, Math.max(0.0F, t));
      int ar = a.red();
      int br = b.red();
      int ag = a.green();
      int bg = b.green();
      int ab = a.blue();
      int bb = b.blue();
      return color(Math.round(ar + clampedT * (br - ar)), Math.round(ag + clampedT * (bg - ag)), Math.round(ab + clampedT * (bb - ab)));
   }

   static <C extends TextColor> C nearestColorTo(final List<C> values, final TextColor any) {
      Objects.requireNonNull(any, "color");
      float matchedDistance = Float.MAX_VALUE;
      C match = (C)values.getFirst();

      for (C potential : values) {
         float distance = TextColorImpl.distance(any.asHSV(), potential.asHSV());
         if (distance < matchedDistance) {
            match = potential;
            matchedDistance = distance;
         }

         if (distance == 0.0F) {
            break;
         }
      }

      return match;
   }

   @Override
   default void styleApply(final Style.Builder style) {
      style.color(this);
   }

   default int compareTo(final TextColor that) {
      return Integer.compare(this.value(), that.value());
   }
}
