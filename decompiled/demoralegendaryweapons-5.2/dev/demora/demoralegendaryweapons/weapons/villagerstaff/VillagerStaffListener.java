package dev.demora.demoralegendaryweapons.weapons.villagerstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VillagerStaffListener implements Listener {
   private final VillagerStaff villagerStaff;

   public VillagerStaffListener(VillagerStaff villagerStaff) {
      this.villagerStaff = villagerStaff;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      boolean isRightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
      if (isRightClick) {
         if (this.villagerStaff.isVillagerStaff(item)) {
            event.setCancelled(true);
            if (this.villagerStaff.isOnCooldown(player)) {
               long remaining = this.villagerStaff.getRemainingCooldown(player);
               player.sendMessage(
                  DemoraLegendaryWeapons.getInstance()
                     .getMessageWithPrefix("weapon-messages.villager-staff-cooldown")
                     .replace("{time}", String.valueOf(remaining))
               );
            } else {
               this.villagerStaff.useStaff(player);
            }
         }
      }
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (event.getEntity().getLastDamageCause() != null) {
         DamageCause cause = event.getEntity().getLastDamageCause().getCause();
         if (cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION) {
            if (this.villagerStaff.isTaggedForReward(event.getEntity().getUniqueId())) {
               event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.EMERALD_BLOCK, 1));
               this.villagerStaff.removeRewardTag(event.getEntity().getUniqueId());
            }
         }
      }
   }
}
