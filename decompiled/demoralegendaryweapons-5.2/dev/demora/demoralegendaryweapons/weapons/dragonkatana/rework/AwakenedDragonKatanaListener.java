package dev.demora.demoralegendaryweapons.weapons.dragonkatana.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AwakenedDragonKatanaListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final AwakenedDragonKatana awakenedKatana;

   public AwakenedDragonKatanaListener(DemoraLegendaryWeapons plugin, AwakenedDragonKatana awakenedKatana) {
      this.plugin = plugin;
      this.awakenedKatana = awakenedKatana;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         final Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.awakenedKatana.isAwakenedDragonKatana(item)) {
            Action mikumikubeeem = event.getAction();
            boolean canDash = mikumikubeeem == Action.RIGHT_CLICK_AIR || mikumikubeeem == Action.RIGHT_CLICK_BLOCK;
            if (canDash) {
               event.setCancelled(true);
               if (this.awakenedKatana.isOnTeleportCooldown(player)) {
                  long remaining = this.awakenedKatana.getTeleportRemainingCooldown(player);
                  player.sendMessage(
                     this.plugin.getMessageWithPrefix("weapon-messages.awakened-dragon-katana-cooldown").replace("{time}", String.valueOf(remaining))
                  );
                  return;
               }

               final Location fromLoc = player.getLocation().clone();
               this.plugin.getDragonKatana().performDash(player);
               (new BukkitRunnable() {
                  public void run() {
                     Location toLoc = player.getLocation().clone();
                     AwakenedDragonKatanaListener.this.awakenedKatana.handleTeleport(player, fromLoc, toLoc);
                  }
               }).runTaskLater(this.plugin, 1L);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      ItemStack item = player.getInventory().getItemInMainHand();
      if (this.awakenedKatana.isAwakenedDragonKatana(item)) {
         if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.CHORUS_FRUIT) {
            this.awakenedKatana.handleTeleport(player, event.getFrom(), event.getTo());
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.awakenedKatana.removePlayerData(event.getPlayer().getUniqueId());
   }
}
