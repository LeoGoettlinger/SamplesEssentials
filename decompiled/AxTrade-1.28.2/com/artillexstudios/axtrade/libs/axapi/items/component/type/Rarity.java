package com.artillexstudios.axtrade.libs.axapi.items.component.type;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Rarity {
   COMMON(NamedTextColor.WHITE),
   UNCOMMON(NamedTextColor.YELLOW),
   RARE(NamedTextColor.AQUA),
   EPIC(NamedTextColor.LIGHT_PURPLE);

   private final NamedTextColor color;

   private Rarity(NamedTextColor color) {
      this.color = color;
   }

   public NamedTextColor color() {
      return this.color;
   }
}
