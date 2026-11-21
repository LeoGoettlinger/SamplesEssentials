package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class KillObjective extends Objective {
   protected KillObjective(int targetAmount, String objectiveName) {
      super(targetAmount, objectiveName);
   }

   public abstract void checkProgress(EliteMobDeathEvent var1, QuestObjectives var2);

   public static class KillObjectiveEvents implements Listener {
      @EventHandler
      public void onEliteDeath(EliteMobDeathEvent event) {
         for (Player player : PartyManager.expandSharedCreditParticipants(event.getEliteEntity().getDamagers().keySet(), event.getEliteEntity())) {
            if (!player.hasMetadata("NPC") && PlayerData.isInMemory(player.getUniqueId())) {
               for (Quest quest : new ArrayList<>(PlayerData.getQuests(player.getUniqueId()))) {
                  if (quest != null) {
                     for (Objective objective : quest.getQuestObjectives().getObjectives()) {
                        if (objective instanceof KillObjective) {
                           ((KillObjective)objective).checkProgress(event, quest.getQuestObjectives());
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
