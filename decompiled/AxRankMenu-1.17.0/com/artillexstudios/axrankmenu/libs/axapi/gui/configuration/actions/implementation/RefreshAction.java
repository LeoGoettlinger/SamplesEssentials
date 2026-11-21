package com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.GuiKeys;
import org.bukkit.entity.Player;

public final class RefreshAction extends Action<String> {
   public RefreshAction(String data) {
      super(data);
   }

   public String transform(String data) {
      return "";
   }

   @Override
   public void run(Player player, HashMapContext context) {
      Gui gui = context.get(GuiKeys.GUI);
      gui.open();
   }
}
