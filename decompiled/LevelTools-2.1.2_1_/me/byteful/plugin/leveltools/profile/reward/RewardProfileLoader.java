package me.byteful.plugin.leveltools.profile.reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import me.byteful.plugin.leveltools.api.RewardType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class RewardProfileLoader {
   private final Logger logger;

   public RewardProfileLoader(@NotNull Logger logger) {
      this.logger = logger;
   }

   @NotNull
   public Map<String, RewardProfile> load(@NotNull FileConfiguration config) {
      Map<String, RewardProfile> profiles = new HashMap<>();
      ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
      if (profilesSection == null) {
         this.logger.warning("No reward profiles found in reward_profiles.yml");
         return profiles;
      } else {
         for (String profileId : profilesSection.getKeys(false)) {
            ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileId);
            if (profileSection != null) {
               try {
                  RewardProfile profile = this.parseProfile(profileId, profileSection);
                  profiles.put(profileId, profile);
               } catch (Exception var8) {
                  this.logger.severe("Failed to load reward profile '" + profileId + "': " + var8.getMessage());
               }
            }
         }

         this.logger.info("Loaded " + profiles.size() + " reward profile(s).");
         return profiles;
      }
   }

   @NotNull
   private RewardProfile parseProfile(@NotNull String id, @NotNull ConfigurationSection section) {
      ConfigurationSection levelsSection = section.getConfigurationSection("levels");
      Map<Integer, List<RewardEntry>> levelRewards = new HashMap<>();
      if (levelsSection != null) {
         for (String levelKey : levelsSection.getKeys(false)) {
            try {
               int level = Integer.parseInt(levelKey);
               List<String> rewardStrings = levelsSection.getStringList(levelKey);
               List<RewardEntry> entries = this.parseRewardEntries(rewardStrings);
               if (!entries.isEmpty()) {
                  levelRewards.put(level, entries);
               }
            } catch (NumberFormatException var10) {
               this.logger.warning("Invalid level key '" + levelKey + "' in reward profile '" + id + "'");
            }
         }
      }

      return new RewardProfile(id, levelRewards);
   }

   @NotNull
   private List<RewardEntry> parseRewardEntries(@NotNull List<String> rewardStrings) {
      List<RewardEntry> entries = new ArrayList<>();

      for (String rewardString : rewardStrings) {
         RewardEntry entry = this.parseRewardEntry(rewardString);
         if (entry != null) {
            entries.add(entry);
         }
      }

      return entries;
   }

   private RewardEntry parseRewardEntry(@NotNull String rewardString) {
      if (rewardString.isEmpty()) {
         return null;
      } else {
         String[] split = rewardString.split(" ");
         if (split.length < 1) {
            return null;
         } else {
            String typeKey = split[0].toLowerCase().trim().replace(" ", "-").replace("_", "-");
            Optional<RewardType> typeOpt = RewardType.fromConfigKey(typeKey);
            if (!typeOpt.isPresent()) {
               this.logger.warning("Unknown reward type: " + split[0]);
               return null;
            } else {
               return new RewardEntry(typeOpt.get(), split, rewardString);
            }
         }
      }
   }
}
