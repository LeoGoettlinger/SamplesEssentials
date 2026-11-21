package com.earth2me.essentials.adventure;

import com.earth2me.essentials.libs.kyori.adventure.text.format.NamedTextColor;

public final class AdventureUtil {
   private static final String LOOKUP = "0123456789abcdefklmnor";
   private static final NamedTextColor[] COLORS = new NamedTextColor[]{
      NamedTextColor.BLACK,
      NamedTextColor.DARK_BLUE,
      NamedTextColor.DARK_GREEN,
      NamedTextColor.DARK_AQUA,
      NamedTextColor.DARK_RED,
      NamedTextColor.DARK_PURPLE,
      NamedTextColor.GOLD,
      NamedTextColor.GRAY,
      NamedTextColor.DARK_GRAY,
      NamedTextColor.BLUE,
      NamedTextColor.GREEN,
      NamedTextColor.AQUA,
      NamedTextColor.RED,
      NamedTextColor.LIGHT_PURPLE,
      NamedTextColor.YELLOW,
      NamedTextColor.WHITE
   };
   private static AdventureFacet ADVENTURE_FACET_INSTANCE = null;

   private AdventureUtil() {
   }

   public static void setAdventureFacet(AdventureFacet adventureFacetInstance) {
      ADVENTURE_FACET_INSTANCE = adventureFacetInstance;
   }

   public static AdventureFacet getAdventureFacet() {
      return ADVENTURE_FACET_INSTANCE;
   }

   public static NamedTextColor fromChar(final char c) {
      int index = "0123456789abcdefklmnor".indexOf(c);
      return index != -1 && index <= 15 ? COLORS[index] : null;
   }

   public static String fromCharName(final char c) {
      NamedTextColor namedTextColor = fromChar(c);
      return namedTextColor == null ? null : namedTextColor.toString();
   }

   public static AdventureUtil.ParsedPlaceholder parsed(final String literal) {
      return new AdventureUtil.ParsedPlaceholder(literal);
   }

   public static class ParsedPlaceholder {
      private final String value;

      protected ParsedPlaceholder(String value) {
         this.value = value;
      }

      @Override
      public String toString() {
         return this.value;
      }
   }
}
