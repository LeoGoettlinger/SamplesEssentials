package com.viaversion.viaversion.bukkit.listeners.v1_20_5to1_21;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class LegacyChangeItemListener extends PlayerChangeItemListener {
   public LegacyChangeItemListener(ViaVersionPlugin plugin) {
      super(plugin);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockDamageEvent(BlockDamageEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItemInHand();
      this.sendAttributeUpdate(player, item, PlayerChangeItemListener.Slot.HAND);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player
         && (event.getInventory().getType() == InventoryType.CRAFTING || event.getInventory().getType() == InventoryType.PLAYER)) {
         this.sendArmorUpdate(player);
      }
   }

   private void sendArmorUpdate(Player player) {
      UserConnection connection = this.getUserConnection(player);
      EfficiencyAttributeStorage storage = this.getEfficiencyStorage(connection);
      if (storage != null) {
         PlayerInventory inventory = player.getInventory();
         ItemStack helmet = inventory.getHelmet();
         ItemStack leggings = this.swiftSneak != null ? inventory.getLeggings() : null;
         ItemStack boots = this.depthStrider != null ? inventory.getBoots() : null;
         storage.setEnchants(
            player.getEntityId(),
            connection,
            storage.activeEnchants()
               .aquaAffinity(helmet != null ? helmet.getEnchantmentLevel(this.aquaAffinity) : 0)
               .swiftSneak(leggings != null ? leggings.getEnchantmentLevel(this.swiftSneak) : 0)
               .depthStrider(boots != null ? boots.getEnchantmentLevel(this.depthStrider) : 0)
         );
      }
   }
}
