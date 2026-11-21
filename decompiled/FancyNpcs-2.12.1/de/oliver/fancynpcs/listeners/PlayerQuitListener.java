package de.oliver.fancynpcs.listeners;

import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcStopLookingEvent;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      for (Npc npc : FancyNpcs.getInstance().getNpcManagerImpl().getAllNpcs()) {
         npc.getIsVisibleForPlayer().put(event.getPlayer().getUniqueId(), false);
         npc.getIsLookingAtPlayer().put(event.getPlayer().getUniqueId(), false);
         npc.getIsTeamCreated().put(event.getPlayer().getUniqueId(), false);
         Bukkit.getPluginManager().callEvent(new NpcStopLookingEvent(npc, event.getPlayer()));
      }

      UUID uuid = event.getPlayer().getUniqueId();

      for (Npc npc : FancyNpcs.getInstance().getNpcManagerImpl().getAllNpcs()) {
         npc.getIsVisibleForPlayer().remove(uuid);
         npc.getIsLookingAtPlayer().remove(uuid);
         npc.getIsTeamCreated().remove(uuid);
         new NpcStopLookingEvent(npc, event.getPlayer()).callEvent();
      }
   }
}
