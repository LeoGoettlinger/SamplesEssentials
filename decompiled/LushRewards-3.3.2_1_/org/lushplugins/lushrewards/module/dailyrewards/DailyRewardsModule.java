package org.lushplugins.lushrewards.module.dailyrewards;

import java.io.File;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.libraries.lushlib.utils.StringUtils;
import org.lushplugins.lushrewards.module.RewardModule;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTrackerModule;
import org.lushplugins.lushrewards.rewards.collections.RewardDay;
import org.lushplugins.lushrewards.utils.Debugger;

public class DailyRewardsModule extends RewardModule implements UserDataModule<DailyRewardsModule.UserData>, GuiDisplayer {
   private final ConcurrentHashMap<UUID, DailyRewardsModule.UserData> userDataCache = new ConcurrentHashMap<>();
   private HashSet<DailyRewardCollection> rewards;
   private DailyRewardsPlaceholder placeholder;
   private int resetDaysAt;
   private DailyRewardsModule.RewardMode rewardMode;
   private boolean allowRewardsStacking;
   private boolean streakBypass;
   private Sound defaultRedeemSound;
   private String upcomingCategory;
   private boolean dateAsAmount;
   private String rewardPlaceholderClaimed;
   private String rewardPlaceholderUnclaimed;
   private DailyRewardsGui.ScrollType scrollType;
   private GuiFormat guiFormat;
   private Integer requiredPlaytime;

   public DailyRewardsModule(String id, File moduleFile) {
      super(id, moduleFile);
   }

   @Override
   public void onEnable() {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(this.moduleFile);
      if (!config.getBoolean("enabled", true)) {
         LushRewards.getInstance().getLogger().info("Module '" + this.id + "' is disabled in it's configuration");
         this.disable();
      } else {
         ConfigurationSection configurationSection = config.getConfigurationSection("daily-rewards");
         if (configurationSection == null) {
            LushRewards.getInstance()
               .getLogger()
               .severe("Failed to load rewards, could not find 'daily-rewards' section in '" + this.moduleFile.getName() + "'");
            this.disable();
         } else {
            this.resetDaysAt = config.getInt("reset-days-at", -1);
            this.rewardMode = StringUtils.getEnum(
                  config.getString("reward-mode", config.getBoolean("streak-mode") ? "streak" : "default"), DailyRewardsModule.RewardMode.class
               )
               .orElse(DailyRewardsModule.RewardMode.DEFAULT);
            this.allowRewardsStacking = config.getBoolean("allow-reward-stacking", true);
            this.streakBypass = config.getBoolean("streak-bypass");
            this.defaultRedeemSound = (Sound)StringUtils.getEnum(config.getString("default-redeem-sound", "none"), Sound.class).orElse(null);
            this.setShouldNotify(config.getBoolean("enable-notifications", true));
            this.upcomingCategory = config.getString("upcoming-category");
            String guiTitle = config.getString("gui.title", "&8&lDaily Rewards");
            this.dateAsAmount = config.getBoolean("gui.date-as-amount", false);
            this.rewardPlaceholderClaimed = config.getString("reward-placeholders.claimed", "true");
            this.rewardPlaceholderUnclaimed = config.getString("reward-placeholders.unclaimed", "false");
            this.scrollType = DailyRewardsGui.ScrollType.valueOf(config.getString("gui.scroll-type", "MONTH").toUpperCase());
            String templateType = config.getString("gui.template", "DEFAULT").toUpperCase();
            GuiFormat.GuiTemplate guiTemplate = templateType.equals("CUSTOM")
               ? new GuiFormat.GuiTemplate(config.getStringList("gui.format"))
               : GuiFormat.GuiTemplate.valueOf(templateType);
            this.guiFormat = new GuiFormat(guiTitle, guiTemplate);
            this.requiredPlaytime = config.contains("required-playtime") ? config.getInt("required-playtime") : null;
            ConfigurationSection itemTemplatesSection = config.getConfigurationSection("gui.item-templates");
            if (itemTemplatesSection != null) {
               this.reloadItemTemplates(itemTemplatesSection);
            }

            this.rewards = new HashSet<>();
            LocalDate today = LocalDate.now();
            Iterator onlinePlayers = configurationSection.getValues(false).values().iterator();

            while (true) {
               DailyRewardCollection dailyRewardCollection;
               while (true) {
                  if (!onlinePlayers.hasNext()) {
                     this.placeholder = new DailyRewardsPlaceholder(this.id);
                     this.placeholder.register();
                     LushRewards.getInstance()
                        .getLogger()
                        .info("Successfully loaded " + this.rewards.size() + " reward collections from '" + configurationSection.getCurrentPath() + "'");
                     Collection<? extends Player> onlinePlayersx = Bukkit.getOnlinePlayers();
                     if (!onlinePlayersx.isEmpty()) {
                        for (Player onlinePlayer : onlinePlayersx) {
                           this.getOrLoadUserData(onlinePlayer.getUniqueId(), true);
                        }

                        LushRewards.getInstance().getLogger().info("Successfully loaded '" + this.id + "' user data for online players");
                     }

                     return;
                  }

                  if (onlinePlayers.next() instanceof ConfigurationSection rewardSection) {
                     try {
                        dailyRewardCollection = DailyRewardCollection.from(rewardSection);
                        break;
                     } catch (InvalidRewardException var15) {
                        var15.printStackTrace();
                     }
                  }
               }

               if (LushRewards.getInstance().getConfigManager().isPerformanceModeEnabled() && dailyRewardCollection.getRewardDate() != null) {
                  LocalDate lowestDate;
                  LocalDate highestDate;
                  switch (this.scrollType) {
                     case DAY:
                        lowestDate = LocalDate.now();
                        highestDate = today.plusDays(guiTemplate.countChar('R') - 1);
                        break;
                     case MONTH:
                        lowestDate = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
                        highestDate = LocalDate.of(today.getYear(), today.getMonthValue(), today.getMonth().length(today.isLeapYear()));
                        break;
                     default:
                        int rewardDisplayCount = guiTemplate.countChar('R');
                        lowestDate = today.minusDays(rewardDisplayCount - 1);
                        highestDate = today.plusDays(rewardDisplayCount - 1);
                  }

                  for (LocalDate dateIndex = lowestDate; !dateIndex.isAfter(highestDate); dateIndex = dateIndex.plusDays(1L)) {
                     if (dailyRewardCollection.isAvailableOn(dateIndex)) {
                        this.rewards.add(dailyRewardCollection);
                        break;
                     }
                  }
               } else {
                  this.rewards.add(dailyRewardCollection);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.rewards != null) {
         this.rewards.clear();
         this.rewards = null;
      }

      if (this.placeholder != null) {
         this.placeholder.unregister();
         this.placeholder = null;
      }

      this.guiFormat = null;
   }

   public boolean meetsRequiredPlaytime(Player player) {
      if (this.requiredPlaytime == null) {
         return true;
      } else {
         Optional<Module> optionalModule = LushRewards.getInstance().getModule("playtime-tracker");
         return optionalModule.isPresent() && optionalModule.get() instanceof PlaytimeTrackerModule module
            ? module.getPlaytimeTracker(player.getUniqueId()).getTotalSessionPlaytime() > this.requiredPlaytime
            : true;
      }
   }

   @Override
   public boolean hasClaimableRewards(Player player) {
      return this.userDataCache.containsKey(player.getUniqueId())
         && !this.getUserData(player.getUniqueId()).hasCollectedToday()
         && this.meetsRequiredPlaytime(player);
   }

   @Override
   public boolean claimRewards(Player player) {
      if (!this.meetsRequiredPlaytime(player)) {
         ChatColorHandler.sendMessage(player, "&#ff6969You must have been online for " + this.requiredPlaytime + " minutes to claim these rewards");
         return false;
      } else {
         RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(player);
         DailyRewardsModule.UserData userData = this.getUserData(player.getUniqueId());
         if (rewardUser == null || userData == null) {
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
         } else if (userData.hasCollectedToday()) {
            return false;
         } else {
            LocalDate lastCollectedDate = userData.getLastCollectedDate();
            boolean missedDay = lastCollectedDate == null
               || lastCollectedDate.isBefore(LocalDate.now().minusDays(1L)) && !lastCollectedDate.isEqual(LocalDate.of(1971, 10, 1));
            if (missedDay && !this.streakBypass) {
               userData.setStreak(1);
            } else {
               userData.incrementStreak();
            }

            userData.setLastCollectedDate(LocalDate.now());
            userData.addCollectedDay(userData.getDayNum());
            this.saveUserData(userData)
               .thenAccept(
                  success -> {
                     if (!success) {
                        LushRewards.getInstance()
                           .getLogger()
                           .severe("Something went wrong when saving data for '" + player.getName() + "' (" + player.getUniqueId() + ")");
                     } else {
                        RewardDay rewardDay = this.getRewardDay(LocalDate.now(), userData.getDayNum());
                        DailyRewardCollection priorityReward = rewardDay.getHighestPriorityRewardCollection();
                        Debugger.sendDebugMessage("Attempting to send daily rewards to " + player.getName(), Debugger.DebugMode.DAILY);
                        if (this.shouldStackRewards()) {
                           rewardDay.giveAllRewards(player);
                        } else {
                           priorityReward.giveAll(player);
                        }

                        Debugger.sendDebugMessage("Successfully gave rewards to " + player.getName(), Debugger.DebugMode.DAILY);
                        ChatColorHandler.sendMessage(player, LushRewards.getInstance().getConfigManager().getMessage("daily-reward-given"));
                        player.playSound(
                           player.getLocation(), priorityReward.getSound() != null ? priorityReward.getSound() : this.getDefaultRedeemSound(), 1.0F, 1.0F
                        );
                     }
                  }
               );
            return true;
         }
      }
   }

   @Override
   public boolean requiresPlaytimeTracker() {
      return this.requiredPlaytime != null;
   }

   public void checkRewardDay(DailyRewardsModule.UserData userData) {
      LocalDate lastJoinDate = userData.getLastJoinDate();
      if (lastJoinDate == null) {
         userData.setLastJoinDate(LocalDate.now());
         this.saveUserData(userData);
      } else if (!lastJoinDate.isEqual(LocalDate.now())) {
         LocalDate lastCollectedDate = userData.getLastCollectedDate();
         boolean missedDay = lastCollectedDate == null
            || lastCollectedDate.isBefore(LocalDate.now().minusDays(1L)) && !lastCollectedDate.isEqual(LocalDate.of(1971, 10, 1));
         switch (this.getRewardMode()) {
            case STREAK:
               if (missedDay) {
                  userData.setDayNum(1);
                  userData.setStreak(1);
                  userData.clearCollectedDays();
               } else {
                  userData.incrementDayNum();
               }
               break;
            case ON_CLAIM_ONLY:
               if (userData.hasCollectedDay(userData.getDayNum()) && !userData.hasCollectedToday()) {
                  userData.incrementDayNum();
               }
               break;
            case ONLINE_ONLY:
               userData.incrementDayNum();
               break;
            case DEFAULT:
               userData.setDayNum((int)(LocalDate.now().toEpochDay() - userData.getStartDate().toEpochDay()) + 1);
         }

         if (missedDay && !this.streakBypass) {
            userData.setStreak(1);
         }

         int resetDay = this.getResetDay();
         if (resetDay > 0 && userData.getDayNum() > resetDay) {
            userData.setStartDate(LocalDate.now());
            userData.setDayNum(1);
            userData.clearCollectedDays();
         }

         userData.setLastJoinDate(LocalDate.now());
         this.saveUserData(userData);
      }
   }

   @NotNull
   public Collection<DailyRewardCollection> getDayNumRewards(int day) {
      return this.rewards.stream().filter(rewardCollection -> rewardCollection.isAvailableOn(day)).toList();
   }

   @NotNull
   public Collection<DailyRewardCollection> getDateRewards(LocalDate date) {
      return this.rewards.stream().filter(rewardCollection -> rewardCollection.isAvailableOn(date)).toList();
   }

   public RewardDay getRewardDay(LocalDate date, int streakDay) {
      RewardDay rewardDay = new RewardDay();
      rewardDay.addCollections(this.getDateRewards(date));
      rewardDay.addCollections(this.getDayNumRewards(streakDay));
      return rewardDay;
   }

   public Optional<DailyRewardCollection> findNextRewardFromCategory(int day, LocalDate date, String category) {
      return this.rewards
         .stream()
         .filter(
            reward -> reward.getCategory().equalsIgnoreCase(category)
               && (
                  reward.getRewardDayNum() != null && reward.getRewardDayNum() >= day
                     || reward.getRewardDate() != null && !reward.getRewardDate().isBefore(date)
               )
         )
         .min((reward1, reward2) -> {
            LocalDate date1 = reward1.getRewardDate();
            LocalDate date2 = reward2.getRewardDate();
            if (date1 == null) {
               Integer dayNum1 = reward1.getRewardDayNum();
               if (dayNum1 != null) {
                  date1 = date.plusDays(dayNum1 - day);
               }
            }

            if (date2 == null) {
               Integer dayNum2 = reward2.getRewardDayNum();
               if (dayNum2 != null) {
                  date2 = date.plusDays(dayNum2 - day);
               }
            }

            if (date1 != null && date2 != null) {
               return date1.compareTo((ChronoLocalDate)date2);
            } else {
               return date1 == null && date2 == null ? 0 : (date1 == null ? -1 : 1);
            }
         });
   }

   public int getResetDay() {
      return this.resetDaysAt;
   }

   public DailyRewardsModule.RewardMode getRewardMode() {
      return this.rewardMode;
   }

   public boolean shouldStackRewards() {
      return this.allowRewardsStacking;
   }

   public Sound getDefaultRedeemSound() {
      return this.defaultRedeemSound;
   }

   public String getUpcomingCategory() {
      return this.upcomingCategory;
   }

   public boolean showDateAsAmount() {
      return this.dateAsAmount;
   }

   public String getRewardPlaceholderClaimed() {
      return this.rewardPlaceholderClaimed;
   }

   public String getRewardPlaceholderUnclaimed() {
      return this.rewardPlaceholderUnclaimed;
   }

   public DailyRewardsGui.ScrollType getScrollType() {
      return this.scrollType;
   }

   public GuiFormat getGuiFormat() {
      return this.guiFormat;
   }

   @Override
   public Gui getGui(Player player) {
      return new DailyRewardsGui(this, player);
   }

   public DailyRewardsModule.UserData getDefaultData(UUID uuid) {
      return new DailyRewardsModule.UserData(uuid, this.id, null, 1, 0, 0, LocalDate.now(), null, new HashSet<>());
   }

   public DailyRewardsModule.UserData getUserData(UUID uuid) {
      return this.userDataCache.get(uuid);
   }

   @Override
   public CompletableFuture<DailyRewardsModule.UserData> getOrLoadUserData(UUID uuid, boolean cacheUser) {
      CompletableFuture<DailyRewardsModule.UserData> future = LushRewards.getInstance().getDataManager().getOrLoadUserData(uuid, this, cacheUser);
      if (cacheUser) {
         future.thenAccept(this::checkRewardDay);
      }

      return future;
   }

   @Override
   public CompletableFuture<DailyRewardsModule.UserData> loadUserData(UUID uuid, boolean cacheUser) {
      CompletableFuture<DailyRewardsModule.UserData> future = LushRewards.getInstance().getDataManager().loadUserData(uuid, this, cacheUser);
      if (cacheUser) {
         future.thenAccept(this::checkRewardDay);
      }

      return future;
   }

   @Override
   public void cacheUserData(UUID uuid, UserDataModule.UserData userData) {
      if (userData instanceof DailyRewardsModule.UserData data) {
         this.userDataCache.put(uuid, data);
      }
   }

   @Override
   public void uncacheUserData(UUID uuid) {
      this.userDataCache.remove(uuid);
   }

   @Override
   public Class<DailyRewardsModule.UserData> getUserDataClass() {
      return DailyRewardsModule.UserData.class;
   }

   public static enum RewardMode {
      DEFAULT,
      STREAK,
      ON_CLAIM_ONLY,
      ONLINE_ONLY;
   }

   public static class UserData extends UserDataModule.UserData {
      private LocalDate startDate;
      private LocalDate lastJoinDate;
      private LocalDate lastCollectedDate;
      private final HashSet<Integer> collectedDays;
      private int dayNum;
      private int streak;
      private int highestStreak;

      public UserData(
         UUID uuid,
         String moduleId,
         LocalDate lastJoinDate,
         int dayNum,
         int streak,
         int highestStreak,
         LocalDate startDate,
         LocalDate lastCollectedDate,
         HashSet<Integer> collectedDays
      ) {
         super(uuid, moduleId);
         this.startDate = startDate;
         this.lastJoinDate = lastJoinDate;
         this.lastCollectedDate = lastCollectedDate;
         this.collectedDays = collectedDays;
         this.dayNum = dayNum;
         this.streak = streak;
         this.highestStreak = highestStreak;
      }

      public LocalDate getLastJoinDate() {
         return this.lastJoinDate;
      }

      public void setLastJoinDate(LocalDate lastJoinDate) {
         this.lastJoinDate = lastJoinDate;
      }

      public int getDayNum() {
         return this.dayNum;
      }

      public void setDayNum(int dayNum) {
         this.dayNum = dayNum;
      }

      public void incrementDayNum() {
         this.dayNum++;
      }

      public int getStreak() {
         return this.streak;
      }

      public void setStreak(int streak) {
         this.streak = streak;
         if (streak > this.highestStreak) {
            this.highestStreak = streak;
         }
      }

      public void incrementStreak() {
         this.setStreak(this.streak + 1);
      }

      public int getHighestStreak() {
         return this.highestStreak;
      }

      public LocalDate getExpectedDateOnDayNum(int dayNum) {
         return LocalDate.now().plusDays(dayNum - this.getDayNum());
      }

      public LocalDate getStartDate() {
         return this.startDate;
      }

      public void setStartDate(LocalDate date) {
         this.startDate = date;
      }

      @Nullable
      public LocalDate getLastCollectedDate() {
         return this.lastCollectedDate;
      }

      public void setLastCollectedDate(LocalDate date) {
         this.lastCollectedDate = date;
      }

      public boolean hasCollectedToday() {
         return this.lastCollectedDate != null && this.lastCollectedDate.isEqual(LocalDate.now());
      }

      public HashSet<Integer> getCollectedDays() {
         return this.collectedDays;
      }

      public boolean hasCollectedDay(int dayNum) {
         return this.collectedDays.contains(dayNum);
      }

      public void addCollectedDay(int dayNum) {
         this.collectedDays.add(dayNum);
      }

      public void clearCollectedDays() {
         this.collectedDays.clear();
      }
   }
}
