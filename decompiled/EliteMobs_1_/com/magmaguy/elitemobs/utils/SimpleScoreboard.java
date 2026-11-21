package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.ScoreboardUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

public class SimpleScoreboard {
   private static final String SIDEBAR_OBJECTIVE = "em_quest_sb";
   private static final Map<UUID, Scoreboard> previousScoreboards = new ConcurrentHashMap<>();
   private static final Set<Scoreboard> managedScoreboards = Collections.newSetFromMap(new WeakHashMap<>());
   private static final Map<UUID, Scoreboard> ownedScoreboards = new HashMap<>();
   private static final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();

   public static Scoreboard lazyScoreboard(Player player, String displayName, List<String> scoreboardContents) {
      cancelExpiry(player);
      Scoreboard scoreboard = createManagedScoreboard(player);
      Objective objective = ScoreboardUtil.registerSidebarObjective(MetadataHandler.PLUGIN, scoreboard, "em_quest_sb", displayName);
      ScoreboardUtil.setSidebarLines(objective, scoreboardContents);
      player.setScoreboard(scoreboard);
      ownedScoreboards.put(player.getUniqueId(), scoreboard);
      return scoreboard;
   }

   public static Scoreboard updateScoreboard(Player player, String displayName, List<String> scoreboardContents) {
      cancelExpiry(player);
      Scoreboard scoreboard = player.getScoreboard();
      if (ownedScoreboards.get(player.getUniqueId()) != scoreboard) {
         return lazyScoreboard(player, displayName, scoreboardContents);
      } else {
         Objective existing = scoreboard.getObjective("em_quest_sb");
         Objective objective;
         if (existing == null) {
            objective = ScoreboardUtil.registerSidebarObjective(MetadataHandler.PLUGIN, scoreboard, "em_quest_sb", displayName);
         } else {
            if (!existing.getDisplayName().equals(displayName)) {
               existing.setDisplayName(displayName);
            }

            existing.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective = existing;
         }

         ScoreboardUtil.setSidebarLines(objective, scoreboardContents);
         return scoreboard;
      }
   }

   public static boolean hasManagedSidebar(Player player) {
      if (player != null && ownedScoreboards.get(player.getUniqueId()) == player.getScoreboard()) {
         Objective objective = player.getScoreboard().getObjective("em_quest_sb");
         return objective != null && objective.equals(player.getScoreboard().getObjective(DisplaySlot.SIDEBAR));
      } else {
         return false;
      }
   }

   public static Scoreboard temporaryScoreboard(final Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
      Scoreboard scoreboard = updateScoreboard(player, displayName, scoreboardContents);
      BukkitTask task = (new BukkitRunnable() {
         public void run() {
            SimpleScoreboard.clearScoreboard(player);
         }
      }).runTaskLater(MetadataHandler.PLUGIN, ticksTimeout);
      expiryTasks.put(player.getUniqueId(), task);
      return scoreboard;
   }

   public static Scoreboard blankScoreboard(Player player) {
      cancelExpiry(player);
      Scoreboard scoreboard = createManagedScoreboard(player);
      player.setScoreboard(scoreboard);
      ownedScoreboards.put(player.getUniqueId(), scoreboard);
      return scoreboard;
   }

   public static void clearScoreboard(Player player) {
      cancelExpiry(player);
      Scoreboard owned = ownedScoreboards.remove(player.getUniqueId());
      Scoreboard previousScoreboard = previousScoreboards.remove(player.getUniqueId());
      if (player.isOnline() && owned == player.getScoreboard()) {
         if (previousScoreboard != null) {
            player.setScoreboard(previousScoreboard);
         } else {
            if (isManagedScoreboard(player.getScoreboard())) {
               player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
         }
      }
   }

   private static Scoreboard createManagedScoreboard(Player player) {
      rememberPreviousScoreboard(player);
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
      copyTeams(getSourceScoreboard(player), scoreboard);
      managedScoreboards.add(scoreboard);
      return scoreboard;
   }

   private static void rememberPreviousScoreboard(Player player) {
      Scoreboard scoreboard = player.getScoreboard();
      if (ownedScoreboards.get(player.getUniqueId()) != scoreboard) {
         previousScoreboards.put(player.getUniqueId(), scoreboard);
      }
   }

   private static Scoreboard getSourceScoreboard(Player player) {
      Scoreboard previousScoreboard = previousScoreboards.get(player.getUniqueId());
      return previousScoreboard == null ? player.getScoreboard() : previousScoreboard;
   }

   private static boolean isManagedScoreboard(Scoreboard scoreboard) {
      return scoreboard != null && managedScoreboards.contains(scoreboard);
   }

   private static void cancelExpiry(Player player) {
      BukkitTask task = expiryTasks.remove(player.getUniqueId());
      if (task != null) {
         task.cancel();
      }
   }

   public static void shutdown() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         clearScoreboard(player);
      }

      expiryTasks.values().forEach(BukkitTask::cancel);
      expiryTasks.clear();
      ownedScoreboards.clear();
      previousScoreboards.clear();
      managedScoreboards.clear();
   }

   private static void copyTeams(Scoreboard sourceScoreboard, Scoreboard targetScoreboard) {
      if (sourceScoreboard != null && targetScoreboard != null) {
         for (Team sourceTeam : sourceScoreboard.getTeams()) {
            Team targetTeam = targetScoreboard.getTeam(sourceTeam.getName());
            if (targetTeam == null) {
               targetTeam = targetScoreboard.registerNewTeam(sourceTeam.getName());
            }

            copyTeamSettings(sourceTeam, targetTeam);

            for (String entry : sourceTeam.getEntries()) {
               targetTeam.addEntry(entry);
            }
         }
      }
   }

   private static void copyTeamSettings(Team sourceTeam, Team targetTeam) {
      targetTeam.setDisplayName(sourceTeam.getDisplayName());
      targetTeam.setPrefix(sourceTeam.getPrefix());
      targetTeam.setSuffix(sourceTeam.getSuffix());
      ChatColor teamColor = sourceTeam.getColor();
      if (teamColor != null && teamColor.isColor()) {
         targetTeam.setColor(teamColor);
      }

      targetTeam.setAllowFriendlyFire(sourceTeam.allowFriendlyFire());
      targetTeam.setCanSeeFriendlyInvisibles(sourceTeam.canSeeFriendlyInvisibles());
      copyTeamOption(sourceTeam, targetTeam, Option.NAME_TAG_VISIBILITY);
      copyTeamOption(sourceTeam, targetTeam, Option.DEATH_MESSAGE_VISIBILITY);
      copyTeamOption(sourceTeam, targetTeam, Option.COLLISION_RULE);
   }

   private static void copyTeamOption(Team sourceTeam, Team targetTeam, Option option) {
      targetTeam.setOption(option, sourceTeam.getOption(option));
   }

   public static class Events implements Listener {
      @EventHandler
      public void onQuit(PlayerQuitEvent event) {
         SimpleScoreboard.clearScoreboard(event.getPlayer());
      }
   }
}
