package org.lushplugins.lushrewards.module.playtimetracker;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.data.RewardUser;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.libraries.lushlib.utils.SimpleLocation;
import org.lushplugins.lushrewards.module.playtimerewards.PlaytimeRewardsModule;

public class PlaytimeTracker {
   private static final int IDLE_TIME_TO_AFK = 300;
   private final Player player;
   private SimpleLocation lastLocation;
   private boolean afk;
   private int sessionTime;
   private int idleTime;
   private int globalTime;

   public PlaytimeTracker(Player player) {
      RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(player);
      if (rewardUser == null) {
         throw new IllegalStateException("Failed to find reward user for user: " + player.getName() + "(" + player.getUniqueId() + ")");
      } else {
         this.player = player;
         this.afk = false;
         this.sessionTime = 0;
         this.idleTime = 0;
         this.globalTime = rewardUser.getMinutesPlayed();
         this.updateLocation();
      }
   }

   public void tick() {
      if (!this.player.isOnline()) {
         Optional<Module> optionalModule = LushRewards.getInstance().getModule("playtime-tracker");
         if (optionalModule.isPresent() && optionalModule.get() instanceof PlaytimeTrackerModule playtimeTrackerModule) {
            playtimeTrackerModule.stopPlaytimeTracker(this.player.getUniqueId());
         }
      } else {
         if (!LushRewards.getInstance().getConfigManager().shouldPlaytimeTrackerIgnoreAfk()) {
            this.whileActive();
         } else if (this.hasMoved()) {
            this.updateLocation();
            this.whileActive();
         } else {
            this.whileInactive();
         }
      }
   }

   public void whileActive() {
      this.incrementSessionTime();
      if (this.afk) {
         this.idleTime = 0;
         this.afk = false;
      }
   }

   public void whileInactive() {
      this.idleTime++;
      if (!this.afk) {
         if (this.idleTime > 300) {
            this.afk = true;
         } else {
            this.incrementSessionTime();
         }
      }
   }

   public void saveData() {
      LushRewards.getInstance()
         .getDataManager()
         .getOrLoadRewardUser(this.player.getUniqueId(), false)
         .thenAccept(rewardUser -> rewardUser.setMinutesPlayed(this.globalTime));
   }

   private void incrementSessionTime() {
      this.sessionTime++;
      if (this.sessionTime % 60 == 0) {
         this.incrementGlobalTime();
      }
   }

   private void incrementGlobalTime() {
      this.globalTime++;
      if (this.player.hasPermission("lushrewards.use")) {
         for (Module module : LushRewards.getInstance().getModules()) {
            if (this.player.hasPermission("lushrewards.use." + module.getId())
               && module instanceof PlaytimeRewardsModule playtimeRewardsModule
               && playtimeRewardsModule.getRefreshTime() > 0
               && this.globalTime % playtimeRewardsModule.getRefreshTime() == 0) {
               playtimeRewardsModule.claimRewards(this.player);
            }
         }
      }

      if (this.globalTime % 5 == 0) {
         RewardUser rewardUser = LushRewards.getInstance().getDataManager().getRewardUser(this.player);
         if (rewardUser != null) {
            rewardUser.setMinutesPlayed(this.globalTime);
         } else {
            Optional<Module> optionalModule = LushRewards.getInstance().getModule("playtime-tracker");
            if (optionalModule.isPresent() && optionalModule.get() instanceof PlaytimeTrackerModule playtimeTrackerModule) {
               playtimeTrackerModule.stopPlaytimeTracker(this.player.getUniqueId());
            }
         }
      }
   }

   public void updateLocation() {
      this.lastLocation = SimpleLocation.adapt(this.player.getLocation());
   }

   public boolean hasMoved() {
      return !SimpleLocation.adapt(this.player.getLocation()).equals(this.lastLocation);
   }

   public int getIdlePlaytime() {
      return this.idleTime;
   }

   public int getSessionPlaytime() {
      return (int)Math.floor(this.sessionTime / 60.0F);
   }

   public int getTotalSessionPlaytime() {
      return (int)Math.floor((this.sessionTime + this.idleTime) / 60.0F);
   }

   public int getGlobalPlaytime() {
      return this.globalTime;
   }

   public void setGlobalPlaytime(int globalPlaytime) {
      this.globalTime = globalPlaytime;
   }
}
