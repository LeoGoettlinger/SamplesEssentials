package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.timer.CompetitionBackupTimer;
import com.oheers.fish.competition.timer.CompetitionTimer;
import com.oheers.fish.competition.types.SpecificFishCompetitionType;
import com.oheers.fish.competition.types.SpecificRarityCompetitionType;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Emitter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Competition {
   private static final CompetitionManager manager = CompetitionManager.getInstance();
   protected Leaderboard leaderboard;
   @Nullable
   private CompetitionType competitionType;
   private String competitionName;
   protected boolean adminStarted = false;
   private EMFMessage startMessage;
   protected long maxDuration;
   protected long timeLeft;
   private CompetitionBossbar statusBar;
   private Long epochStartTime;
   private LocalDateTime startTime;
   private final List<Long> alertTimes;
   private final Map<Integer, List<Reward>> rewards;
   private int playersNeeded;
   private Sound startSound;
   private CompetitionTimer timingSystem;
   private CompetitionBackupTimer backupSystem;
   private CompetitionFile competitionFile;
   private int numberNeeded = 0;
   private UUID singleWinner = null;

   public Competition(@NonNull final CompetitionFile competitionFile) {
      this.competitionFile = competitionFile;
      this.competitionName = competitionFile.getId();
      this.playersNeeded = competitionFile.getPlayersNeeded();
      this.startSound = competitionFile.getStartSound();
      this.maxDuration = competitionFile.getDuration() * 60L;
      this.timeLeft = this.maxDuration;
      this.alertTimes = competitionFile.getAlertTimes();
      this.rewards = competitionFile.getRewards();
      this.numberNeeded = competitionFile.getNumberNeeded();
      this.competitionType = resolveType(competitionFile, this);
   }

   @Nullable
   private static CompetitionType resolveType(@NonNull CompetitionFile file, @NonNull Competition competition) {
      CompetitionType type = file.getType();
      if (type == null) {
         Logging.warn("Invalid competition: " + file.getId() + " has an invalid competition type.");
         return null;
      } else {
         return (CompetitionType)(type instanceof CompetitionType.Random random
            ? new CompetitionType.Forwarding(random, random.getRandomType(competition))
            : type);
      }
   }

   @NonNull
   private CompetitionBossbar createBossbar() {
      CompetitionBossbar bar = new CompetitionBossbar();
      bar.setShouldShow(this.competitionFile.shouldShowBossbar());
      bar.setColour(this.competitionFile.getBossbarColour());
      EMFSingleMessage prefix = this.competitionFile.getBossbarPrefix();
      IRarity selectedRarity = this.getSelectedRarity();
      if (selectedRarity != null) {
         prefix.setRarity(selectedRarity.getDisplayName());
      }

      IFish selectedFish = this.getSelectedFish();
      if (selectedFish != null) {
         prefix.setRarity(selectedFish.getRarity().getDisplayName());
         prefix.setVariable("{fish}", selectedFish.getDisplayName());
      }

      if (this.competitionType != null) {
         bar.setPrefix(prefix, this.competitionType);
      }

      return bar;
   }

   public Competition(final long duration, @NonNull final CompetitionType type) {
      this.maxDuration = duration;
      this.alertTimes = new ArrayList<>();
      this.rewards = new HashMap<>();
      this.competitionType = type;
   }

   public void setMaxDuration(long durationSeconds) {
      this.maxDuration = durationSeconds;
   }

   public void setTimeLeft(long durationSeconds) {
      this.timeLeft = durationSeconds;
   }

   public void setTime(long durationSeconds) {
      this.setMaxDuration(durationSeconds);
      this.setTimeLeft(durationSeconds);
   }

   public void addTime(long durationSeconds) {
      this.maxDuration += durationSeconds;
      this.timeLeft += durationSeconds;
   }

   public boolean isPlayerRequirementMet() {
      return EvenMoreFish.getInstance().getVisibleOnlinePlayers().size() >= this.playersNeeded;
   }

   public boolean begin() {
      if (this.maxDuration <= 0L) {
         Logging.warn("Tried to start a competition with an invalid duration: " + this.competitionFile.getId());
         return false;
      } else if (this.timeLeft <= 0L) {
         Logging.warn("Tried to start a competition that has already ended.");
         return false;
      } else {
         try {
            if (!this.isAdminStarted() && !this.isPlayerRequirementMet()) {
               ConfigMessage.NOT_ENOUGH_PLAYERS.getMessage().broadcast();
               return false;
            } else if (this.competitionType == null) {
               Logging.warn("Cannot start competition " + this.competitionName + ": Invalid CompetitionType.");
               return false;
            } else if (!this.competitionType.isUsable(this)) {
               return false;
            } else {
               manager.setActive(this);
               this.leaderboard = new Leaderboard(this.competitionType);
               if (this.statusBar == null) {
                  this.statusBar = this.createBossbar();
               }

               this.statusBar.show();
               this.initTimer();
               this.announceBegin();
               EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(this);
               Bukkit.getServer().getPluginManager().callEvent(startEvent);
               if (this.epochStartTime == null) {
                  this.setStartTime(Instant.now());
               }

               this.getCompetitionFile().getStartCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
               return true;
            }
         } catch (Exception var2) {
            Logging.error("An exception was thrown while starting the competition.", var2);
            this.end(true);
            return false;
         }
      }
   }

   public void end(boolean startFail) {
      this.end(startFail, false);
   }

   public void end(boolean startFail, boolean save) {
      if (this.timingSystem != null) {
         this.timingSystem.stop();
      }

      if (this.backupSystem != null) {
         this.backupSystem.stop();
      }

      if (this.statusBar != null) {
         this.statusBar.hide();
      }

      if (startFail) {
         manager.activeCompetition = null;
      } else if (save && MainConfig.getInstance().shouldCompetitionResume()) {
         this.saveToFile();
      } else {
         try {
            CompetitionManager.dataFile.delete();
            this.fireEndEvent();
            this.notifyPlayers();
            this.processRewards();
            this.resetCompetitionTypeIfForwarding();
            this.updateDatabase();
            this.leaderboard.clear();
         } catch (Exception var7) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "An exception was thrown while the competition was being ended!", (Throwable)var7);
         } finally {
            this.timeLeft = 0L;
            manager.activeCompetition = null;
            manager.checkHeldCompetition();
         }
      }
   }

   private void fireEndEvent() {
      EMFCompetitionEndEvent endEvent = new EMFCompetitionEndEvent(this);
      endEvent.callEvent();
   }

   private void notifyPlayers() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         ConfigMessage.COMPETITION_END.getMessage().send(player);
         this.sendLeaderboard(player);
      }
   }

   private void processRewards() {
      if (this.competitionType != null) {
         if (this.competitionType.isSingleReward()) {
            if (this.singleWinner == null) {
               Logging.warn("Single-winner competition ended without a winner.");
               return;
            }

            this.singleReward(this.singleWinner);
         } else {
            this.handleRewards();
         }
      }
   }

   private void resetCompetitionTypeIfForwarding() {
      if (this.competitionType instanceof CompetitionType.Forwarding forwarding) {
         this.competitionType = forwarding.getRandom();
      }
   }

   private void updateDatabase() {
      if (DatabaseUtil.isDatabaseOnline()) {
         EvenMoreFish plugin = EvenMoreFish.getInstance();
         plugin.getPluginDataManager()
            .getCompetitionDataManager()
            .update(this.competitionName, new CompetitionReport(this, this.startTime, LocalDateTime.now()));
      }
   }

   private void initTimer() {
      CompetitionTimer timer = new CompetitionTimer(this);
      timer.start();
      this.timingSystem = timer;
      if (MainConfig.getInstance().isCompetitionBackupEnabled()) {
         CompetitionBackupTimer backupTimer = new CompetitionBackupTimer(this);
         backupTimer.start();
         this.backupSystem = backupTimer;
      }
   }

   private boolean processCompetitionSecond(long timeLeft) {
      if (this.alertTimes.contains(timeLeft)) {
         EMFMessage message = this.format(ConfigMessage.TIME_ALERT);
         message.broadcast();
      } else if (timeLeft <= 0L) {
         this.end(false);
         return true;
      }

      return false;
   }

   @NonNull
   public EMFMessage format(@NonNull ConfigMessage configMessage) {
      return this.format(configMessage.getMessage());
   }

   @NonNull
   public EMFMessage format(@NonNull Component message) {
      return this.format(EMFSingleMessage.of(message));
   }

   @NonNull
   public EMFMessage format(@NonNull EMFMessage message) {
      if (this.competitionType == null) {
         Logging.warn("Cannot format a Competition message: Invalid CompetitionType.");
         return EMFSingleMessage.empty();
      } else {
         message.setTimeFormatted(FishUtils.timeFormat(this.timeLeft));
         message.setTimeRaw(FishUtils.timeRaw(this.timeLeft));
         message.setCompetitionType(this.competitionType.getTypeVariable(this));
         if (this.numberNeeded <= 0) {
            return message;
         } else {
            message.setAmount(this.numberNeeded);
            IRarity selectedRarity = this.getSelectedRarity();
            if (selectedRarity != null) {
               message.setRarity(selectedRarity);
               return message;
            } else {
               IFish selectedFish = this.getSelectedFish();
               if (selectedFish != null) {
                  message.setRarity(selectedFish.getRarity());
                  message.setFishCaught(selectedFish);
               }

               return message;
            }
         }
      }
   }

   public boolean decreaseTime() {
      if (this.processCompetitionSecond(this.timeLeft)) {
         return true;
      } else {
         this.timeLeft--;
         return false;
      }
   }

   public void applyToLeaderboard(IFish fish, Player fisher) {
      UUID uuid = fisher.getUniqueId();
      Scheduling.getInstance().runTask(() -> {
         if (this.competitionType != null) {
            this.competitionType.applyToLeaderboard(fish, uuid, this.leaderboard, this);
         }
      });
   }

   public void announceBegin() {
      this.getStartMessage().broadcast();
      if (this.startSound != null) {
         Bukkit.getOnlinePlayers().forEach(player -> player.playSound(this.startSound, Emitter.self()));
      }
   }

   public void sendLeaderboard(@NonNull CommandSender sender) {
      if (!manager.isCompetitionActive()) {
         ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
      } else if (this.leaderboard.getSize() == 0) {
         ConfigMessage.NO_FISH_CAUGHT.getMessage().send(sender);
      } else {
         List<String> competitionColours = this.competitionFile.getLeaderboardColours();
         List<CompetitionEntry> entries = this.leaderboard.getEntries();
         EMFMessage leaderboardMessage = this.buildLeaderboardMessage(entries, competitionColours);
         leaderboardMessage.send(sender);
         EMFMessage message = ConfigMessage.LEADERBOARD_TOTAL_PLAYERS.getMessage();
         message.setAmount(Integer.toString(this.leaderboard.getSize()));
         message.send(sender);
      }
   }

   @NonNull
   private EMFListMessage buildLeaderboardMessage(List<CompetitionEntry> entries, List<String> competitionColours) {
      if (this.competitionType == null) {
         Logging.warn("Cannot fetch leaderboard message: Invalid CompetitionType.");
         return EMFListMessage.empty();
      } else {
         if (entries == null) {
            entries = List.of();
         }

         int maxCount = MessageConfig.getInstance().getLeaderboardCount();
         List<Component> leaderboard = new ArrayList<>();
         int pos = 0;

         for (CompetitionEntry entry : entries) {
            pos++;
            if (pos > competitionColours.size() || pos > maxCount) {
               break;
            }

            EMFSingleMessage message = EMFSingleMessage.of(this.competitionType.formatLeaderboardEntry(entry));
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getPlayer());
            String name = FishUtils.getPlayerNameOrDefault(player, "Unknown Player");
            EMFSingleMessage colour = EMFSingleMessage.fromString(competitionColours.get(pos - 1));
            colour.setVariable("{name}", name);
            message.setVariable("{pos_colour}", "");
            message.setVariable("{player}", colour);
            message.setPlayer(player);
            message.setPosition(Integer.toString(pos));
            message.setRarity(entry.getFish().getRarity().getDisplayName());
            message.setFishCaught(entry.getFish().getDisplayName());
            leaderboard.add(message.getComponentMessage());
         }

         return EMFListMessage.ofList(leaderboard);
      }
   }

   private void handleDatabaseUpdates(CompetitionEntry entry, boolean isTopEntry) {
      if (DatabaseUtil.isDatabaseOnline()) {
         UserReport userReport = EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager().get(String.valueOf(entry.getPlayer()));
         if (userReport == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not fetch User Report for " + entry.getPlayer() + ", their data has not been modified.");
         } else {
            if (isTopEntry) {
               userReport.incrementCompetitionsWon(1);
            }

            userReport.incrementCompetitionsJoined(1);
         }
      }
   }

   private void handleRewards() {
      if (this.leaderboard.getSize() == 0) {
         ConfigMessage.NO_WINNERS.getMessage().broadcast();
      } else {
         int rewardPlace = 1;
         List<CompetitionEntry> entries = this.leaderboard.getEntries();
         CompetitionEntry topEntry = this.leaderboard.getTopEntry();
         if (DatabaseUtil.isDatabaseOnline() && !entries.isEmpty()) {
            this.handleDatabaseUpdates(topEntry, true);
         }

         for (CompetitionEntry entry : entries) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getPlayer());
            if (this.rewards.containsKey(rewardPlace)) {
               this.rewards.get(rewardPlace).forEach(reward -> reward.give(player, null));
            } else {
               List<Reward> participation = this.rewards.get(-1);
               if (participation != null) {
                  participation.forEach(reward -> reward.give(player, null));
               }
            }

            if (!topEntry.getPlayer().equals(entry.getPlayer())) {
               this.handleDatabaseUpdates(entry, false);
            }

            rewardPlace++;
         }
      }
   }

   private void singleReward(UUID winner) {
      if (this.competitionType != null) {
         OfflinePlayer player = Bukkit.getOfflinePlayer(winner);
         EMFMessage message = this.format(ConfigMessage.COMPETITION_SINGLE_WINNER);
         message.setPlayer(player);
         message.setCompetitionType(this.competitionType.getTypeVariable(this));
         message.broadcast();
         if (!this.rewards.isEmpty()) {
            for (Reward reward : this.rewards.get(1)) {
               reward.give(player, null);
            }
         }

         CompetitionEntry entry = this.leaderboard.getEntry(player.getUniqueId());
         this.handleDatabaseUpdates(entry, true);
         this.leaderboard.getEntries().forEach(e -> {
            if (!e.getPlayer().equals(player.getUniqueId())) {
               this.handleDatabaseUpdates(e, false);
            }
         });
      }
   }

   @NonNull
   public CompetitionBossbar getStatusBar() {
      return this.statusBar;
   }

   @Nullable
   public CompetitionType getCompetitionType() {
      return this.competitionType;
   }

   public void setNumberNeeded(int numberNeeded) {
      this.numberNeeded = numberNeeded;
   }

   public int getLeaderboardSize() {
      return this.leaderboard.getSize();
   }

   @NonNull
   public Leaderboard getLeaderboard() {
      return this.leaderboard;
   }

   @NonNull
   public EMFMessage getStartMessage() {
      if (this.competitionType == null) {
         Logging.warn("Cannot fetch competition start message: Invalid CompetitionType.");
         return EMFSingleMessage.empty();
      } else {
         if (this.startMessage == null) {
            this.startMessage = ConfigMessage.COMPETITION_START.getMessage();
            this.startMessage.setCompetitionType(this.competitionType.getTypeVariable(this));
         }

         return this.startMessage;
      }
   }

   @NonNull
   public String getCompetitionName() {
      return this.competitionName;
   }

   @Nullable
   public LocalDateTime getStartTime() {
      return this.startTime;
   }

   public void setStartTime(@NonNull Instant instant) {
      this.epochStartTime = instant.getEpochSecond();
      this.startTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
   }

   @Nullable
   public Long getEpochStartTime() {
      return this.epochStartTime;
   }

   @NonNull
   public CompetitionFile getCompetitionFile() {
      return this.competitionFile;
   }

   public void setCompetitionType(@Nullable CompetitionType competitionType) {
      this.competitionType = competitionType;
   }

   @Nullable
   public IFish getSelectedFish() {
      return this.competitionType instanceof SpecificFishCompetitionType specific ? specific.getSelectedFish() : null;
   }

   @Nullable
   public IRarity getSelectedRarity() {
      return this.competitionType instanceof SpecificRarityCompetitionType specific ? specific.getSelectedRarity() : null;
   }

   public int getNumberNeeded() {
      return this.numberNeeded;
   }

   public boolean isAdminStarted() {
      return this.adminStarted;
   }

   public void setAdminStarted(boolean adminStarted) {
      this.adminStarted = adminStarted;
   }

   public long getTimeLeft() {
      return this.timeLeft;
   }

   public long getMaxDuration() {
      return this.maxDuration;
   }

   public void setSingleWinner(@Nullable UUID winner) {
      this.singleWinner = winner;
   }

   @ApiStatus.Experimental
   public void saveToFile() {
      EvenMoreFish plugin = EvenMoreFish.getInstance();
      ConfigBase base = new ConfigBase(CompetitionManager.dataFile, plugin, false);
      YamlDocument config = base.getConfig();
      config.set("comp-id", this.getCompetitionFile().getId());
      config.set("total-duration", this.maxDuration);
      config.set("time-left", this.timeLeft);
      if (this.epochStartTime != null) {
         config.set("start-time", this.epochStartTime);
      }

      for (CompetitionEntry entry : this.leaderboard.getEntries()) {
         UUID uuid = entry.getPlayer();
         config.set("leaderboard." + uuid + ".fish", entry.getFish().getRarityKey().toString());
         config.set("leaderboard." + uuid + ".value", entry.getValue());
         config.set("leaderboard." + uuid + ".time", entry.getTime());
      }

      base.save();
   }
}
