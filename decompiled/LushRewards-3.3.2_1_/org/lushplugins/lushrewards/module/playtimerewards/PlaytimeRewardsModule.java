package org.lushplugins.lushrewards.module.playtimerewards;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.data.RewardUser;
import org.lushplugins.lushrewards.exceptions.InvalidRewardException;
import org.lushplugins.lushrewards.gui.GuiDisplayer;
import org.lushplugins.lushrewards.gui.GuiFormat;
import org.lushplugins.lushrewards.libraries.lushlib.gui.inventory.Gui;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.module.RewardModule;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushrewards.rewards.collections.RewardCollection;
import org.lushplugins.lushrewards.utils.Debugger;

public class PlaytimeRewardsModule extends RewardModule implements UserDataModule<PlaytimeRewardsModule.UserData>, GuiDisplayer {
   private final ConcurrentHashMap<UUID, PlaytimeRewardsModule.UserData> userDataCache = new ConcurrentHashMap<>();
   private ConcurrentHashMap<Integer, PlaytimeRewardCollection> minutesToReward;
   private PlaytimeRewardsPlaceholder placeholder;
   private int resetPlaytimeAt;
   private int refreshTime;
   private boolean receiveWithDailyRewards;
   private GuiFormat guiFormat;
   private PlaytimeRewardsGui.ScrollType scrollType;

   public PlaytimeRewardsModule(String id, File moduleFile) {
      super(id, moduleFile, true);
   }

   @Override
   public void onEnable() {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(this.moduleFile);
      if (!config.getBoolean("enabled", true)) {
         LushRewards.getInstance().getLogger().info("Module '" + this.id + "' is disabled in it's configuration");
         this.disable();
      } else {
         String goalsSection = config.contains("goals")
            ? "goals"
            : (config.contains("daily-goals") ? "daily-goals" : (config.contains("global-goals") ? "global-goals" : null));
         if (goalsSection == null) {
            LushRewards.getInstance().getLogger().severe("Failed to load rewards, could not find 'goals' section in '" + this.moduleFile.getName() + "'");
            this.disable();
         } else {
            if (config.contains("reset-playtime-at")) {
               this.resetPlaytimeAt = config.getInt("reset-playtime-at");
            } else if (this.id.contains("daily")) {
               this.resetPlaytimeAt = 1;
            } else if (this.id.contains("weekly")) {
               this.resetPlaytimeAt = 7;
            }

            this.refreshTime = config.getInt("refresh-time");
            this.receiveWithDailyRewards = config.getBoolean("give-with-daily-rewards");
            this.setShouldNotify(config.getBoolean("enable-notifications", false));
            String guiTitle = config.getString("gui.title", "&8&lDaily Rewards");
            String templateType = config.getString("gui.template", "DEFAULT").toUpperCase();
            GuiFormat.GuiTemplate guiTemplate = templateType.equals("CUSTOM")
               ? new GuiFormat.GuiTemplate(config.getStringList("gui.format"))
               : GuiFormat.GuiTemplate.valueOf(templateType);
            this.guiFormat = new GuiFormat(guiTitle, guiTemplate);
            this.scrollType = PlaytimeRewardsGui.ScrollType.valueOf(config.getString("gui.scroll-type", "FIXED").toUpperCase());
            ConfigurationSection itemTemplatesSection = config.getConfigurationSection("gui.item-templates");
            if (itemTemplatesSection != null) {
               this.reloadItemTemplates(itemTemplatesSection);
            }

            this.minutesToReward = new ConcurrentHashMap<>();

            for (Map<?, ?> rewardMap : config.getMapList(goalsSection)) {
               PlaytimeRewardCollection rewardCollection;
               try {
                  rewardCollection = PlaytimeRewardCollection.from(rewardMap);
               } catch (InvalidRewardException var11) {
                  var11.printStackTrace();
                  continue;
               }

               int minutes = rewardMap.containsKey("play-minutes") ? (Integer)rewardMap.get("play-minutes") : 60;
               this.minutesToReward.put(minutes, rewardCollection);
            }

            this.placeholder = new PlaytimeRewardsPlaceholder(this.id);
            this.placeholder.register();
            LushRewards.getInstance().getLogger().info("Successfully loaded " + this.minutesToReward.size() + " reward collections from 'goals'");
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            if (!onlinePlayers.isEmpty()) {
               for (Player onlinePlayer : onlinePlayers) {
                  this.getOrLoadUserData(onlinePlayer.getUniqueId(), true);
               }

               LushRewards.getInstance().getLogger().info("Successfully loaded '" + this.id + "' user data for online players");
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.minutesToReward != null) {
         this.minutesToReward.clear();
         this.minutesToReward = null;
      }

      if (this.placeholder != null) {
         this.placeholder.unregister();
         this.placeholder = null;
      }
   }

   public void checkForReset(RewardUser rewardUser, PlaytimeRewardsModule.UserData userData) {
      if (this.resetPlaytimeAt > 0) {
         if (!userData.getStartDate().isAfter(LocalDate.now().minusDays(this.resetPlaytimeAt))) {
            Debugger.sendDebugMessage(
               String.format(
                  "Set start date for %s from %s to %s (Module: %s)",
                  userData.getUniqueId(),
                  userData.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                  LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                  this.getId()
               ),
               Debugger.DebugMode.PLAYTIME
            );
            userData.setStartDate(LocalDate.now());
            Debugger.sendDebugMessage(
               String.format(
                  "Set previous day end playtime for %s from %s to %s (Module: %s)",
                  userData.getUniqueId(),
                  userData.getPreviousDayEndPlaytime(),
                  rewardUser.getMinutesPlayed(),
                  this.getId()
               ),
               Debugger.DebugMode.PLAYTIME
            );
            userData.setPreviousDayEndPlaytime(rewardUser.getMinutesPlayed());
            Debugger.sendDebugMessage(
               String.format(
                  "Set last collected playtime for %s from %s to %s (Module: %s)",
                  userData.getUniqueId(),
                  userData.getLastCollectedPlaytime(),
                  rewardUser.getMinutesPlayed(),
                  this.getId()
               ),
               Debugger.DebugMode.PLAYTIME
            );
            userData.setLastCollectedPlaytime(rewardUser.getMinutesPlayed());
            this.saveUserData(userData);
         }
      }
   }

   public void checkAllOnlineForReset() {
      if (this.resetPlaytimeAt > 0) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(player);
            PlaytimeRewardsModule.UserData userData = this.getUserData(player.getUniqueId());
            if (rewardUser != null && userData != null) {
               this.checkForReset(rewardUser, userData);
            }
         }
      }
   }

   @Override
   public boolean hasClaimableRewards(Player player) {
      return this.hasClaimableRewards(player, null);
   }

   public boolean hasClaimableRewards(Player player, Integer globalPlaytime) {
      RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(player);
      PlaytimeRewardsModule.UserData userData = this.getUserData(player.getUniqueId());
      if (rewardUser != null && userData != null) {
         this.checkForReset(rewardUser, userData);
         globalPlaytime = globalPlaytime != null ? globalPlaytime : rewardUser.getMinutesPlayed();
         int previousDayEnd = userData.getPreviousDayEndPlaytime();
         return !this.getRewardCollectionsInRange(userData.getLastCollectedPlaytime() - previousDayEnd, globalPlaytime - previousDayEnd).isEmpty();
      } else {
         return false;
      }
   }

   @Override
   public boolean claimRewards(Player player) {
      return this.claimRewards(player, null);
   }

   public boolean claimRewards(Player player, Integer globalPlaytime) {
      RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(player);
      PlaytimeRewardsModule.UserData userData = this.getUserData(player.getUniqueId());
      if (rewardUser != null && userData != null) {
         boolean saveUserData = false;
         if (this.resetPlaytimeAt > 0 && userData.getStartDate().isEqual(LocalDate.now().minusDays(this.resetPlaytimeAt))) {
            Debugger.sendDebugMessage(
               String.format(
                  "Set start date for %s from %s to %s (Module: %s)",
                  userData.getUniqueId(),
                  userData.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                  LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                  this.getId()
               ),
               Debugger.DebugMode.PLAYTIME
            );
            userData.setStartDate(LocalDate.now());
            Debugger.sendDebugMessage(
               String.format(
                  "Set previous day end playtime for %s from %s to %s (Module: %s)",
                  userData.getUniqueId(),
                  userData.getPreviousDayEndPlaytime(),
                  userData.getLastCollectedPlaytime(),
                  this.getId()
               ),
               Debugger.DebugMode.PLAYTIME
            );
            userData.setPreviousDayEndPlaytime(userData.getLastCollectedPlaytime());
            saveUserData = true;
         }

         globalPlaytime = globalPlaytime != null ? globalPlaytime : rewardUser.getMinutesPlayed();
         int previousDayEnd = userData.getPreviousDayEndPlaytime();
         int playtime = globalPlaytime - previousDayEnd;
         int lastCollectedPlaytime = Math.max(userData.getLastCollectedPlaytime() - previousDayEnd, 0);
         int playtimeSinceLastCollected = playtime - lastCollectedPlaytime;
         HashMap<PlaytimeRewardCollection, Integer> rewards = this.getRewardCollectionsInRange(lastCollectedPlaytime, playtime);
         if (rewards.isEmpty()) {
            if (saveUserData) {
               this.saveUserData(userData);
            }

            return false;
         } else {
            rewards.forEach((rewardCollection, amount) -> {
               for (int i = 0; i < amount; i++) {
                  rewardCollection.giveAll(player);
               }
            });
            String rewardMessage = LushRewards.getInstance().getConfigManager().getMessage("playtime-reward-given");
            if (rewardMessage.isBlank()) {
               LushRewards.getInstance()
                  .getLogger()
                  .warning(
                     "Deprecated: The 'daily-playtime-reward-given' message has been renamed to 'playtime-reward-given', make sure to adjust the messages section of your config."
                  );
               rewardMessage = LushRewards.getInstance().getConfigManager().getMessage("daily-playtime-reward-given");
            }

            ChatColorHandler.sendMessage(
               player,
               rewardMessage.replace("%minutes%", String.valueOf(playtimeSinceLastCollected))
                  .replace("%hours%", String.valueOf((int)Math.floor(playtimeSinceLastCollected / 60.0)))
                  .replace("%total_minutes%", String.valueOf(playtime))
                  .replace("%total_hours%", String.valueOf((int)Math.floor(playtime / 60.0)))
            );
            userData.setLastCollectedPlaytime(globalPlaytime);
            this.saveUserData(userData);
            return true;
         }
      } else {
         ChatColorHandler.sendMessage(player, "&#ff6969Failed to collect your reward user data, try relogging. If this continues inform an administrator");
         LushRewards.getInstance()
            .getLogger()
            .warning(
               "Failed to collect reward user data for '"
                  + player.getName()
                  + "', reward user was '"
                  + (rewardUser != null ? "found" : "not found")
                  + "', user data was '"
                  + (userData != null ? "found" : "not found")
                  + "'"
            );
         return false;
      }
   }

   public int getResetPlaytimeAt() {
      return this.resetPlaytimeAt;
   }

   public int getRefreshTime() {
      return this.refreshTime;
   }

   public boolean shouldReceiveWithDailyRewards() {
      return this.receiveWithDailyRewards;
   }

   public Collection<PlaytimeRewardCollection> getRewards() {
      return this.minutesToReward.values();
   }

   @Nullable
   public RewardCollection getRewardCollection(int minutes) {
      return this.minutesToReward.get(minutes);
   }

   @NotNull
   public HashMap<PlaytimeRewardCollection, Integer> getRewardCollectionsInRange(int lower, int upper) {
      HashMap<PlaytimeRewardCollection, Integer> output = new HashMap<>();
      this.minutesToReward.values().forEach(rewardCollection -> {
         int amount = rewardCollection.amountAvailableAt(lower, upper);
         if (amount > 0) {
            output.put(rewardCollection, amount);
         }
      });
      return output;
   }

   @NotNull
   private List<Integer> getKeysInRange(int lower, int upper) {
      return this.minutesToReward.keySet().stream().filter(key -> key > lower && key <= upper).toList();
   }

   @Nullable
   public Integer getShortestRepeatFrequency() {
      return this.getShortestRepeatFrequency(-1);
   }

   @Nullable
   public Integer getShortestRepeatFrequency(int playtime) {
      Integer shortestFrequency = null;

      for (PlaytimeRewardCollection reward : this.minutesToReward.values()) {
         if (playtime < 0 || reward.getRepeatsUntil() >= playtime) {
            int frequency = reward.getRepeatFrequency();
            if (frequency > 0 && (shortestFrequency == null || frequency < shortestFrequency)) {
               shortestFrequency = frequency;
            }
         }
      }

      return shortestFrequency;
   }

   public GuiFormat getGuiFormat() {
      return this.guiFormat;
   }

   @Override
   public Gui getGui(Player player) {
      return new PlaytimeRewardsGui(this, player);
   }

   public PlaytimeRewardsGui.ScrollType getScrollType() {
      return this.scrollType;
   }

   public PlaytimeRewardsModule.UserData getDefaultData(UUID uuid) {
      return new PlaytimeRewardsModule.UserData(uuid, this.id, 0, LocalDate.now(), 0);
   }

   public PlaytimeRewardsModule.UserData getUserData(UUID uuid) {
      return this.userDataCache.get(uuid);
   }

   @Override
   public void cacheUserData(UUID uuid, UserDataModule.UserData userData) {
      if (userData instanceof PlaytimeRewardsModule.UserData data) {
         this.userDataCache.put(uuid, data);
      }
   }

   @Override
   public void uncacheUserData(UUID uuid) {
      this.userDataCache.remove(uuid);
   }

   @Override
   public Class<PlaytimeRewardsModule.UserData> getUserDataClass() {
      return PlaytimeRewardsModule.UserData.class;
   }

   public static class UserData extends UserDataModule.UserData {
      private int lastCollectedPlaytime;
      private LocalDate startDate;
      private int previousDayEndPlaytime;

      public UserData(UUID uuid, String id, int lastCollectedPlaytime, @NotNull LocalDate startDate, int previousDayEndPlaytime) {
         super(uuid, id);
         this.lastCollectedPlaytime = lastCollectedPlaytime;
         this.startDate = startDate;
         this.previousDayEndPlaytime = previousDayEndPlaytime;
      }

      public int getLastCollectedPlaytime() {
         return this.lastCollectedPlaytime;
      }

      public void setLastCollectedPlaytime(int lastCollectedPlaytime) {
         this.lastCollectedPlaytime = lastCollectedPlaytime;
      }

      @NotNull
      public LocalDate getStartDate() {
         return this.startDate;
      }

      public void setStartDate(@NotNull LocalDate startDate) {
         this.startDate = startDate;
      }

      public int getPreviousDayEndPlaytime() {
         return this.previousDayEndPlaytime;
      }

      public void setPreviousDayEndPlaytime(int previousDayEndPlaytime) {
         this.previousDayEndPlaytime = previousDayEndPlaytime;
      }
   }
}
