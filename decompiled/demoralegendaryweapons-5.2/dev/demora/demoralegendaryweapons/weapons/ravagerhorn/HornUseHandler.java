package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class HornUseHandler {
   public static void handle(PlayerInteractEvent event, RideStateCache state, DemoraLegendaryWeapons plugin, RavagerHorn ravagerHorn) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (ravagerHorn.isRavagerHorn(item)) {
               event.setCancelled(true);
               if (player.getVehicle() == null) {
                  long remaining = state.getRemainingCooldownSeconds(player.getUniqueId());
                  if (remaining > 0L) {
                     player.sendMessage(plugin.getMessageWithPrefix("weapon-messages.ravager-horn-cooldown").replace("{time}", String.valueOf(remaining)));
                  } else {
                     Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(2));
                     spawnLoc.setY(player.getLocation().getY());
                     Ravager ravager = (Ravager)player.getWorld().spawn(spawnLoc, Ravager.class, r -> {
                        r.setAware(false);
                        double health = plugin.getConfig().getDouble("weapons.ravager_horn.health", 100.0);
                        if (AttributeCompat.getMaxHealth() != null) {
                           AttributeInstance maxHealth = r.getAttribute(AttributeCompat.getMaxHealth());
                           if (maxHealth != null) {
                              maxHealth.setBaseValue(health);
                           }
                        }

                        r.setHealth(Math.max(1.0, health));
                     });
                     state.putMount(player.getUniqueId(), ravager);
                     ravager.addPassenger(player);
                     player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0F, 1.0F);
                  }
               }
            }
         }
      }
   }
}
