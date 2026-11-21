package de.oliver.fancynpcs.listeners;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcCreateEvent;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.api.events.NpcRemoveEvent;
import java.util.Comparator;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerNpcsListener implements Listener {
   private static final boolean isUsingPlotSquared = FancyNpcs.getInstance().isUsingPlotSquared();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   public static boolean checkNpcOwnership(Player player, Location loc) {
      if (!isUsingPlotSquared) {
         return true;
      } else if (loc == null || player == null) {
         return false;
      } else if (player.hasPermission("fancynpcs.admin")) {
         return true;
      } else {
         com.plotsquared.core.location.Location npcLoc = com.plotsquared.core.location.Location.at(
            loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
         );
         PlotArea plotArea = PlotSquared.platform().plotAreaManager().getPlotArea(npcLoc);
         if (plotArea == null) {
            return false;
         } else {
            Plot plot = plotArea.getOwnedPlot(npcLoc);
            return plot != null && plot.isOwner(player.getUniqueId());
         }
      }
   }

   @EventHandler
   public void onNpcCreate(NpcCreateEvent event) {
      if (event.getCreator() instanceof Player player) {
         boolean isOwner = checkNpcOwnership(player, event.getNpc().getData().getLocation());
         if (!isOwner) {
            this.translator.translate("player_npcs_create_failure_not_owned_plot").send(player);
            event.setCancelled(true);
         } else {
            int maxNpcs = FancyNpcs.getInstance()
               .getFancyNpcConfig()
               .getMaxNpcsPerPermission()
               .entrySet()
               .stream()
               .filter(entry -> player.hasPermission(entry.getKey()))
               .max(Comparator.comparingInt(Entry::getValue))
               .map(Entry::getValue)
               .orElse(Integer.MAX_VALUE);
            int npcAmount = 0;

            for (Npc npc : FancyNpcs.getInstance().getNpcManager().getAllNpcs()) {
               if (npc.getData().getCreator().equals(player.getUniqueId())) {
                  npcAmount++;
               }
            }

            if (npcAmount >= maxNpcs && !player.hasPermission("fancynpcs.admin")) {
               this.translator.translate("player_npcs_create_failure_limit_reached").send(player);
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onNpcRemove(NpcRemoveEvent event) {
      if (event.getSender() instanceof Player player) {
         if (!event.getNpc().getData().getCreator().equals(player.getUniqueId()) && !player.hasPermission("fancynpcs.admin")) {
            this.translator.translate("player_npcs_cannot_modify_npc").send(player);
            event.setCancelled(true);
         }
      } else {
         FancyNpcs.getInstance().getFancyLogger().warn("NpcRemoveEvent sender is not a Player!");
      }
   }

   @EventHandler
   public void onNpcModify(NpcModifyEvent event) {
      if (event.getModifier() instanceof Player player) {
         if (event.getNewValue() instanceof Location location) {
            boolean isOwner = checkNpcOwnership(player, location);
            if (!isOwner) {
               this.translator.translate("player_npcs_cannot_move_npc").send(player);
               event.setCancelled(true);
            }
         } else {
            FancyNpcs.getInstance().getFancyLogger().warn("NpcModifyEvent newValue is not a Location!");
         }
      } else {
         FancyNpcs.getInstance().getFancyLogger().warn("NpcModifyEvent modifier is not a Player!");
      }
   }
}
