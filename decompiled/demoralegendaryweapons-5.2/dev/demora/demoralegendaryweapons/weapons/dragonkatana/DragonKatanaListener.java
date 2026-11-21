package dev.demora.demoralegendaryweapons.weapons.dragonkatana;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class DragonKatanaListener implements Listener {
   private final DragonKatana dragonKatana;

   public DragonKatanaListener(DragonKatana dragonKatana) {
      this.dragonKatana = dragonKatana;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent e) {
      Action a = e.getAction();
      boolean canDash = a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK;
      if (canDash) {
         Player p = e.getPlayer();
         ItemStack item = p.getInventory().getItemInMainHand();
         if (this.dragonKatana.isKatana(item)) {
            e.setCancelled(true);
            if (this.dragonKatana.isOnCooldown(p)) {
               long remaining = this.dragonKatana.getRemainingCooldown(p);
               p.sendMessage(
                  DemoraLegendaryWeapons.getInstance()
                     .getMessageWithPrefix("weapon-messages.dragon-katana-cooldown")
                     .replace("{time}", String.valueOf(remaining))
               );
            } else {
               this.dragonKatana.performDash(p);
            }
         }
      }
   }

   @EventHandler
   public void onItemHeld(PlayerItemHeldEvent e) {
      Player p = e.getPlayer();
      ItemStack newItem = p.getInventory().getItem(e.getNewSlot());
      boolean hasKatanaNow = this.dragonKatana.isKatana(newItem);
      this.dragonKatana.setHasKatana(p.getUniqueId(), hasKatanaNow);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.dragonKatana.removePlayerData(e.getPlayer().getUniqueId());
   }
}
