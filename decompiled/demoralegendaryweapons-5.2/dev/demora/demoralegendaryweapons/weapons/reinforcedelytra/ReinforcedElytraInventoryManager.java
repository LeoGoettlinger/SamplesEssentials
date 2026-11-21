package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReinforcedElytraInventoryManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReinforcedElytra reinforcedElytra;

   public ReinforcedElytraInventoryManager(DemoraLegendaryWeapons plugin, ReinforcedElytra reinforcedElytra) {
      this.plugin = plugin;
      this.reinforcedElytra = reinforcedElytra;
   }

   public void removeNormalElytraFromInventory(Player player) {
      ItemStack[] contents = player.getInventory().getContents();

      for (int i = 0; i < contents.length; i++) {
         ItemStack item = contents[i];
         if (item != null && item.getType() == Material.ELYTRA && !this.reinforcedElytra.isReinforcedElytra(item)) {
            player.getInventory().setItem(i, null);
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.reinforced-elytra-conflict"));
         }
      }
   }

   public boolean playerHasReinforcedElytra(Player player) {
      return this.reinforcedElytra.isReinforcedElytra(player.getInventory().getChestplate()) ? true : this.playerHasReinforcedElytraInStorage(player);
   }

   private boolean playerHasReinforcedElytraInStorage(Player player) {
      for (ItemStack item : player.getInventory().getStorageContents()) {
         if (this.reinforcedElytra.isReinforcedElytra(item)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasNormalElytraInStorage(Player player) {
      for (ItemStack item : player.getInventory().getStorageContents()) {
         if (item != null && item.getType() == Material.ELYTRA && !this.reinforcedElytra.isReinforcedElytra(item)) {
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
