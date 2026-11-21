package com.artillexstudios.axafkzone.listeners;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.items.NBTWrapper;
import com.artillexstudios.axafkzone.libs.axapi.serializers.Serializers;
import com.artillexstudios.axafkzone.selection.Selection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WandListeners implements Listener {
   private static final Map<Player, Selection> selections = new HashMap<>();

   @NotNull
   public static Map<Player, Selection> getSelections() {
      return selections;
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      selections.remove(event.getPlayer());
   }

   @EventHandler
   public void onInteract(@NotNull PlayerInteractEvent event) {
      if (event.getClickedBlock() != null) {
         if (event.getItem() != null && !event.getItem().getType().equals(Material.AIR)) {
            ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
            if (!itemInHand.getType().equals(Material.AIR)) {
               NBTWrapper wrapper = new NBTWrapper(itemInHand);
               if (wrapper.getBooleanOr("axafkzone-wand", false)) {
                  event.setCancelled(true);
                  if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                     this.onLeftClick(event.getPlayer(), event.getClickedBlock().getLocation());
                  } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                     this.onRightClick(event.getPlayer(), event.getClickedBlock().getLocation());
                  }
               }
            }
         }
      }
   }

   private void onLeftClick(@NotNull Player player, @NotNull Location location) {
      if (!selections.containsKey(player)) {
         selections.put(player, new Selection(player));
      }

      if (!Objects.equals(selections.get(player).getPosition1(), location)) {
         player.setCooldown(player.getInventory().getItemInMainHand().getType(), 5);
         selections.get(player).setPosition1(location);
         AxAFKZone.MESSAGEUTILS.sendLang(player, "selection.pos1", Collections.singletonMap("%location%", Serializers.LOCATION.serialize(location)));
      }
   }

   private void onRightClick(@NotNull Player player, @NotNull Location location) {
      if (!selections.containsKey(player)) {
         selections.put(player, new Selection(player));
      }

      if (!Objects.equals(selections.get(player).getPosition2(), location)) {
         player.setCooldown(player.getInventory().getItemInMainHand().getType(), 5);
         selections.get(player).setPosition2(location);
         AxAFKZone.MESSAGEUTILS.sendLang(player, "selection.pos2", Collections.singletonMap("%location%", Serializers.LOCATION.serialize(location)));
      }
   }
}
