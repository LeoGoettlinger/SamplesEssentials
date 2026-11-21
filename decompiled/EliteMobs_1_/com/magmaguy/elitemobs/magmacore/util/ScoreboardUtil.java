package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class ScoreboardUtil {
   private static final String DEFAULT_SIDEBAR_OBJECTIVE = "mcore_sb";
   private static final int MAX_SIDEBAR_LINES = 15;
   private static final int SCOREBOARD_LABEL_LIMIT = 32767;
   private static final Map<Scoreboard, Map<String, List<ScoreboardUtil.SidebarRow>>> sidebarRows = new WeakHashMap<>();
   private static final Set<String> shutdownListenerRegistrations = new HashSet<>();
   private static boolean adapterInitializationAttempted = false;

   private ScoreboardUtil() {
   }

   public static Scoreboard createSidebarScoreboard(Plugin plugin, String displayName, List<String> scoreboardContents) {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
      Objective objective = registerSidebarObjective(plugin, scoreboard, "mcore_sb", displayName);
      setSidebarLines(objective, scoreboardContents);
      return scoreboard;
   }

   public static Scoreboard lazyScoreboard(Plugin plugin, Player player, String displayName, List<String> scoreboardContents) {
      Scoreboard scoreboard = createSidebarScoreboard(plugin, displayName, scoreboardContents);
      player.setScoreboard(scoreboard);
      return scoreboard;
   }

   public static Scoreboard temporaryScoreboard(Plugin plugin, final Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
      final Scoreboard scoreboard = lazyScoreboard(plugin, player, displayName, scoreboardContents);
      (new BukkitRunnable() {
         public void run() {
            if (player.getScoreboard().equals(scoreboard)) {
               ScoreboardUtil.clearScoreboard(player);
            }
         }
      }).runTaskLater(plugin, ticksTimeout);
      return scoreboard;
   }

   public static void clearScoreboard(Player player) {
      player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
   }

   public static Objective registerSidebarObjective(Plugin plugin, Scoreboard scoreboard, String objectiveName, String displayName) {
      Objective objective = registerSidebarObjective(scoreboard, objectiveName, displayName);
      hideScoreboardNumbers(plugin, objective);
      return objective;
   }

   public static Objective registerSidebarObjective(Scoreboard scoreboard, String objectiveName, String displayName) {
      Objective objective = scoreboard.registerNewObjective(objectiveName, "dummy", displayName == null ? "" : displayName);
      objective.setDisplaySlot(DisplaySlot.SIDEBAR);
      hideScoreboardNumbers(objective);
      return objective;
   }

   public static boolean hideScoreboardNumbers(Plugin plugin, Objective objective) {
      if (objective == null) {
         return false;
      } else {
         ensureAdapter(plugin);
         return hideScoreboardNumbers(objective);
      }
   }

   public static boolean hideScoreboardNumbers(Objective objective) {
      return objective != null && NMSManager.isEnabled() && NMSManager.getAdapter() != null ? NMSManager.getAdapter().hideScoreboardNumbers(objective) : false;
   }

   public static void setSidebarLines(Objective objective, List<String> scoreboardContents) {
      Scoreboard scoreboard = objective.getScoreboard();
      if (scoreboard != null) {
         List<ScoreboardUtil.SidebarRow> rows = sidebarRows.computeIfAbsent(scoreboard, ignored -> new HashMap<>())
            .computeIfAbsent(objective.getName(), ignored -> new ArrayList<>());
         int lineCount = scoreboardContents == null ? 0 : Math.min(scoreboardContents.size(), 15);

         while (rows.size() > lineCount) {
            ScoreboardUtil.SidebarRow removed = rows.remove(rows.size() - 1);
            Map<Objective, Integer> otherScores = new HashMap<>();

            for (Objective other : scoreboard.getObjectives()) {
               if (!other.equals(objective)) {
                  Score score = other.getScore(removed.entry);
                  if (score.isScoreSet()) {
                     otherScores.put(other, score.getScore());
                  }
               }
            }

            scoreboard.resetScores(removed.entry);
            otherScores.forEach((otherx, value) -> otherx.getScore(removed.entry).setScore(value));
            Team team = scoreboard.getTeam(removed.teamName);
            if (team != null) {
               team.unregister();
            }
         }

         for (int i = 0; i < lineCount; i++) {
            if (rows.size() == i) {
               String teamName;
               do {
                  teamName = "mc" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
               } while (scoreboard.getTeam(teamName) != null);

               StringBuilder entry = new StringBuilder();

               for (char digit : teamName.substring(2).toCharArray()) {
                  entry.append('§').append(digit);
               }

               Team team = scoreboard.registerNewTeam(teamName);
               team.addEntry(entry.toString());
               rows.add(new ScoreboardUtil.SidebarRow(teamName, entry.toString()));
            }

            ScoreboardUtil.SidebarRow row = rows.get(i);
            String label = trimScoreboardEntry(scoreboardContents.get(i));
            Team team = scoreboard.getTeam(row.teamName);
            if (team == null) {
               team = scoreboard.registerNewTeam(row.teamName);
               team.addEntry(row.entry);
            }

            if (!label.equals(team.getPrefix())) {
               team.setPrefix(label);
            }

            Score score = objective.getScore(row.entry);
            if (!score.isScoreSet() || score.getScore() != i) {
               score.setScore(i);
            }
         }
      }
   }

   private static String trimScoreboardEntry(String entry) {
      if (entry == null) {
         return "";
      } else {
         return entry.length() <= 32767 ? entry : entry.substring(0, 32766);
      }
   }

   private static synchronized void ensureAdapter(Plugin plugin) {
      if (plugin != null && plugin.isEnabled()) {
         registerShutdownListener(plugin);
         if (!NMSManager.isEnabled() || NMSManager.getAdapter() == null) {
            if (!adapterInitializationAttempted) {
               adapterInitializationAttempted = true;
               NMSManager.initializeAdapter(plugin);
            }
         }
      }
   }

   private static synchronized void registerShutdownListener(final Plugin plugin) {
      if (plugin != null && !shutdownListenerRegistrations.contains(plugin.getName())) {
         shutdownListenerRegistrations.add(plugin.getName());
         Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPluginDisable(PluginDisableEvent event) {
               if (event.getPlugin().getName().equals(plugin.getName())) {
                  NMSManager.shutdown();
                  synchronized (ScoreboardUtil.class) {
                     ScoreboardUtil.shutdownListenerRegistrations.remove(plugin.getName());
                     ScoreboardUtil.adapterInitializationAttempted = false;
                  }
               }
            }
         }, plugin);
      }
   }

   private record SidebarRow(String teamName, String entry) {
   }
}
