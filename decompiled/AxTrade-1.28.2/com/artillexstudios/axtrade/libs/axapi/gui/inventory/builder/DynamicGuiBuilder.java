package com.artillexstudios.axtrade.libs.axapi.gui.inventory.builder;

import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiBuilder;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.implementation.DynamicGui;
import org.bukkit.entity.Player;

public class DynamicGuiBuilder extends GuiBuilder<DynamicGui, DynamicGuiBuilder> {
   public DynamicGui build(Player player) {
      return new DynamicGui(player, this.titleProvider, this.type, this.rows, this.context, this.modifiers);
   }
}
