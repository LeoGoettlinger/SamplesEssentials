package dev.demora.demoralegendaryweapons.weapons.mjolnir;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MjolnirListener implements Listener {
   private final Mjolnir mjolnir;
   private final DemoraLegendaryWeapons plugin;

   public MjolnirListener(Mjolnir mjolnir, DemoraLegendaryWeapons plugin) {
      this.mjolnir = mjolnir;
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      if (item != null && this.mjolnir.isMjolnir(item)) {
         boolean isRightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
         if (isRightClick) {
            if (event.getHand() == EquipmentSlot.HAND) {
               event.setCancelled(true);
               if (this.mjolnir.isOnThrowCooldown(player)) {
                  long remaining = this.mjolnir.getRemainingThrowCooldown(player);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.mjolnir-cooldown").replace("{time}", String.valueOf(remaining)));
               } else {
                  this.mjolnir.setThrowCooldown(player);
                  this.mjolnir.throwMjolnir(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player attacker = (Player)event.getDamager();
         ItemStack zov = attacker.getInventory().getItemInMainHand();
         if (this.mjolnir.isMjolnir(zov)) {
            if (event.getEntity() instanceof Player) {
               Player target = (Player)event.getEntity();
               if (this.mjolnir.isOnMeleeCooldown(attacker)) {
                  long remaining = this.mjolnir.getRemainingMeleeCooldown(attacker);
                  attacker.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.mjolnir-cooldown").replace("{time}", String.valueOf(remaining)));
                  event.setCancelled(true);
               } else if (!this.mjolnir.isProcessingDamage(target.getUniqueId())) {
                  this.mjolnir.setMeleeCooldown(attacker);
                  this.mjolnir.addProcessingDamage(target.getUniqueId());

                  try {
                     double meleeDamage = this.plugin.getConfig().getDouble("weapons.mjolnir.damage.melee_damage", 20.0);
                     event.setDamage(meleeDamage);
                     this.mjolnir.performMeleeAttack(target, attacker);
                  } finally {
                     this.mjolnir.removeProcessingDamage(target.getUniqueId());
                  }
               }
            }
         }
      }
   }
}
