package dev.demora.demoralegendaryweapons.weapons.reaperscythe;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ReaperScytheListener implements Listener {
   private final ReaperScythe reaperScythe;

   public ReaperScytheListener(ReaperScythe reaperScythe) {
      this.reaperScythe = reaperScythe;
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         if (event.getEntity() instanceof Player) {
            Player attacker = (Player)event.getDamager();
            Player target = (Player)event.getEntity();
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (this.reaperScythe.isReaperScythe(weapon)) {
               if (this.reaperScythe.isOnCooldown(attacker)) {
                  long remaining = this.reaperScythe.getRemainingCooldown(attacker);
                  attacker.sendMessage(
                     DemoraLegendaryWeapons.getInstance()
                        .getMessageWithPrefix("weapon-messages.reaper-scythe-cooldown")
                        .replace("{time}", String.valueOf(remaining))
                  );
               } else {
                  this.reaperScythe.performReaperAttack(attacker, target);
               }
            }
         }
      }
   }
}
