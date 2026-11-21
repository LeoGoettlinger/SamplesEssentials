package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.CombatHealthFormatter;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class PartySidebar {
   private static final int MAX_LINES = 15;
   private static final long REFRESH_PERIOD_TICKS = 20L;
   private static final Map<UUID, PartySidebar.TemporaryQuestView> temporaryQuestViews = new HashMap<>();
   private static final Map<UUID, PartySidebar.SidebarSnapshot> lastRenderedSidebars = new HashMap<>();
   private static BukkitTask refreshTask;
   private static boolean showInviteAction = true;
   private static int secondsSinceActionRotation = 0;

   private PartySidebar() {
   }

   public static boolean isEnabled() {
      return PartyConfig.isEnabled() && PartyConfig.isSidebarEnabled();
   }

   static void initialize() {
      if (refreshTask != null) {
         refreshTask.cancel();
      }

      secondsSinceActionRotation = 0;
      refreshTask = (new BukkitRunnable() {
         public void run() {
            PartyManager.cleanupExpiredInvites();
            PartySidebar.cleanupExpiredViews();
            if (PartySidebar.isEnabled()) {
               if (++PartySidebar.secondsSinceActionRotation >= PartyConfig.getSidebarRotationSeconds()) {
                  PartySidebar.showInviteAction = !PartySidebar.showInviteAction;
                  PartySidebar.secondsSinceActionRotation = 0;
               }

               PartyManager.getParties().values().forEach(party -> party.getMembers().forEach(playerId -> {
                  Player player = Bukkit.getPlayer(playerId);
                  if (player != null) {
                     PartySidebar.refresh(player);
                  }
               }));
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
   }

   static void shutdown() {
      if (refreshTask != null) {
         refreshTask.cancel();
      }

      refreshTask = null;
      temporaryQuestViews.clear();
      lastRenderedSidebars.clear();
      showInviteAction = true;
      secondsSinceActionRotation = 0;
   }

   public static void showTemporaryQuest(final Player player, String questName, List<String> questLines, int ticksTimeout) {
      if (isEnabled() && PartyManager.isInParty(player.getUniqueId())) {
         final UUID token = UUID.randomUUID();
         temporaryQuestViews.put(
            player.getUniqueId(), new PartySidebar.TemporaryQuestView(token, questName, List.copyOf(questLines), System.nanoTime() + ticksTimeout * 50000000L)
         );
         refresh(player);
         (new BukkitRunnable() {
            public void run() {
               PartySidebar.TemporaryQuestView current = PartySidebar.temporaryQuestViews.get(player.getUniqueId());
               if (current != null && current.token().equals(token)) {
                  PartySidebar.temporaryQuestViews.remove(player.getUniqueId());
                  if (player.isOnline() && PartyManager.isInParty(player.getUniqueId())) {
                     PartySidebar.refresh(player);
                  }
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, ticksTimeout);
      }
   }

   public static void refresh(Player player) {
      try {
         refreshInternal(player);
      } catch (RuntimeException var2) {
         Logger.warn("Failed to refresh the party sidebar for " + player.getName() + ".");
         var2.printStackTrace();
      }
   }

   private static void refreshInternal(Player player) {
      if (isEnabled()) {
         Party party = PartyManager.getParty(player.getUniqueId());
         if (party != null && player.isOnline()) {
            if (!QuestsConfig.isHideQuestScoreboardDuringQuestDialogue() || !QuestDialogueBossBarManager.hasActiveSession(player)) {
               DungeonInstance dungeonInstance = getDungeonInstance(player);
               List<String> lines = new ArrayList<>();

               for (UUID memberId : party.getMembersInDisplayOrder()) {
                  Player member = Bukkit.getPlayer(memberId);
                  String template = memberId.equals(party.getLeader()) ? PartyConfig.getSidebarLeaderLine() : PartyConfig.getSidebarMemberLine();
                  lines.add(renderMemberLine(template, member, dungeonInstance));
               }

               PartySidebar.QuestView questView = currentQuestView(player);
               int reservedActionLines = 1;
               if (questView != null && lines.size() + reservedActionLines < 15) {
                  lines.add(color(PartyConfig.getSidebarQuestLine().replace("$quest", questView.name())));
                  int availableQuestLines = 15 - lines.size() - reservedActionLines;

                  for (String questLine : questView.lines()) {
                     if (availableQuestLines-- <= 0) {
                        break;
                     }

                     lines.add(questLine);
                  }
               }

               lines.add(color(showInviteAction ? PartyConfig.getSidebarInviteAction() : PartyConfig.getSidebarLeaveAction()));
               Collections.reverse(lines);
               String title = color(PartyConfig.getSidebarTitle());
               PartySidebar.SidebarSnapshot snapshot = new PartySidebar.SidebarSnapshot(title, List.copyOf(lines));
               if (!snapshot.equals(lastRenderedSidebars.get(player.getUniqueId())) || !SimpleScoreboard.hasManagedSidebar(player)) {
                  SimpleScoreboard.updateScoreboard(player, title, lines);
                  lastRenderedSidebars.put(player.getUniqueId(), snapshot);
               }
            }
         }
      }
   }

   private static String renderMemberLine(String template, Player member, DungeonInstance dungeonInstance) {
      boolean downed = member != null && dungeonInstance != null && dungeonInstance.isSpectator(member) && dungeonInstance.getRemainingLives(member) != null;
      String health = renderHealth(member, downed);
      if (downed) {
         health = health + PartyConfig.getSidebarDownedDisplay();
      }

      String lives = renderLives(member, dungeonInstance);
      boolean hasHealthPlaceholder = template.contains("$health");
      boolean hasLivesPlaceholder = template.contains("$lives");
      String rendered = template.replace("$player", member == null ? PartyConfig.getUnknownPlayerName() : member.getName())
         .replace("$health", health)
         .replace("$lives", lives);
      if (!hasHealthPlaceholder) {
         rendered = rendered + health;
      }

      if (!hasLivesPlaceholder) {
         rendered = rendered + lives;
      }

      return color(rendered);
   }

   private static String renderHealth(Player member, boolean downed) {
      if (member != null && member.isOnline()) {
         AttributeInstance attribute = member.getAttribute(Attribute.MAX_HEALTH);
         double maximum = Math.max(1.0, attribute == null ? member.getHealth() : attribute.getValue());
         double current = !downed && !member.isDead() ? Math.max(0.0, Math.min(maximum, member.getHealth())) : 0.0;
         double fraction = current / maximum;
         int from = fraction >= 0.5 ? 16711680 : 524288;
         int to = fraction >= 0.5 ? 5635925 : 16711680;
         double blend = fraction >= 0.5 ? (fraction - 0.5) * 2.0 : fraction * 2.0;
         int red = interpolateChannel(from >> 16, to >> 16, blend);
         int green = interpolateChannel(from >> 8, to >> 8, blend);
         int blue = interpolateChannel(from, to, blend);
         String tint = ChatColor.of(new Color(red, green, blue)).toString();
         return " " + tint + CombatHealthFormatter.format(current) + "&7/&f" + CombatHealthFormatter.format(maximum);
      } else {
         return " &8-/-";
      }
   }

   private static int interpolateChannel(int from, int to, double blend) {
      return (int)Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * blend);
   }

   private static String renderLives(Player member, DungeonInstance dungeonInstance) {
      if (member != null && dungeonInstance != null) {
         Integer lives = dungeonInstance.getRemainingLives(member);
         return lives == null ? "" : PartyConfig.getSidebarLivesDisplay().replace("$lives", String.valueOf(Math.max(0, lives)));
      } else {
         return "";
      }
   }

   private static DungeonInstance getDungeonInstance(Player viewer) {
      return PlayerData.getMatchInstance(viewer) instanceof DungeonInstance dungeonInstance ? dungeonInstance : null;
   }

   static void clearPlayer(Player player) {
      temporaryQuestViews.remove(player.getUniqueId());
      if (lastRenderedSidebars.remove(player.getUniqueId()) != null) {
         if (!QuestsConfig.isHideQuestScoreboardDuringQuestDialogue() || !QuestDialogueBossBarManager.hasActiveSession(player)) {
            SimpleScoreboard.clearScoreboard(player);
            QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
            if (tracking != null) {
               tracking.refreshScoreboard();
            }
         }
      }
   }

   private static PartySidebar.QuestView currentQuestView(Player player) {
      if (!QuestsConfig.isUseQuestScoreboards()) {
         return null;
      } else {
         PartySidebar.TemporaryQuestView temporary = temporaryQuestViews.get(player.getUniqueId());
         if (temporary != null) {
            if (temporary.expiresAtNanos() > System.nanoTime()) {
               return new PartySidebar.QuestView(temporary.name(), temporary.lines());
            }

            temporaryQuestViews.remove(player.getUniqueId());
         }

         QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
         return tracking == null
            ? null
            : new PartySidebar.QuestView(tracking.getQuest().getQuestName(), tracking.getQuest().getQuestObjectives().getScoreboardObjectiveText());
      }
   }

   private static void cleanupExpiredViews() {
      long now = System.nanoTime();
      temporaryQuestViews.entrySet().removeIf(entry -> entry.getValue().expiresAtNanos() <= now);
   }

   private static String color(String value) {
      return ChatColorConverter.convert(value == null ? "" : value);
   }

   private record QuestView(String name, List<String> lines) {
   }

   private record SidebarSnapshot(String title, List<String> lines) {
   }

   private record TemporaryQuestView(UUID token, String name, List<String> lines, long expiresAtNanos) {
   }
}
