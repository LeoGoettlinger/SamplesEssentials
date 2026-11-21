package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.ArenaCompleteEvent;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import java.util.ArrayList;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArenaObjective extends Objective {
   public String arenaFilename;

   protected ArenaObjective(String objectiveName, String arenaFilename) {
      super(1, objectiveName);
      this.arenaFilename = arenaFilename;
   }

   @Generated
   public String getArenaFilename() {
      return this.arenaFilename;
   }

   public static class ArenaObjectiveEvents implements Listener {
      @EventHandler
      public void onArenaComplete(ArenaCompleteEvent arenaCompleteEvent) {
         for (Player player : arenaCompleteEvent.getArenaInstance().getPlayers()) {
            for (Quest quest : new ArrayList<>(PlayerData.getQuests(player.getUniqueId()))) {
               if (quest instanceof CustomQuest) {
                  for (Objective objective : quest.getQuestObjectives().getObjectives()) {
                     if (objective instanceof ArenaObjective arenaObjective
                        && arenaObjective.arenaFilename.equals(arenaCompleteEvent.getArenaInstance().getCustomArenasConfigFields().getFilename())) {
                        objective.progressObjective(quest.getQuestObjectives());
                     }
                  }
               }
            }
         }
      }
   }
}
