package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerStatsTracker implements Listener {
   public static void registerPlayerDeath(Player player) {
      PlayerData.incrementDeaths(player.getUniqueId());
      PlayerData.decrementScore(player.getUniqueId());
   }

   @EventHandler
   public void onEliteDeath(EliteMobDeathEvent event) {
      if (!event.getEliteEntity().isTriggeredAntiExploit()) {
         if (!event.getEliteEntity().getDamagers().isEmpty()) {
            for (Player player : event.getEliteEntity().getDamagers().keySet()) {
               if (!player.hasMetadata("NPC") && PlayerData.isInMemory(player.getUniqueId())) {
                  PlayerData.incrementKills(player.getUniqueId());
                  PlayerData.setHighestLevelKilled(player.getUniqueId(), event.getEliteEntity().getLevel());
                  PlayerData.incrementScore(player.getUniqueId(), event.getEliteEntity().getLevel());
               }
            }
         }
      }
   }

   @EventHandler
   public void onDungeonComplete(DungeonCompleteEvent event) {
      Set<UUID> completingParticipantIds = event.getDungeonInstance()
         .getParticipants()
         .stream()
         .<UUID>map(OfflinePlayer::getUniqueId)
         .collect(Collectors.toSet());
      completingParticipantIds.retainAll(event.getDungeonInstance().getStartingParticipantIds());

      for (UUID participantId : completingParticipantIds) {
         if (PlayerData.isDataLoaded(participantId)) {
            PlayerData.incrementDungeonsCompleted(participantId);
         }
      }
   }
}
