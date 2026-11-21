package com.sk89q.worldedit.util.formatting.text.format;

import com.sk89q.worldedit.util.formatting.text.util.NameMap;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum TextColor implements TextFormat {
   BLACK("black"),
   DARK_BLUE("dark_blue"),
   DARK_GREEN("dark_green"),
   DARK_AQUA("dark_aqua"),
   DARK_RED("dark_red"),
   DARK_PURPLE("dark_purple"),
   GOLD("gold"),
   GRAY("gray"),
   DARK_GRAY("dark_gray"),
   BLUE("blue"),
   GREEN("green"),
   AQUA("aqua"),
   RED("red"),
   LIGHT_PURPLE("light_purple"),
   YELLOW("yellow"),
   WHITE("white");

   public static final NameMap<TextColor> NAMES = NameMap.create(TextColor.class, constant -> constant.name);
   private final String name;

   private TextColor(final String name) {
      this.name = name;
   }

   @NonNull
   @Override
   public String toString() {
      return this.name;
   }
}
