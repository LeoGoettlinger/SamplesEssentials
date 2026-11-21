package de.oliver.fancynpcs.tracker;

import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VisibilityTracker implements Runnable {
   private Set<UUID> joinDelayPlayers = new HashSet<>();

   @Override
   public void run() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (!this.joinDelayPlayers.contains(player.getUniqueId())) {
            for (Npc npc : FancyNpcs.getInstance().getNpcManagerImpl().getAllNpcs()) {
               npc.checkAndUpdateVisibility(player);
            }
         }
      }
   }

   public void addJoinDelayPlayer(UUID player) {
      this.joinDelayPlayers.add(player);
   }

   public void removeJoinDelayPlayer(UUID player) {
      this.joinDelayPlayers.remove(player);
   }
}
