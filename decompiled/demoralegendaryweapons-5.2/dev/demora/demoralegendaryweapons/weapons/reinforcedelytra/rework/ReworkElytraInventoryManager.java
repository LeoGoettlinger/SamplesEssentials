package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReworkElytraInventoryManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReworkElytra reworkElytra;

   public ReworkElytraInventoryManager(DemoraLegendaryWeapons plugin, ReworkElytra reworkElytra) {
      this.plugin = plugin;
      this.reworkElytra = reworkElytra;
   }

   public void removeNormalElytraFromInventory(Player player) {
      ItemStack[] contents = player.getInventory().getContents();

      for (int i = 0; i < contents.length; i++) {
         ItemStack item = contents[i];
         if (item != null
            && item.getType() == Material.ELYTRA
            && !this.reworkElytra.isReworkElytra(item)
            && !this.plugin.getReinforcedElytra().isReinforcedElytra(item)) {
            player.getInventory().setItem(i, null);
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.rework-elytra-conflict"));
         }
      }
   }

   public boolean playerHasReworkElytra(Player player) {
      if (this.reworkElytra.isReworkElytra(player.getInventory().getChestplate())) {
         return true;
      } else {
         for (ItemStack item : player.getInventory().getStorageContents()) {
            if (this.reworkElytra.isReworkElytra(item)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean hasNormalElytraInStorage(Player player) {
      for (ItemStack item : player.getInventory().getStorageContents()) {
         if (item != null
            && item.getType() == Material.ELYTRA
            && !this.reworkElytra.isReworkElytra(item)
            && !this.plugin.getReinforcedElytra().isReinforcedElytra(item)) {
            return true;
         }
      }

      return false;
   }

   public void scheduleRemoveNormalElytra(Player player) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         if (player.isOnline()) {
            this.removeNormalElytraFromInventory(player);
         }
      }, 1L);
   }
}
