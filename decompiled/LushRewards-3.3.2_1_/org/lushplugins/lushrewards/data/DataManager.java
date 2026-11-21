package org.lushplugins.lushrewards.data;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.manager.Manager;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushrewards.module.playtimerewards.PlaytimeRewardsModule;
import org.lushplugins.lushrewards.storage.StorageManager;

public class DataManager extends Manager {
   private StorageManager storageManager;
   private final ConcurrentHashMap<UUID, RewardUser> rewardUsersCache = new ConcurrentHashMap<>();

   @Override
   public void onEnable() {
      this.storageManager = new StorageManager();
      Bukkit.getOnlinePlayers()
         .forEach(player -> this.getOrLoadRewardUser(player.getUniqueId()).thenAccept(rewardUser -> rewardUser.setUsername(player.getName())));
   }

   @Override
   public void onDisable() {
      if (this.storageManager != null) {
         this.saveCachedRewardUsers();
         this.storageManager.disable();
         this.storageManager = null;
      }
   }

   @Nullable
   public RewardUser getRewardUser(@NotNull Player player) {
      return this.getRewardUser(player.getUniqueId());
   }

   @Nullable
   public RewardUser getRewardUser(@NotNull UUID uuid) {
      return this.rewardUsersCache.get(uuid);
   }

   public CompletableFuture<RewardUser> getOrLoadRewardUser(UUID uuid) {
      return this.getOrLoadRewardUser(uuid, true);
   }

   public CompletableFuture<RewardUser> getOrLoadRewardUser(UUID uuid, boolean cacheUser) {
      return this.rewardUsersCache.containsKey(uuid)
         ? CompletableFuture.completedFuture(this.rewardUsersCache.get(uuid))
         : this.loadRewardUser(uuid, cacheUser);
   }

   public CompletableFuture<RewardUser> loadRewardUser(@NotNull UUID uuid) {
      return this.loadRewardUser(uuid, true);
   }

   public CompletableFuture<RewardUser> loadRewardUser(@NotNull UUID uuid, boolean cacheUser) {
      CompletableFuture<RewardUser> future = new CompletableFuture<>();
      this.loadUserData(uuid, null, RewardUser.class).thenAccept(rewardUser -> {
         if (rewardUser != null && cacheUser) {
            this.rewardUsersCache.put(uuid, rewardUser);
         }

         future.complete(rewardUser);
      });
      return future;
   }

   public void unloadRewardUser(UUID uuid) {
      this.rewardUsersCache.remove(uuid);
   }

   public void reloadRewardUsers(boolean save) {
      this.rewardUsersCache.forEach((uuid, rewardUser) -> {
         if (save) {
            this.saveRewardUser(rewardUser);
         }

         this.unloadRewardUser(uuid);
         this.loadRewardUser(uuid);
      });
   }

   public void saveCachedRewardUsers() {
      this.rewardUsersCache.values().forEach(this::saveRewardUser);
   }

   public CompletableFuture<Boolean> saveRewardUser(RewardUser rewardUser) {
      return this.saveUserData(rewardUser);
   }

   public void saveRewardUser(Player player) {
      RewardUser rewardUser = this.getRewardUser(player);
      if (rewardUser != null) {
         this.saveRewardUser(rewardUser);
      }
   }

   public <T extends UserDataModule.UserData> CompletableFuture<T> getOrLoadUserData(UUID uuid, UserDataModule<T> module) {
      return this.getOrLoadUserData(uuid, module, true);
   }

   public <T extends UserDataModule.UserData> CompletableFuture<T> getOrLoadUserData(UUID uuid, UserDataModule<T> module, boolean cacheUser) {
      T userData = module.getUserData(uuid);
      return userData != null ? CompletableFuture.completedFuture(userData) : this.loadUserData(uuid, module, cacheUser);
   }

   public <T extends UserDataModule.UserData> CompletableFuture<T> loadUserData(UUID uuid, UserDataModule<T> module) {
      return this.loadUserData(uuid, module, true);
   }

   public <T extends UserDataModule.UserData> CompletableFuture<T> loadUserData(UUID uuid, UserDataModule<T> module, boolean cacheUser) {
      CompletableFuture<T> future = new CompletableFuture<>();
      this.loadUserData(uuid, module.getId(), module.getUserDataClass()).thenAccept(userData -> {
         if (userData != null && cacheUser) {
            module.cacheUserData(uuid, userData);
         }

         future.complete((T)userData);
      });
      return future;
   }

   public <T extends UserDataModule.UserData> CompletableFuture<T> loadUserData(@NotNull UUID uuid, String moduleId, Class<T> dataClass) {
      CompletableFuture<T> future = new CompletableFuture<>();
      this.storageManager
         .loadModuleUserData(uuid, moduleId)
         .orTimeout(15L, TimeUnit.SECONDS)
         .whenComplete(
            (json, exception) -> {
               if (exception != null) {
                  LushRewards.getInstance().log(Level.WARNING, "Caught error when parsing data:", exception);
                  future.complete(null);
               } else if (json == null) {
                  LushRewards.getInstance()
                     .getLogger()
                     .info("No storage data found for '" + uuid + "' for module '" + (moduleId != null ? moduleId : "main") + "', creating default data!");
                  if (moduleId != null) {
                     UserDataModule<?> module = (UserDataModule<?>)LushRewards.getInstance().getModule(moduleId).orElse(null);
                     if (module != null) {
                        T userData = dataClass.cast(module.getDefaultData(uuid));
                        this.saveUserData(userData).thenAccept(ignored -> future.complete(userData));
                     } else {
                        future.complete(null);
                     }
                  } else if (dataClass.isAssignableFrom(RewardUser.class)) {
                     T userData = dataClass.cast(new RewardUser(uuid, null, 0));
                     this.saveUserData(userData).thenAccept(ignored -> future.complete(userData));
                  } else {
                     future.complete(null);
                  }
               } else {
                  try {
                     json.addProperty("uuid", uuid.toString());
                     json.addProperty("moduleId", moduleId);
                     T userData = (T)LushRewards.getInstance().getGson().fromJson(json, dataClass);
                     if (userData == null) {
                        future.complete(null);
                        return;
                     }

                     if (userData instanceof PlaytimeRewardsModule.UserData playtimeUserData) {
                        PlaytimeRewardsModule module = (PlaytimeRewardsModule)LushRewards.getInstance().getModule(moduleId).orElse(null);
                        if (module != null) {
                           int resetPlaytimeAt = module.getResetPlaytimeAt();
                           if (resetPlaytimeAt > 0 && !playtimeUserData.getStartDate().isAfter(LocalDate.now().minusDays(resetPlaytimeAt))) {
                              playtimeUserData.setStartDate(LocalDate.now());
                              playtimeUserData.setPreviousDayEndPlaytime(playtimeUserData.getLastCollectedPlaytime());
                              this.saveUserData(userData);
                           }
                        }
                     }

                     future.complete(userData);
                  } catch (Throwable var11) {
                     LushRewards.getInstance().log(Level.WARNING, "Caught error when parsing user data:", var11);
                     future.complete(null);
                  }
               }
            }
         );
      return future;
   }

   public CompletableFuture<Boolean> saveUserData(UserDataModule.UserData userData) {
      return CompletableFuture.<CompletableFuture<Void>>supplyAsync(() -> this.storageManager.saveModuleUserData(userData))
         .orTimeout(30L, TimeUnit.SECONDS)
         .handle((storageData, exception) -> {
            if (exception != null) {
               LushRewards.getInstance().log(Level.WARNING, "Caught error when saving data:", exception);
               return false;
            } else {
               return storageData != null;
            }
         });
   }

   public void loadModulesUserData(UUID uuid) {
      LushRewards.getInstance().getEnabledRewardModules().forEach(module -> {
         if (module instanceof UserDataModule<?> userDataModule) {
            userDataModule.getOrLoadUserData(uuid, true);
         }
      });
   }

   public void unloadModulesUserData(UUID uuid) {
      LushRewards.getInstance().getRewardModules().forEach(module -> {
         if (module instanceof UserDataModule<?> userDataModule) {
            userDataModule.uncacheUserData(uuid);
         }
      });
   }

   public void saveModulesUserData(UUID uuid) {
      LushRewards.getInstance().getEnabledRewardModules().forEach(module -> {
         if (module instanceof UserDataModule<?> userDataModule) {
            this.saveModuleUserData(uuid, userDataModule);
         }
      });
   }

   public void saveModuleUserData(UUID uuid, String moduleId) {
      LushRewards.getInstance().getModule(moduleId).ifPresent(module -> {
         if (module instanceof UserDataModule<?> userDataModule) {
            this.saveModuleUserData(uuid, userDataModule);
         }
      });
   }

   public CompletableFuture<Boolean> saveModuleUserData(UUID uuid, UserDataModule<?> userDataModule) {
      UserDataModule.UserData userData = userDataModule.getUserData(uuid);
      return userData != null ? this.saveUserData(userData) : CompletableFuture.completedFuture(true);
   }
}
