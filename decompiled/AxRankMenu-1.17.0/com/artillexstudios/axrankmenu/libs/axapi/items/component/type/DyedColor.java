package com.artillexstudios.axrankmenu.libs.axapi.items.component.type;

import org.bukkit.Color;

public record DyedColor(Color color, boolean showInTooltip) {
   public int rgb() {
      return this.color.asRGB();
   }
}
