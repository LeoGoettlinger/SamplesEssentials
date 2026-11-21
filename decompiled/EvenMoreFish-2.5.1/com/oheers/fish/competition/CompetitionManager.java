package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.RarityKey;
import com.oheers.fish.competition.configs.CompetitionConversions;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.types.LargestTotalCompetitionType;
import com.oheers.fish.competition.types.MostFishCompetitionType;
import com.oheers.fish.competition.types.RandomCompetitionType;
import com.oheers.fish.competition.types.ShortestFishCompetitionType;
import com.oheers.fish.competition.types.ShortestTotalCompetitionType;
import com.oheers.fish.competition.types.SpecificFishCompetitionType;
import com.oheers.fish.competition.types.SpecificRarityCompetitionType;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.TimeCode;
import java.io.File;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionManager extends AbstractFileBasedManager<CompetitionFile> {
   private static final CompetitionManager INSTANCE = new CompetitionManager();
   protected static final File dataFile = new File(EvenMoreFish.getInstance().getDataFolder(), "competition-data.yml.tmp");
   private static final List<CompetitionFile> held = new ArrayList<>();
   private final TreeMap<TimeCode, CompetitionFile> competitions = new TreeMap<>(TimeCode.getComparator());
   private final AutoRunner autoRunner = new AutoRunner(this);
   protected Competition activeCompetition;

   private CompetitionManager() {
   }

   @NonNull
   public static CompetitionManager getInstance() {
      return INSTANCE;
   }

   @Override
   protected void performPreLoadConversions() {
      new CompetitionConversions().performCheck();
   }

   @Override
   protected void loadItems() {
      this.loadDefaultTypes();
      this.loadItemsFromFiles("competitions", CompetitionFile::new, CompetitionFile::getId, CompetitionFile::isDisabled);
      this.competitions.clear();
      this.getItemMap()
         .values()
         .forEach(
            file -> {
               if (!this.loadSpecificDayTimes(file)) {
                  if (!this.loadRepeatedTiming(file)) {
                     EvenMoreFish.getInstance()
                        .debug(
                           Level.WARNING,
                           Optional.ofNullable(file.getFile()).map(File::getName).orElse("Competition")
                              + "'s timings are not configured properly. This competition will never automatically start."
                        );
                  }
               }
            }
         );
   }

   @Override
   protected void logLoadedItems() {
      EvenMoreFish.getInstance()
         .getLogger()
         .info("Loaded " + this.getItemMap().size() + " competition file(s) and " + this.competitions.size() + " scheduled competitions.");
   }

   @NonNull
   public AutoRunner getAutoRunner() {
      return this.autoRunner;
   }

   public Map<TimeCode, CompetitionFile> getCompetitions() {
      return this.competitions;
   }

   private boolean loadSpecificDayTimes(@NonNull CompetitionFile file) {
      Map<DayOfWeek, List<String>> scheduledDays = file.getScheduledDays();
      if (scheduledDays.isEmpty()) {
         return false;
      } else {
         scheduledDays.forEach((day, times) -> times.forEach(time -> {
            String[] split = time.split(":");
            if (split.length != 2) {
               Logging.warn("Invalid TimeCode in " + file.getFileName() + ": " + time);
            } else {
               this.competitions.put(this.generateTimeCode(day, split[0], split[1]), file);
            }
         }));
         return true;
      }
   }

   private boolean loadRepeatedTiming(@NonNull CompetitionFile file) {
      List<String> repeatedTimes = file.getTimes();
      if (repeatedTimes.isEmpty()) {
         return false;
      } else {
         List<DayOfWeek> daysToUse = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
         daysToUse.removeAll(file.getBlacklistedDays());

         for (String time : repeatedTimes) {
            String[] split = time.split(":");
            if (split.length != 2) {
               Logging.warn("Invalid TimeCode in " + file.getFileName() + ": " + time);
            } else {
               for (DayOfWeek day : daysToUse) {
                  this.competitions.put(this.generateTimeCode(day, split[0], split[1]), file);
               }
            }
         }

         return true;
      }
   }

   @Nullable
   public TimeCode generateTimeCode(@NonNull DayOfWeek day, @NonNull String hourStr, @NonNull String minuteStr) {
      Integer hour = FishUtils.getInteger(hourStr);
      Integer minute = FishUtils.getInteger(minuteStr);
      return hour != null && minute != null ? TimeCode.exact(day, hour, minute) : null;
   }

   public int getSize() {
      return this.competitions.size();
   }

   @Nullable
   public TimeCode getNextCompetition() {
      if (this.competitions.isEmpty()) {
         return null;
      } else {
         TimeCode now = TimeCode.now();
         TimeCode next = this.competitions.ceilingKey(now);
         return next == null ? this.competitions.firstKey() : next;
      }
   }

   public boolean hasTimings() {
      return !this.competitions.isEmpty();
   }

   @Nullable
   public CompetitionFile getFileFromId(@Nullable String id) {
      if (id == null) {
         return null;
      } else {
         for (CompetitionFile file : this.competitions.values()) {
            if (file.getId().equalsIgnoreCase(id)) {
               return file;
            }
         }

         return null;
      }
   }

   private void loadDefaultTypes() {
      CompetitionTypeRegistry registry = CompetitionTypeRegistry.getInstance();
      registry.register((CompetitionType)CompetitionType.DEFAULT, true);
      registry.register((CompetitionType)(new LargestTotalCompetitionType()), true);
      registry.register((CompetitionType)(new MostFishCompetitionType()), true);
      registry.register((CompetitionType)(new RandomCompetitionType()), true);
      registry.register((CompetitionType)(new ShortestFishCompetitionType()), true);
      registry.register((CompetitionType)(new ShortestTotalCompetitionType()), true);
      registry.register((CompetitionType)(new SpecificFishCompetitionType()), true);
      registry.register((CompetitionType)(new SpecificRarityCompetitionType()), true);
   }

   protected void setActive(@NonNull Competition competition) {
      if (this.activeCompetition != null) {
         this.activeCompetition.end(false);
      }

      this.activeCompetition = competition;
   }

   public boolean isCompetitionActive() {
      return this.activeCompetition != null;
   }

   @Nullable
   public Competition getActiveCompetition() {
      return this.activeCompetition;
   }

   public boolean isDoingFirstPlaceActionBar() {
      if (this.activeCompetition == null) {
         Logging.warn("Cannot check Competition#isDoingFirstPlaceActionBar: There is no active competition.");
         return false;
      } else {
         CompetitionType type = this.activeCompetition.getCompetitionType();
         if (type == null) {
            Logging.warn("Cannot check Competition#isDoingFirstPlaceActionBar: Invalid CompetitionType.");
            return false;
         } else {
            boolean doActionBarMessage = MessageConfig.getInstance().getConfig().getBoolean("action-bar-message");
            List<String> supportedTypes = MessageConfig.getInstance().getConfig().getStringList("action-bar-types");
            boolean isSupportedActionBarType = this.activeCompetition != null && supportedTypes.contains(type.getKey());
            return doActionBarMessage && isSupportedActionBarType;
         }
      }
   }

   @NonNull
   public EMFMessage getNextCompetitionMessage() {
      if (this.activeCompetition != null) {
         return EMFSingleMessage.empty();
      } else {
         long remainingTime = this.getRemainingTime();
         if (remainingTime == -1L) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITIONS_SCHEDULED.getMessage();
         } else {
            EMFMessage message = ConfigMessage.PLACEHOLDER_TIME_REMAINING_INACTIVE.getMessage();
            message.setDays(Long.toString(remainingTime / 1440L));
            message.setHours(Long.toString(remainingTime % 1440L / 60L));
            message.setMinutes(Long.toString(remainingTime % 1440L % 60L % 60L));
            return message;
         }
      }
   }

   private long getRemainingTime() {
      TimeCode next = this.getNextCompetition();
      if (next == null) {
         return -1L;
      } else {
         long startTime = next.toMillis();
         long currentTime = System.currentTimeMillis();
         return Duration.ofMillis(startTime - currentTime).toMinutes();
      }
   }

   public void resumeFromFile() {
      EvenMoreFish plugin = EvenMoreFish.getInstance();
      if (dataFile.exists()) {
         ConfigBase base = new ConfigBase(dataFile, plugin, false);
         YamlDocument config = base.getConfig();
         String id = config.getString("comp-id");
         long totalDuration = config.getLong("total-duration");
         long timeLeft = config.getLong("time-left");
         long epochStartTime = config.getLong("start-time", -1L);
         CompetitionFile file = getInstance().getFileFromId(id);
         if (file == null) {
            Logging.warn("Failed to resume competition. It is no longer configured?");
            dataFile.delete();
         } else {
            Competition competition = new Competition(file);
            competition.timeLeft = timeLeft;
            competition.maxDuration = totalDuration;
            competition.adminStarted = true;
            if (epochStartTime != -1L) {
               Instant instant = Instant.ofEpochMilli(epochStartTime);
               competition.setStartTime(instant);
            }

            if (competition.begin()) {
               Section leaderboardSection = config.getSection("leaderboard");
               if (leaderboardSection == null) {
                  Logging.debug("Competition backup file had no leaderboard data.");
               } else {
                  CompetitionType type = competition.getCompetitionType();
                  leaderboardSection.getRoutesAsStrings(false).forEach(key -> {
                     Section entrySection = leaderboardSection.getSection(key);
                     if (entrySection != null) {
                        UUID player;
                        try {
                           player = UUID.fromString(key);
                        } catch (IllegalArgumentException var9x) {
                           Logging.warn("Competition backup file had invalid uuid: " + key);
                           return;
                        }

                        String fishStr = entrySection.getString("fish");
                        RarityKey rarityKey = RarityKey.of(fishStr);
                        if (rarityKey == null) {
                           Logging.warn("Failed to restore leaderboard entry. Fish " + fishStr + " is no longer configured?");
                        } else {
                           CompetitionEntry entry = new CompetitionEntry(player, rarityKey.getFish(), type);
                           entry.value = entrySection.getFloat("value");
                           entry.time = entrySection.getLong("time");
                           competition.leaderboard.addEntry(entry);
                        }
                     }
                  });
                  dataFile.delete();
               }
            }
         }
      }
   }

   public void holdCompetition(@NonNull CompetitionFile file) {
      if (!MainConfig.getInstance().shouldCompetitionHold()) {
         Logging.debug("Could not hold a competition as the feature is disabled.");
      } else {
         held.add(file);
      }
   }

   protected void checkHeldCompetition() {
      if (held.isEmpty()) {
         Logging.debug("No competitions have been held back.");
      } else {
         CompetitionFile file = held.removeFirst();
         if (file != null) {
            Logging.info("A competition was held back during this one. It will now be started.");
            new Competition(file).begin();
         }
      }
   }
}
