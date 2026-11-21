package org.lushplugins.lushrewards.gui;

import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.libraries.lushlib.gui.inventory.Gui;

public interface GuiDisplayer {
   Gui getGui(Player var1);

   default void displayGui(Player player) {
      this.getGui(player).open();
   }
}
