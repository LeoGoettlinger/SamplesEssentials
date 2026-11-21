package com.oheers.fish.competition.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.serializer.SoundSerializer;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.CompetitionTypeRegistry;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.EMFSingleMessage;
import java.io.File;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionFile extends ConfigBase {
   @NonNull
   private final Logger logger = this.getPlugin().getLogger();
   @NonNull
   private final String id;
   @Nullable
   private final CompetitionType type;
   private final int duration;

   public CompetitionFile(@NonNull File file) throws InvalidConfigurationException {
      super(file, EvenMoreFish.getInstance(), false);
      CompetitionFileUpdates.update(this);
      this.id = this.validateId();
      this.type = null;
      this.duration = this.validateDuration();
   }

   public CompetitionFile(@NonNull String id, @NonNull CompetitionType type, int duration) {
      this.id = id;
      this.type = type;
      this.duration = duration;
   }

   private String validateId() throws InvalidConfigurationException {
      String id = this.getConfig().getString("id");
      if (id == null) {
         throw new InvalidConfigurationException("CompetitionFile " + this.getFileName() + " has no configured id.");
      } else {
         return id;
      }
   }

   private int validateDuration() throws InvalidConfigurationException {
      Integer duration = this.getConfig().getInt("duration", null);
      if (duration == null) {
         throw new InvalidConfigurationException("CompetitionFile " + this.getFileName() + " has no configured duration.");
      } else if (duration < 1) {
         throw new InvalidConfigurationException("CompetitionFile " + this.getFileName() + " has an invalid duration. Must be 1 or more.");
      } else {
         return duration;
      }
   }

   @NonNull
   public String getId() {
      return this.id;
   }

   public boolean isDisabled() {
      return this.getConfig().getBoolean("disabled");
   }

   @Nullable
   public CompetitionType getType() {
      if (this.type != null) {
         return this.type;
      } else {
         String typeStr = this.getConfig().getString("type");
         if (typeStr == null) {
            Logging.warn("CompetitionFile " + this.getFileName() + " has no configured type.");
            return null;
         } else {
            CompetitionType type = CompetitionTypeRegistry.getInstance().get(typeStr);
            if (type == null) {
               Logging.warn("CompetitionFile " + this.getFileName() + " has an invalid type: " + typeStr);
               return null;
            } else {
               return type;
            }
         }
      }
   }

   @NonNull
   public List<String> getTimes() {
      return this.getConfig().getStringList("times");
   }

   @NonNull
   public Map<DayOfWeek, List<String>> getScheduledDays() {
      Section section = this.getConfig().getSection("days");
      Map<DayOfWeek, List<String>> dayMap = new HashMap<>();
      if (section == null) {
         return dayMap;
      } else {
         for (String dayStr : section.getRoutesAsStrings(false)) {
            DayOfWeek day = FishUtils.getDay(dayStr);
            if (day != null) {
               dayMap.put(day, section.getStringList(dayStr));
            }
         }

         return dayMap;
      }
   }

   public int getDuration() {
      return this.duration;
   }

   @NonNull
   public List<String> getStartCommands() {
      String route = "start-commands";
      if (!this.getConfig().contains(route)) {
         return List.of();
      } else {
         return this.getConfig().isList(route) ? this.getConfig().getStringList(route) : List.of(this.getConfig().getString(route));
      }
   }

   @NonNull
   public List<DayOfWeek> getBlacklistedDays() {
      return this.getConfig().getStringList("blacklisted-days").stream().map(FishUtils::getDay).filter(Objects::nonNull).toList();
   }

   @NonNull
   public List<IRarity> getAllowedRarities() {
      return this.getConfig().getStringList("allowed-rarities").stream().map(FishManager.getInstance()::getRarity).filter(Objects::nonNull).toList();
   }

   public int getNumberNeeded() {
      return Math.max(1, this.getConfig().getInt("number-needed", 0));
   }

   public boolean shouldBroadcastOnlyRods() {
      return this.getConfig().getBoolean("broadcast-only-rods", true);
   }

   public int getBroadcastRange() {
      return this.getConfig().getInt("broadcast-range", -1);
   }

   @NonNull
   public List<String> getLeaderboardColours() {
      return this.getConfig()
         .getStringList(
            "leaderboard", List.of("<gold>{name}</gold>", "<yellow>{name}</yellow>", "<gray>{name}</gray>", "<gray>{name}</gray>", "<#888888>{name}</#888888>")
         );
   }

   @NonNull
   public List<Long> getAlertTimes() {
      List<String> times = this.getAlertTimesAsStrings();
      List<Long> finalTimes = new ArrayList<>();

      for (String time : times) {
         String[] split = time.split(":");
         if (split.length != 2) {
            this.logger.severe(time + " is not formatted correctly. Use MM:SS.");
         } else {
            try {
               long seconds = Long.parseLong(split[1]);
               seconds += Long.parseLong(split[0]) * 60L;
               finalTimes.add(seconds);
            } catch (NumberFormatException var8) {
               this.logger
                  .severe(
                     "Could not turn " + time + " into an alert time. If you need support, feel free to join the discord server: https://discord.gg/9fRbqWTnHS"
                  );
            }
         }
      }

      return finalTimes;
   }

   @NonNull
   public List<String> getAlertTimesAsStrings() {
      return this.getConfig().getStringList("alerts");
   }

   @NonNull
   public Map<Integer, List<Reward>> getRewards() {
      Section section = this.getConfig().getSection("rewards");
      if (section == null) {
         return Map.of();
      } else {
         Map<Integer, List<Reward>> rewardMap = new HashMap<>();

         for (String positionStr : section.getRoutesAsStrings(false)) {
            Integer position;
            if (positionStr.equalsIgnoreCase("participation")) {
               position = -1;
            } else {
               position = FishUtils.getInteger(positionStr);
            }

            if (position != null) {
               List<Reward> rewards = section.getStringList(positionStr).stream().map(Reward::new).toList();
               rewardMap.put(position, rewards);
            }
         }

         return rewardMap;
      }
   }

   @NonNull
   public Color getBossbarColour() {
      String colour = this.getConfig().getString("bossbar-colour", "GREEN");
      Color color = FishUtils.getEnumValue(Color.class, colour);
      if (color == null) {
         EvenMoreFish.getInstance().getLogger().warning(colour + " is not a valid bossbar colour. Defaulting to GREEN.");
         return Color.GREEN;
      } else {
         return color;
      }
   }

   public boolean shouldShowBossbar() {
      return this.getConfig().getBoolean("show-bossbar", true);
   }

   public EMFSingleMessage getBossbarPrefix() {
      String prefix = this.getConfig().getString("bossbar-prefix", "<green><bold>Fishing Contest: ");
      return EMFSingleMessage.fromString(prefix);
   }

   public int getPlayersNeeded() {
      return Math.max(1, this.getConfig().getInt("minimum-players", 5));
   }

   @Nullable
   public Sound getStartSound() {
      String soundString = this.getConfig().getString("start-sound", "NONE");
      if (soundString.equalsIgnoreCase("NONE")) {
         return null;
      } else {
         Sound sound = SoundSerializer.get().deserialize(soundString, 10.0F, 1.0F);
         if (sound == null) {
            EvenMoreFish.getInstance().getLogger().warning(soundString + " is not a valid sound. Defaulting to NONE.");
         }

         return sound;
      }
   }

   public List<World> getRequiredWorlds() {
      return this.getConfig().getStringList("required-worlds").stream().<World>map(Bukkit::getWorld).filter(Objects::nonNull).toList();
   }

   public boolean isAllowHunting() {
      return this.getConfig().getBoolean("allow-hunting", false);
   }

   public boolean isAllowFishing() {
      return this.getConfig().getBoolean("allow-fishing", true);
   }
}
