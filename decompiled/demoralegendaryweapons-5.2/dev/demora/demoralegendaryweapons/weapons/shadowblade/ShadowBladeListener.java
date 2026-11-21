package dev.demora.demoralegendaryweapons.weapons.shadowblade;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ShadowBladeListener implements Listener {
   private final ShadowBlade shadowBlade;

   public ShadowBladeListener(ShadowBlade shadowBlade) {
      this.shadowBlade = shadowBlade;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (item != null && this.shadowBlade.isShadowBlade(item)) {
            event.setCancelled(true);
            if (this.shadowBlade.isOnCooldown(player)) {
               int remainingSeconds = this.shadowBlade.getRemainingCooldown(player);
               player.sendMessage(
                  DemoraLegendaryWeapons.getInstance()
                     .getMessageWithPrefix("weapon-messages.shadow-blade-cooldown")
                     .replace("{time}", String.valueOf(remainingSeconds))
               );
            } else if (!this.shadowBlade.isInShadowMode(player)) {
               this.shadowBlade.activateShadowBlade(player);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.shadowBlade.hidePlayerFromNewJoiner(event.getPlayer());
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         Player victim = (Player)event.getEntity();
         if (this.shadowBlade.isInShadowMode(victim)) {
            this.shadowBlade.returnFromShadow(victim);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.shadowBlade.removePlayerData(event.getPlayer());
   }
}
