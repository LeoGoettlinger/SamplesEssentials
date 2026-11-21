package de.mistrx.buildpaste.listeners;

import de.mistrx.buildpaste.items.PastePaperItem;
import de.mistrx.buildpaste.items.PositionSelectorItem;
import de.mistrx.buildpaste.util.Debug;
import de.mistrx.buildpaste.util.Functions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemClickListener implements Listener {
   @EventHandler
   public void onRightClick(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      String uuid = player.getUniqueId().toString();
      if (!player.getItemInHand().getType().equals(Material.AIR)) {
         ItemStack item = event.getItem();
         if (event.getHand() == EquipmentSlot.HAND) {
            if (PositionSelectorItem.IsPositionSelector(item)) {
               if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                  Location pos = event.getClickedBlock().getLocation();
                  Functions.SetFirstPos(player, pos);
               } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                  Location pos = event.getClickedBlock().getLocation();
                  Functions.SetSecondPos(player, pos);
               }

               event.setCancelled(true);
            } else if (PastePaperItem.IsPastePaper(item) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
               String command = "paste " + PastePaperItem.GetPastePaperBuildId(item);
               Debug.Warning(command);
               player.performCommand(command);
            }
         }
      }
   }
}
