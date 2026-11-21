package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class RavagerHornUnavailableListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final RavagerHorn ravagerHorn;

   public RavagerHornUnavailableListener(DemoraLegendaryWeapons plugin, RavagerHorn ravagerHorn) {
      this.plugin = plugin;
      this.ravagerHorn = ravagerHorn;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onInteract(PlayerInteractEvent event) {
      if (this.plugin.getRavagerHornListener() == null) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() == EquipmentSlot.HAND) {
               Player player = event.getPlayer();
               ItemStack item = player.getInventory().getItemInMainHand();
               if (this.ravagerHorn.isRavagerHorn(item)) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.ravager-horn-unavailable-version"));
               }
            }
         }
      }
   }
}
