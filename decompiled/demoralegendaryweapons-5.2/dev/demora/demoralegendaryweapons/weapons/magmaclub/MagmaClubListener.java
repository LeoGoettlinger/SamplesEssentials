package dev.demora.demoralegendaryweapons.weapons.magmaclub;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MagmaClubListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final MagmaClub magmaClub;

   public MagmaClubListener(DemoraLegendaryWeapons plugin, MagmaClub magmaClub) {
      this.plugin = plugin;
      this.magmaClub = magmaClub;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      if (item != null && this.magmaClub.isMagmaClub(item)) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            this.magmaClub.fireBlast(player);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.magmaClub.removePlayerData(event.getPlayer());
   }
}
