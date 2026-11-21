package org.skriptlang.skript.bukkit.bossbar;

import ch.njol.skript.util.Color;
import ch.njol.skript.util.SkriptColor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.Nullable;
import org.bukkit.boss.BarColor;
import org.skriptlang.skript.util.LabColor;

public final class BossBarUtils {
   private BossBarUtils() {
   }

   public static Color rgbFromBarColor(BarColor color) {
      return Arrays.stream(BossBarUtils.BarColorRGB.values())
         .filter(barColor -> barColor.barColor == color)
         .map(barColor -> barColor.color)
         .findFirst()
         .orElse(null);
   }

   @Nullable
   public static BarColor nearest(Color color) {
      LabColor labColor = LabColor.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
      Optional<BossBarUtils.BarColorRGB> value = Arrays.stream(BossBarUtils.BarColorRGB.values())
         .min(
            Comparator.comparingDouble(
               barColor -> labColor.euclideanDistanceSquared(LabColor.fromRGB(barColor.color.getRed(), barColor.color.getGreen(), barColor.color.getBlue()))
            )
         );
      if (value.isEmpty()) {
         return null;
      } else {
         LabColor bestLab = LabColor.fromRGB(value.get().color.getRed(), value.get().color.getGreen(), value.get().color.getBlue());
         return labColor.euclideanDistanceSquared(bestLab) < 2500.0 ? value.get().barColor : null;
      }
   }

   static enum BarColorRGB {
      PINK(BarColor.PINK, SkriptColor.LIGHT_RED),
      BLUE(BarColor.BLUE, SkriptColor.DARK_BLUE),
      RED(BarColor.RED, SkriptColor.DARK_RED),
      GREEN(BarColor.GREEN, SkriptColor.LIGHT_GREEN),
      YELLOW(BarColor.YELLOW, SkriptColor.YELLOW),
      PURPLE(BarColor.PURPLE, SkriptColor.DARK_PURPLE),
      WHITE(BarColor.WHITE, SkriptColor.WHITE);

      final BarColor barColor;
      final Color color;

      private BarColorRGB(BarColor barColor, SkriptColor color) {
         this.barColor = barColor;
         this.color = color;
      }
   }
}
