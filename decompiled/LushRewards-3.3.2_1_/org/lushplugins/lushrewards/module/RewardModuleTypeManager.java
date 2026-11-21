package org.lushplugins.lushrewards.module;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.manager.Manager;
import org.lushplugins.lushrewards.module.dailyrewards.DailyRewardsModule;
import org.lushplugins.lushrewards.module.playtimerewards.PlaytimeRewardsModule;

public class RewardModuleTypeManager extends Manager {
   private ConcurrentHashMap<String, RewardModule.Constructor<? extends RewardModule>> rewardModuleTypes;

   @Override
   public void onEnable() {
      this.rewardModuleTypes = new ConcurrentHashMap<>();
      this.register("daily-rewards", DailyRewardsModule::new);
      this.register("playtime-rewards", PlaytimeRewardsModule::new);
   }

   @Override
   public void onDisable() {
      if (this.rewardModuleTypes != null) {
         this.rewardModuleTypes.clear();
         this.rewardModuleTypes = null;
      }
   }

   public boolean isRegistered(String type) {
      return this.rewardModuleTypes.containsKey(type);
   }

   public void register(String type, RewardModule.Constructor<? extends RewardModule> constructor) {
      this.rewardModuleTypes.put(type, constructor);
   }

   public void unregister(String type) {
      this.rewardModuleTypes.remove(type);
   }

   @Nullable
   public RewardModule.Constructor<? extends RewardModule> getConstructor(String type) {
      return this.rewardModuleTypes.get(type);
   }

   public RewardModule loadModuleType(@NotNull String type, @NotNull String moduleId, @NotNull File moduleFile) {
      return this.rewardModuleTypes.containsKey(type) ? this.rewardModuleTypes.get(type).build(moduleId, moduleFile) : null;
   }
}
