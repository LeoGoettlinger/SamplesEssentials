package com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;

public final class CloseAction extends Action<Object> {
   public CloseAction(String data) {
      super(data);
   }

   @Override
   public Object transform(String data) {
      return "";
   }

   @Override
   public void run(Player player, HashMapContext context) {
      player.closeInventory(Reason.PLUGIN);
   }
}
