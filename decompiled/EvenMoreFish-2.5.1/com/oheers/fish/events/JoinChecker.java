package com.oheers.fish.events;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;

public class JoinChecker implements Listener {
   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      this.checkCompetitionJoin(event.getPlayer());
      this.checkRewardsJoin(event.getPlayer());
   }

   private void checkCompetitionJoin(@NonNull Player player) {
      Competition activeComp = CompetitionManager.getInstance().getActiveCompetition();
      if (activeComp != null) {
         activeComp.getStatusBar().addPlayer(player);
         EMFMessage message = activeComp.format(ConfigMessage.COMPETITION_JOIN);
         Scheduling.getInstance().runTaskLater(() -> message.send(player), 60L);
      }
   }

   public void checkRewardsJoin(@NonNull Player player) {
      Scheduling.getInstance().runTaskLater(() -> Reward.checkCache(player.getUniqueId()), 60L);
   }

   @EventHandler
   public void onLeave(PlayerQuitEvent event) {
      this.checkCompetitionLeave(event.getPlayer());
   }

   private void checkCompetitionLeave(@NonNull Player player) {
      Competition activeComp = CompetitionManager.getInstance().getActiveCompetition();
      if (activeComp != null) {
         activeComp.getStatusBar().removePlayer(player);
      }

      if (DatabaseUtil.isDatabaseOnline()) {
         EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager().flush();
         EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager().flush();
      }
   }
}
