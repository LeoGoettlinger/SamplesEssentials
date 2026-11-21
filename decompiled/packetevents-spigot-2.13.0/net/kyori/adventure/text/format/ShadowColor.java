package net.kyori.adventure.text.format;

import net.kyori.adventure.util.ARGBLike;
import net.kyori.adventure.util.RGBLike;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface ShadowColor extends StyleBuilderApplicable, ARGBLike {
   @NotNull
   static ShadowColor lerp(final float t, @NotNull final ARGBLike a, @NotNull final ARGBLike b) {
      float clampedT = Math.min(1.0F, Math.max(0.0F, t));
      int ar = a.red();
      int br = b.red();
      int ag = a.green();
      int bg = b.green();
      int ab = a.blue();
      int bb = b.blue();
      int aa = a.alpha();
      int ba = b.alpha();
      return shadowColor(
         Math.round(ar + clampedT * (br - ar)),
         Math.round(ag + clampedT * (bg - ag)),
         Math.round(ab + clampedT * (bb - ab)),
         Math.round(aa + clampedT * (ba - aa))
      );
   }

   @NotNull
   static ShadowColor none() {
      return ShadowColorImpl.NONE;
   }

   @Contract(
      pure = true
   )
   @NotNull
   static ShadowColor shadowColor(final int argb) {
      return (ShadowColor)(argb == 0 ? none() : new ShadowColorImpl(argb));
   }

   @Contract(
      pure = true
   )
   @NotNull
   static ShadowColor shadowColor(
      @Range(from = 0L,to = 255L) final int red,
      @Range(from = 0L,to = 255L) final int green,
      @Range(from = 0L,to = 255L) final int blue,
      @Range(from = 0L,to = 255L) final int alpha
   ) {
      int value = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
      return (ShadowColor)(value == 0 ? none() : new ShadowColorImpl(value));
   }

   @Contract(
      pure = true
   )
   @NotNull
   static ShadowColor shadowColor(@NotNull final RGBLike rgb, @Range(from = 0L,to = 255L) final int alpha) {
      return shadowColor(rgb.red(), rgb.green(), rgb.blue(), alpha);
   }

   @NotNull
   static ShadowColor shadowColor(@NotNull final ARGBLike argb) {
      return argb instanceof ShadowColor ? (ShadowColor)argb : shadowColor(argb.red(), argb.green(), argb.blue(), argb.alpha());
   }

   @Contract(
      pure = true
   )
   @Nullable
   static ShadowColor fromHexString(@Pattern("#[0-9a-fA-F]{8}") @NotNull final String hex) {
      if (hex.length() != 9) {
         return null;
      } else if (!hex.startsWith("#")) {
         return null;
      } else {
         try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            int a = Integer.parseInt(hex.substring(7, 9), 16);
            return new ShadowColorImpl(a << 24 | r << 16 | g << 8 | b);
         } catch (NumberFormatException var5) {
            return null;
         }
      }
   }

   @NotNull
   default String asHexString() {
      int argb = this.value();
      int a = argb >> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      return String.format("#%02X%02X%02X%02X", r, g, b, a);
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

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   default int alpha() {
      return this.value() >> 24 & 0xFF;
   }

   int value();

   @Override
   default void styleApply(@NotNull final Style.Builder style) {
      style.shadowColor(this);
   }
}
