package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import de.theredend2000.advancedhunt.lib.kyori.adventure.util.HSVLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Index;
import java.util.List;
import org.jspecify.annotations.Nullable;

public final class NamedTextColor implements TextColor {
   static final int BLACK_VALUE = 0;
   static final int DARK_BLUE_VALUE = 170;
   static final int DARK_GREEN_VALUE = 43520;
   static final int DARK_AQUA_VALUE = 43690;
   static final int DARK_RED_VALUE = 11141120;
   static final int DARK_PURPLE_VALUE = 11141290;
   static final int GOLD_VALUE = 16755200;
   static final int GRAY_VALUE = 11184810;
   static final int DARK_GRAY_VALUE = 5592405;
   static final int BLUE_VALUE = 5592575;
   static final int GREEN_VALUE = 5635925;
   static final int AQUA_VALUE = 5636095;
   static final int RED_VALUE = 16733525;
   static final int LIGHT_PURPLE_VALUE = 16733695;
   static final int YELLOW_VALUE = 16777045;
   static final int WHITE_VALUE = 16777215;
   public static final NamedTextColor BLACK = new NamedTextColor("black", 0);
   public static final NamedTextColor DARK_BLUE = new NamedTextColor("dark_blue", 170);
   public static final NamedTextColor DARK_GREEN = new NamedTextColor("dark_green", 43520);
   public static final NamedTextColor DARK_AQUA = new NamedTextColor("dark_aqua", 43690);
   public static final NamedTextColor DARK_RED = new NamedTextColor("dark_red", 11141120);
   public static final NamedTextColor DARK_PURPLE = new NamedTextColor("dark_purple", 11141290);
   public static final NamedTextColor GOLD = new NamedTextColor("gold", 16755200);
   public static final NamedTextColor GRAY = new NamedTextColor("gray", 11184810);
   public static final NamedTextColor DARK_GRAY = new NamedTextColor("dark_gray", 5592405);
   public static final NamedTextColor BLUE = new NamedTextColor("blue", 5592575);
   public static final NamedTextColor GREEN = new NamedTextColor("green", 5635925);
   public static final NamedTextColor AQUA = new NamedTextColor("aqua", 5636095);
   public static final NamedTextColor RED = new NamedTextColor("red", 16733525);
   public static final NamedTextColor LIGHT_PURPLE = new NamedTextColor("light_purple", 16733695);
   public static final NamedTextColor YELLOW = new NamedTextColor("yellow", 16777045);
   public static final NamedTextColor WHITE = new NamedTextColor("white", 16777215);
   static final List<NamedTextColor> VALUES = List.of(
      BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE
   );
   public static final Index<String, NamedTextColor> NAMES = Index.create(NamedTextColor::name, VALUES);
   private final String name;
   private final int value;
   private final HSVLike hsvLike;

   @Nullable
   public static NamedTextColor namedColor(final int value) {
      return switch (value) {
         case 0 -> BLACK;
         case 170 -> DARK_BLUE;
         case 43520 -> DARK_GREEN;
         case 43690 -> DARK_AQUA;
         case 5592405 -> DARK_GRAY;
         case 5592575 -> BLUE;
         case 5635925 -> GREEN;
         case 5636095 -> AQUA;
         case 11141120 -> DARK_RED;
         case 11141290 -> DARK_PURPLE;
         case 11184810 -> GRAY;
         case 16733525 -> RED;
         case 16733695 -> LIGHT_PURPLE;
         case 16755200 -> GOLD;
         case 16777045 -> YELLOW;
         case 16777215 -> WHITE;
         default -> null;
      };
   }

   public static NamedTextColor nearestTo(final TextColor any) {
      return any instanceof NamedTextColor namedTextColor ? namedTextColor : TextColor.nearestColorTo(VALUES, any);
   }

   private NamedTextColor(final String name, final int value) {
      this.name = name;
      this.value = value;
      this.hsvLike = HSVLike.fromRGB(this.red(), this.green(), this.blue());
   }

   public String name() {
      return this.name;
   }

   @Override
   public int value() {
      return this.value;
   }

   @Override
   public HSVLike asHSV() {
      return this.hsvLike;
   }

   @Override
   public String toString() {
      return this.name();
   }
}
