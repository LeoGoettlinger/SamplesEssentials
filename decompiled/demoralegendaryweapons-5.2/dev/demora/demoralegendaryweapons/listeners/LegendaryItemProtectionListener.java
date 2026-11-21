package dev.demora.demoralegendaryweapons.listeners;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class LegendaryItemProtectionListener implements Listener {
   private final DemoraLegendaryWeapons plugin;

   public LegendaryItemProtectionListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onItemSpawn(ItemSpawnEvent event) {
      Item item = event.getEntity();
      if (this.plugin.isLegendaryWeapon(item.getItemStack())) {
         item.setInvulnerable(true);
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onItemDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Item item) {
         if (this.plugin.isLegendaryWeapon(item.getItemStack())) {
            switch (event.getCause()) {
               case FIRE:
               case FIRE_TICK:
               case LAVA:
               case BLOCK_EXPLOSION:
               case ENTITY_EXPLOSION:
                  event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onEnchant(EnchantItemEvent event) {
      if (this.isEnchantingDisabled()) {
         if (this.plugin.isLegendaryWeapon(event.getItem())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPrepareEnchant(PrepareItemEnchantEvent event) {
      if (this.isEnchantingDisabled()) {
         if (this.plugin.isLegendaryWeapon(event.getItem())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPrepareAnvil(PrepareAnvilEvent event) {
      if (this.isEnchantingDisabled()) {
         ItemStack left = event.getInventory().getItem(0);
         ItemStack right = event.getInventory().getItem(1);
         boolean leftLegendary = left != null && this.plugin.isLegendaryWeapon(left);
         boolean rightLegendary = right != null && this.plugin.isLegendaryWeapon(right);
         if (leftLegendary && right != null || rightLegendary) {
            event.setResult(null);
         }
      }
   }

   private boolean isEnchantingDisabled() {
      return this.plugin.getConfig().getBoolean("disable-legendary-enchanting", true);
   }
}
