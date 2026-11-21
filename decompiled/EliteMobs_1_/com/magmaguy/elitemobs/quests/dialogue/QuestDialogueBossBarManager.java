package com.magmaguy.elitemobs.quests.dialogue;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.parties.PartySidebar;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class QuestDialogueBossBarManager {
   private static final int BOX_BAR_INDEX = 0;
   private static final int NAME_BAR_INDEX = 1;
   private static final int FIRST_LINE_BAR_INDEX = 2;
   private static final int BODY_LINES = 3;
   private static final int PROMPT_BAR_INDEX = 5;
   private static final int TOTAL_BARS = 6;
   private static final int DIALOGUE_SLOWNESS_AMPLIFIER = 2;
   private static final int DIALOGUE_SLOWNESS_DURATION_TICKS = 600;
   private static final Map<UUID, QuestDialogueBossBarManager.DialogueSession> activeSessions = new ConcurrentHashMap<>();
   private static final Map<String, Long> recentlyShownTurnInDialogs = new ConcurrentHashMap<>();
   private static BukkitTask keyedBarTask;

   private static void suppressExternalBossBars() {
      Iterator<KeyedBossBar> iterator = Bukkit.getBossBars();

      while (iterator.hasNext()) {
         KeyedBossBar bar = iterator.next();

         for (Player viewer : bar.getPlayers()) {
            QuestDialogueBossBarManager.DialogueSession session = activeSessions.get(viewer.getUniqueId());
            if (session != null && !session.closed && session.player == viewer) {
               session.hiddenKeyedBossBars.add(bar);
               bar.removePlayer(viewer);
            }
         }
      }
   }

   private QuestDialogueBossBarManager() {
   }

   public static void shutdown() {
      for (QuestDialogueBossBarManager.DialogueSession session : new ArrayList<>(activeSessions.values())) {
         session.close(false);
      }

      activeSessions.clear();
      recentlyShownTurnInDialogs.clear();
      if (keyedBarTask != null) {
         keyedBarTask.cancel();
      }

      keyedBarTask = null;
   }

   public static boolean showQuestMenuIntro(List<? extends Quest> quests, Player player, NPCEntity npcEntity, Runnable onComplete) {
      if (!canShowQuestDialogueBossBars(player)) {
         return false;
      } else if (quests != null && !quests.isEmpty()) {
         List<String> dialogueLines = new ArrayList<>();

         for (Quest quest : quests) {
            dialogueLines.addAll(getQuestDialogueLines(quest, player, npcEntity));
         }

         if (dialogueLines.isEmpty()) {
            return false;
         } else {
            startDialogue(player, getSpeakerName(npcEntity), dialogueLines, onComplete);
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean showRawDialogue(Player player, String speakerName, List<String> dialogueLines, Runnable onComplete) {
      if (!canShowQuestDialogueBossBars(player)) {
         return false;
      } else if (dialogueLines != null && !dialogueLines.isEmpty()) {
         startDialogue(player, speakerName, dialogueLines, onComplete);
         return true;
      } else {
         return false;
      }
   }

   private static boolean canShowQuestDialogueBossBars(Player player) {
      return QuestsConfig.isUseQuestDialogueBossBars() && player != null && !BedrockChecker.isBedrock(player);
   }

   public static boolean consumeRecentlyShownQuestCompleteDialog(Player player, Quest quest) {
      Long shownAt = recentlyShownTurnInDialogs.remove(dialogueKey(player, quest));
      return shownAt != null && System.currentTimeMillis() - shownAt < 60000L;
   }

   private static void startDialogue(Player player, String speakerName, List<String> dialogueLines, Runnable onComplete) {
      QuestDialogueBossBarManager.DialogueSession existingSession = activeSessions.get(player.getUniqueId());
      if (existingSession != null) {
         existingSession.replace(speakerName, dialogueLines, onComplete);
      } else {
         QuestDialogueBossBarManager.DialogueSession session = new QuestDialogueBossBarManager.DialogueSession(player, speakerName, dialogueLines, onComplete);
         activeSessions.put(player.getUniqueId(), session);
         session.start();
      }
   }

   public static void close(Player player, boolean runCallback) {
      QuestDialogueBossBarManager.DialogueSession session = activeSessions.remove(player.getUniqueId());
      if (session != null) {
         session.close(runCallback);
      }
   }

   public static boolean hasActiveSession(Player player) {
      return player != null && activeSessions.containsKey(player.getUniqueId());
   }

   static List<String> getQuestDialogueLines(Quest quest, Player player, NPCEntity npcEntity) {
      List<String> lines = new ArrayList<>();
      if (quest instanceof CustomQuest customQuest) {
         if (customQuest.getCustomQuestsConfigFields() == null) {
            return lines;
         }

         if (!quest.isAccepted()) {
            lines.addAll(
               firstNonEmpty(customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog(), customQuest.getCustomQuestsConfigFields().getQuestLore())
            );
         } else if (quest.getQuestObjectives().isOver()
            && npcEntity != null
            && Objects.equals(quest.getQuestTaker(), npcEntity.getNPCsConfigFields().getFilename())) {
            List<String> questCompleteDialog = customQuest.getCustomQuestsConfigFields().getQuestCompleteDialog();
            if (questCompleteDialog != null && !questCompleteDialog.isEmpty()) {
               lines.addAll(questCompleteDialog);
               recentlyShownTurnInDialogs.put(dialogueKey(player, quest), System.currentTimeMillis());
            }
         }
      } else if (quest instanceof DynamicQuest && !quest.isAccepted()) {
         for (TextComponent textComponent : QuestMenu.generateBody(quest)) {
            lines.add(textComponent.toPlainText());
         }
      }

      return parsePlaceholders(player, lines);
   }

   private static String dialogueKey(Player player, Quest quest) {
      return player.getUniqueId() + ":" + quest.getQuestID();
   }

   private static List<String> firstNonEmpty(List<String> first, List<String> fallback) {
      if (first != null && !first.isEmpty()) {
         return first;
      } else {
         return fallback != null ? fallback : List.of();
      }
   }

   private static List<String> parsePlaceholders(Player player, List<String> lines) {
      List<String> parsedLines = new ArrayList<>();
      Location location = player.getLocation();

      for (String line : lines) {
         if (line != null) {
            parsedLines.add(
               line.replace("$player", player.getName())
                  .replace("$getX", location.getX() + "")
                  .replace("$getY", location.getY() + "")
                  .replace("$getZ", location.getZ() + "")
            );
         }
      }

      return parsedLines;
   }

   private static String getSpeakerName(NPCEntity npcEntity) {
      return npcEntity != null && npcEntity.getNPCsConfigFields().getName() != null ? npcEntity.getNPCsConfigFields().getName() : "&eEliteMobs";
   }

   private static List<String> wrapLines(List<String> rawLines) {
      List<String> wrappedLines = new ArrayList<>();

      for (String rawLine : rawLines) {
         String line = ChatColorConverter.convert(rawLine);
         String strippedLine = ChatColor.stripColor(line);
         if (strippedLine != null && !strippedLine.isBlank()) {
            wrappedLines.addAll(wrapLine(line, Math.max(1, QuestsConfig.getQuestDialogueCharactersPerLine())));
         } else {
            wrappedLines.add("");
         }
      }

      return wrappedLines;
   }

   private static List<String> wrapLine(String line, int limit) {
      List<String> lines = new ArrayList<>();
      StringBuilder current = new StringBuilder();

      for (String word : line.split(" ")) {
         boolean currentHasVisibleContent = visibleLength(current.toString()) > 0;
         String wordWithSpace = currentHasVisibleContent ? " " + word : word;
         if (currentHasVisibleContent && visibleLength("" + current + wordWithSpace) > limit) {
            lines.add(current.toString());
            current = new StringBuilder(activeFormatCodes(current.toString()));
            if (visibleLength("" + current + word) > limit) {
               lines.addAll(splitVisible(current + word, limit));
               current = new StringBuilder();
            } else {
               current.append(word);
            }
         } else if (visibleLength("" + current + word) > limit) {
            lines.addAll(splitVisible(current + word, limit));
            current = new StringBuilder();
         } else {
            current.append(wordWithSpace);
         }
      }

      if (!current.isEmpty()) {
         lines.add(current.toString());
      }

      return lines;
   }

   private static List<String> splitVisible(String line, int limit) {
      List<String> lines = new ArrayList<>();
      String remaining = line;

      while (visibleLength(remaining) > limit) {
         String part = substringVisible(remaining, limit);
         lines.add(part);
         remaining = activeFormatCodes(part) + remaining.substring(part.length());
      }

      if (!remaining.isEmpty()) {
         lines.add(remaining);
      }

      return lines;
   }

   private static List<QuestDialogueBossBarManager.Page> paginate(List<String> wrappedLines) {
      List<QuestDialogueBossBarManager.Page> pages = new ArrayList<>();

      for (int i = 0; i < wrappedLines.size(); i += 3) {
         List<String> pageLines = new ArrayList<>();

         for (int j = 0; j < 3; j++) {
            int index = i + j;
            pageLines.add(index < wrappedLines.size() ? wrappedLines.get(index) : "");
         }

         pages.add(new QuestDialogueBossBarManager.Page(pageLines));
      }

      if (pages.isEmpty()) {
         pages.add(new QuestDialogueBossBarManager.Page(List.of("", "", "")));
      }

      return pages;
   }

   private static String formatName(String name) {
      return OffsetFontUtil.shift(
         forceStyle(ChatColorConverter.convert(QuestsConfig.getQuestDialogueNamePrefix() + name + QuestsConfig.getQuestDialogueNameSuffix()), ChatColor.BOLD),
         QuestsConfig.getQuestDialogueNameOffsetX()
      );
   }

   private static String formatLine(String line) {
      return OffsetFontUtil.shift(
         ChatColorConverter.convert(QuestsConfig.getQuestDialogueLinePrefix() + line + QuestsConfig.getQuestDialogueLineSuffix()),
         QuestsConfig.getQuestDialogueLineOffsetX()
      );
   }

   private static String formatPrompt() {
      return OffsetFontUtil.shift(
         ChatColorConverter.convert(
            QuestsConfig.getQuestDialogueLinePrefix() + QuestsConfig.getQuestDialoguePromptText() + QuestsConfig.getQuestDialogueLineSuffix()
         ),
         QuestsConfig.getQuestDialoguePromptOffsetX()
      );
   }

   private static int visibleLength(String string) {
      int length = 0;

      for (int i = 0; i < string.length(); i++) {
         if (string.charAt(i) == 167 && i + 1 < string.length()) {
            i++;
         } else {
            length++;
         }
      }

      return length;
   }

   private static String substringVisible(String string, int visibleCharacters) {
      if (visibleCharacters <= 0) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         int visibleCount = 0;

         for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (character == 167 && i + 1 < string.length()) {
               builder.append(character).append(string.charAt(i + 1));
               i++;
            } else {
               if (visibleCount >= visibleCharacters) {
                  break;
               }

               builder.append(character);
               visibleCount++;
            }
         }

         return builder.toString();
      }
   }

   private static String forceStyle(String string, ChatColor style) {
      if (string != null && !string.isEmpty()) {
         StringBuilder builder = new StringBuilder(style.toString());

         for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            builder.append(character);
            if (character == 167 && i + 1 < string.length()) {
               char code = string.charAt(++i);
               builder.append(code);
               if ((code == 'x' || code == 'X') && i + 12 < string.length()) {
                  builder.append(string, i + 1, i + 13).append(style);
                  i += 12;
               } else {
                  ChatColor chatColor = ChatColor.getByChar(code);
                  if (chatColor != null && (chatColor.isColor() || chatColor == ChatColor.RESET)) {
                     builder.append(style);
                  }
               }
            }
         }

         return builder.toString();
      } else {
         return "";
      }
   }

   private static String activeFormatCodes(String string) {
      String activeColor = "";
      List<ChatColor> activeFormats = new ArrayList<>();

      for (int i = 0; i < string.length(); i++) {
         if (string.charAt(i) == 167 && i + 1 < string.length()) {
            char code = string.charAt(++i);
            if (code != 'x' && code != 'X') {
               ChatColor chatColor = ChatColor.getByChar(code);
               if (chatColor != null) {
                  if (chatColor == ChatColor.RESET) {
                     activeColor = "";
                     activeFormats.clear();
                  } else if (chatColor.isColor()) {
                     activeColor = chatColor.toString();
                     activeFormats.clear();
                  } else if (!activeFormats.contains(chatColor)) {
                     activeFormats.add(chatColor);
                  }
               }
            } else {
               String hexColor = readHexColor(string, i - 1);
               if (!hexColor.isEmpty()) {
                  activeColor = hexColor;
                  activeFormats.clear();
                  i += 12;
               }
            }
         }
      }

      StringBuilder builder = new StringBuilder(activeColor);

      for (ChatColor activeFormat : activeFormats) {
         builder.append(activeFormat);
      }

      return builder.toString();
   }

   private static String readHexColor(String string, int colorCodeStart) {
      if (colorCodeStart + 13 >= string.length()) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder(14);

         for (int i = 0; i < 7; i++) {
            int sectionIndex = colorCodeStart + i * 2;
            if (string.charAt(sectionIndex) != 167) {
               return "";
            }

            builder.append('§').append(string.charAt(sectionIndex + 1));
         }

         return builder.toString();
      }
   }

   private static class DialogueSession {
      private final Player player;
      private String speakerName;
      private List<QuestDialogueBossBarManager.Page> pages;
      private Runnable onComplete;
      private final Set<KeyedBossBar> hiddenKeyedBossBars = new LinkedHashSet<>();
      private final BossBar[] bars = new BossBar[6];
      private final PotionEffect previousSlowness;
      private long movementLockTick;
      private boolean movementLockApplied;
      private BossBarOrderManager.Suspension bossBarSuspension;
      private BukkitTask task;
      private int pageIndex = 0;
      private int visibleCharacters = 0;
      private boolean closed = false;

      private DialogueSession(Player player, String speakerName, List<String> dialogueLines, Runnable onComplete) {
         this.player = player;
         this.speakerName = speakerName;
         this.pages = QuestDialogueBossBarManager.paginate(QuestDialogueBossBarManager.wrapLines(dialogueLines));
         this.onComplete = onComplete;
         this.previousSlowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
      }

      private void start() {
         try {
            this.bossBarSuspension = BossBarOrderManager.suspendPlayer(this.player);
            QuestDialogueBossBarManager.suppressExternalBossBars();
            if (QuestDialogueBossBarManager.keyedBarTask == null) {
               QuestDialogueBossBarManager.keyedBarTask = Bukkit.getScheduler()
                  .runTaskTimer(MetadataHandler.PLUGIN, QuestDialogueBossBarManager::suppressExternalBossBars, 1L, 1L);
            }

            this.suppressScoreboard();
            this.applyMovementLock();
            this.createBars();
            this.showCurrentPage();
            this.task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::tick, 1L, 1L);
         } catch (Error | RuntimeException var2) {
            this.close(false);
            throw var2;
         }
      }

      private void replace(String speakerName, List<String> dialogueLines, Runnable onComplete) {
         this.speakerName = speakerName;
         this.pages = QuestDialogueBossBarManager.paginate(QuestDialogueBossBarManager.wrapLines(dialogueLines));
         this.onComplete = onComplete;
         this.pageIndex = 0;
         QuestDialogueBossBarManager.suppressExternalBossBars();
         this.showCurrentPage();
      }

      private void suppressScoreboard() {
         if (QuestsConfig.isHideQuestScoreboardDuringQuestDialogue()) {
            SimpleScoreboard.blankScoreboard(this.player);
         }
      }

      private void restoreScoreboard() {
         if (QuestsConfig.isHideQuestScoreboardDuringQuestDialogue() && this.player.isOnline()) {
            QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(this.player.getUniqueId());
            if (tracking != null) {
               tracking.refreshScoreboard();
            } else if (PartySidebar.isEnabled() && PartyManager.isInParty(this.player.getUniqueId())) {
               PartySidebar.refresh(this.player);
            } else {
               SimpleScoreboard.clearScoreboard(this.player);
            }
         }
      }

      private void applyMovementLock() {
         GameClock.initialize();
         this.movementLockTick = GameClock.getCurrentTick();
         this.movementLockApplied = this.player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 2, false, false, false));
      }

      private void createBars() {
         for (int i = 0; i < this.bars.length; i++) {
            BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID, new BarFlag[0]);
            bossBar.setProgress(0.0);
            bossBar.addPlayer(this.player);
            this.bars[i] = bossBar;
         }

         this.bars[0].setTitle(OffsetFontUtil.shift(OffsetFontUtil.boxGlyph(), QuestsConfig.getQuestDialogueBoxOffsetX()));
      }

      private void showCurrentPage() {
         this.visibleCharacters = 0;
         this.updateBars();
      }

      private void tick() {
         if (!this.player.isOnline()) {
            this.close(false, false);
         } else {
            QuestDialogueBossBarManager.Page page = this.pages.get(this.pageIndex);
            if (this.visibleCharacters < page.visibleLength()) {
               this.visibleCharacters = Math.min(page.visibleLength(), this.visibleCharacters + Math.max(1, QuestsConfig.getQuestDialogueCharactersPerTick()));
               this.updateBars();
            }
         }
      }

      private void advance() {
         QuestDialogueBossBarManager.Page page = this.pages.get(this.pageIndex);
         if (this.visibleCharacters < page.visibleLength()) {
            this.visibleCharacters = page.visibleLength();
            this.updateBars();
         } else if (this.pageIndex + 1 >= this.pages.size()) {
            this.close(true);
         } else {
            this.pageIndex++;
            this.showCurrentPage();
         }
      }

      private void updateBars() {
         QuestDialogueBossBarManager.Page page = this.pages.get(this.pageIndex);
         this.bars[1].setTitle(QuestDialogueBossBarManager.formatName(this.speakerName));
         int remaining = this.visibleCharacters;

         for (int i = 0; i < 3; i++) {
            String line = page.lines().get(i);
            int visibleLength = QuestDialogueBossBarManager.visibleLength(line);
            int lineCharacters = Math.min(visibleLength, Math.max(0, remaining));
            this.bars[2 + i].setTitle(QuestDialogueBossBarManager.formatLine(QuestDialogueBossBarManager.substringVisible(line, lineCharacters)));
            remaining -= visibleLength;
         }

         this.bars[5].setTitle(QuestDialogueBossBarManager.formatPrompt());
      }

      private void close(boolean runCallback) {
         this.close(runCallback, true);
      }

      private void close(boolean runCallback, boolean restoreUi) {
         if (!this.closed) {
            this.closed = true;
            QuestDialogueBossBarManager.activeSessions.remove(this.player.getUniqueId(), this);
            if (QuestDialogueBossBarManager.activeSessions.isEmpty() && QuestDialogueBossBarManager.keyedBarTask != null) {
               QuestDialogueBossBarManager.keyedBarTask.cancel();
               QuestDialogueBossBarManager.keyedBarTask = null;
            }

            if (this.task != null) {
               this.task.cancel();
            }

            for (BossBar bossBar : this.bars) {
               if (bossBar != null) {
                  bossBar.removeAll();
               }
            }

            this.clearMovementLock();
            if (restoreUi) {
               this.restoreBossBars();
               this.restoreScoreboard();
            } else {
               BossBarOrderManager.discardSuspension(this.player, this.bossBarSuspension);
               this.bossBarSuspension = null;
            }

            if (runCallback && this.onComplete != null && this.player.isOnline()) {
               Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, this.onComplete);
            }
         }
      }

      private void clearMovementLock() {
         if (this.movementLockApplied && this.player.isOnline()) {
            long elapsed = Math.max(0L, GameClock.getCurrentTick() - this.movementLockTick);
            int expected = (int)Math.max(0L, 600L - elapsed);
            PotionEffect current = this.player.getPotionEffect(PotionEffectType.SLOWNESS);
            if (current != null
               && expected != 0
               && current.getAmplifier() == 2
               && !current.isAmbient()
               && !current.hasParticles()
               && !current.hasIcon()
               && Math.abs((long)current.getDuration() - expected) <= 1L) {
               this.player.removePotionEffect(PotionEffectType.SLOWNESS);
               if (!this.player.hasPotionEffect(PotionEffectType.SLOWNESS) && this.previousSlowness != null) {
                  int remaining = this.previousSlowness.isInfinite() ? -1 : (int)Math.max(0L, this.previousSlowness.getDuration() - elapsed);
                  if (remaining != 0) {
                     this.player
                        .addPotionEffect(
                           new PotionEffect(
                              PotionEffectType.SLOWNESS,
                              remaining,
                              this.previousSlowness.getAmplifier(),
                              this.previousSlowness.isAmbient(),
                              this.previousSlowness.hasParticles(),
                              this.previousSlowness.hasIcon()
                           )
                        );
                  }
               }
            }
         }
      }

      private void restoreBossBars() {
         BossBarOrderManager.Suspension suspension = this.bossBarSuspension;
         this.bossBarSuspension = null;

         try {
            BossBarOrderManager.resumePlayer(this.player, suspension);
         } catch (RuntimeException var4) {
            this.scheduleManagedBossBarRestoreRetry(suspension, var4);
         }

         if (this.player.isOnline()) {
            for (KeyedBossBar bossBar : this.hiddenKeyedBossBars) {
               if (Bukkit.getBossBar(bossBar.getKey()) == bossBar) {
                  bossBar.addPlayer(this.player);
               }
            }
         }
      }

      private void scheduleManagedBossBarRestoreRetry(BossBarOrderManager.Suspension suspension, RuntimeException firstFailure) {
         if (this.player.isOnline() && MetadataHandler.PLUGIN.isEnabled()) {
            try {
               Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                  try {
                     BossBarOrderManager.resumePlayer(this.player, suspension);
                  } catch (RuntimeException var4x) {
                     firstFailure.addSuppressed(var4x);
                     this.discardFailedManagedBossBarRestore(suspension, firstFailure);
                  }
               });
            } catch (RuntimeException var4) {
               firstFailure.addSuppressed(var4);
               this.discardFailedManagedBossBarRestore(suspension, firstFailure);
            }
         } else {
            this.discardFailedManagedBossBarRestore(suspension, firstFailure);
         }
      }

      private void discardFailedManagedBossBarRestore(BossBarOrderManager.Suspension suspension, RuntimeException failure) {
         BossBarOrderManager.discardSuspension(this.player, suspension);
         MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to restore managed boss bars after quest dialogue", (Throwable)failure);
      }
   }

   private record Page(List<String> lines) {
      private int visibleLength() {
         int total = 0;

         for (String line : this.lines) {
            total += QuestDialogueBossBarManager.visibleLength(line);
         }

         return total;
      }
   }

   public static class QuestDialogueBossBarEvents implements Listener {
      @EventHandler
      public void onSneak(PlayerToggleSneakEvent event) {
         if (event.isSneaking()) {
            QuestDialogueBossBarManager.DialogueSession session = QuestDialogueBossBarManager.activeSessions.get(event.getPlayer().getUniqueId());
            if (session != null) {
               event.setCancelled(true);
               session.advance();
            }
         }
      }

      @EventHandler
      public void onQuit(PlayerQuitEvent event) {
         QuestDialogueBossBarManager.DialogueSession session = QuestDialogueBossBarManager.activeSessions.get(event.getPlayer().getUniqueId());
         if (session != null) {
            session.close(false, false);
         }
      }
   }
}
