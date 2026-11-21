package org.lushplugins.lushrewards.libraries.lushlib.manager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.libraries.lushlib.LushLib;
import org.lushplugins.lushrewards.libraries.lushlib.gui.inventory.Gui;
import org.lushplugins.lushrewards.libraries.lushlib.listener.InventoryListener;

public class GuiManager extends Manager {
   private ConcurrentHashMap<UUID, Gui> playerGuiMap = null;

   @Override
   public void onEnable() {
      this.playerGuiMap = new ConcurrentHashMap<>();
      LushLib.getInstance().getPlugin().getServer().getPluginManager().registerEvents(new InventoryListener(), LushLib.getInstance().getPlugin());
   }

   @Override
   public void onDisable() {
      if (this.playerGuiMap != null) {
         this.playerGuiMap.values().forEach(Gui::close);
         this.playerGuiMap.clear();
         this.playerGuiMap = null;
      }
   }

   public Gui getGui(UUID uuid) {
      return this.playerGuiMap.get(uuid);
   }

   public void addGui(UUID uuid, Gui gui) {
      this.playerGuiMap.put(uuid, gui);
   }

   public void removeGui(UUID uuid) {
      this.playerGuiMap.remove(uuid);
   }

   public void closeAll() {
      this.playerGuiMap.keySet().forEach(uuid -> {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            player.closeInventory();
         }
      });
   }
}
