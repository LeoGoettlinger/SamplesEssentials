package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.data.manager.UserManager;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.fishing.items.FishManager;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public abstract class UniqueFishCaughtProgressPlaceholder implements EMFPlaceholder {
   private final String prefix;
   private final int prefixLength;

   protected UniqueFishCaughtProgressPlaceholder(@NonNull String prefix) {
      this.prefix = prefix;
      this.prefixLength = prefix.length();
   }

   @Override
   public boolean shouldProcess(@NonNull String identifier) {
      return identifier.startsWith(this.prefix);
   }

   protected int getPrefixLength() {
      return this.prefixLength;
   }

   @Nullable
   protected UUID resolveTarget(@Nullable OfflinePlayer player, @NonNull String target, @NonNull String identifier) {
      UUID uuid = this.fetchPlayerOrUUIDString(player, target);
      if (uuid == null) {
         if (target.equalsIgnoreCase("player")) {
            this.debug("Placeholder %s could not resolve player context for identifier: %s".formatted(this.getClass().getSimpleName(), identifier));
         } else {
            this.debug("Placeholder %s received an invalid UUID target '%s' for identifier: %s".formatted(this.getClass().getSimpleName(), target, identifier));
         }
      }

      return uuid;
   }

   @NonNull
   protected String formatProgress(int caught, int total) {
      return caught + "/" + total;
   }

   @NonNull
   protected String formatPercent(int caught, int total) {
      if (total <= 0) {
         return "0%";
      } else {
         double percent = caught * 100.0 / total;
         return percent == Math.rint(percent) ? (int)percent + "%" : String.format(Locale.ROOT, "%.1f%%", percent);
      }
   }

   protected int remainingFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
      return Math.max(0, fishList.size() - this.countCaughtFish(uuid, fishList));
   }

   protected int totalCaughtQuantity(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
      int userId = this.resolveUserId(uuid);
      if (userId == 0) {
         return 0;
      } else {
         DataManager<UserFishStats> userFishStatsDataManager = this.getUserFishStatsDataManager();
         if (userFishStatsDataManager == null) {
            return 0;
         } else {
            int total = 0;

            for (IFish fish : fishList) {
               UserFishStats stats = userFishStatsDataManager.peek(this.key(userId, fish));
               if (stats != null) {
                  total += stats.getQuantity();
               }
            }

            return total;
         }
      }
   }

   protected boolean hasCaughtFish(@NonNull UUID uuid, @NonNull IFish fish) {
      return this.timesCaught(uuid, fish) > 0;
   }

   protected int timesCaught(@NonNull UUID uuid, @NonNull IFish fish) {
      int userId = this.resolveUserId(uuid);
      if (userId == 0) {
         return 0;
      } else {
         DataManager<UserFishStats> userFishStatsDataManager = this.getUserFishStatsDataManager();
         if (userFishStatsDataManager == null) {
            return 0;
         } else {
            UserFishStats stats = userFishStatsDataManager.peek(this.key(userId, fish));
            return stats == null ? 0 : stats.getQuantity();
         }
      }
   }

   @Nullable
   protected IFish firstUncaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
      for (IFish fish : fishList) {
         if (!this.hasCaughtFish(uuid, fish)) {
            return fish;
         }
      }

      return null;
   }

   @NonNull
   protected List<IFish> getAllFish() {
      return FishManager.getInstance().getRarityMap().values().stream().flatMap(rarity -> rarity.getOriginalFishList().stream()).collect(Collectors.toList());
   }

   @Nullable
   protected IRarity resolveRarity(@NonNull String rarityId) {
      return FishManager.getInstance().getRarity(rarityId);
   }

   @Nullable
   protected IFish resolveFish(@NonNull String rarityId, @NonNull String fishName) {
      return FishManager.getInstance().getFish(rarityId, fishName);
   }

   protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
      int userId = this.resolveUserId(uuid);
      if (userId == 0) {
         return 0;
      } else {
         DataManager<UserFishStats> userFishStatsDataManager = this.getUserFishStatsDataManager();
         if (userFishStatsDataManager == null) {
            return 0;
         } else {
            int caught = 0;

            for (IFish fish : fishList) {
               UserFishStats stats = userFishStatsDataManager.peek(UserFishRarityKey.of(userId, fish).toString());
               if (stats != null && stats.getQuantity() > 0) {
                  caught++;
               }
            }

            return caught;
         }
      }
   }

   @Nullable
   protected UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget parseRarityTarget(@Nullable OfflinePlayer player, @NonNull String identifier) {
      String payload = identifier.substring(this.getPrefixLength());
      int splitIndex = payload.lastIndexOf(95);
      if (splitIndex > 0 && splitIndex != payload.length() - 1) {
         String rarityId = payload.substring(0, splitIndex);
         String target = payload.substring(splitIndex + 1);
         UUID uuid = this.resolveTarget(player, target, identifier);
         if (uuid == null) {
            return null;
         } else {
            IRarity rarity = this.resolveRarity(rarityId);
            if (rarity == null) {
               this.debug("Placeholder %s received an invalid rarity '%s' for identifier: %s".formatted(this.getClass().getSimpleName(), rarityId, identifier));
               return null;
            } else {
               return new UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget(uuid, rarityId, rarity, rarity.getOriginalFishList());
            }
         }
      } else {
         this.debug("Placeholder %s received an invalid rarity/target payload for identifier: %s".formatted(this.getClass().getSimpleName(), identifier));
         return null;
      }
   }

   @Nullable
   protected UniqueFishCaughtProgressPlaceholder.ParsedFishTarget parseFishTarget(@Nullable OfflinePlayer player, @NonNull String identifier) {
      String payload = identifier.substring(this.getPrefixLength());
      int targetSplitIndex = payload.lastIndexOf(95);
      if (targetSplitIndex > 0 && targetSplitIndex != payload.length() - 1) {
         String fishKey = payload.substring(0, targetSplitIndex);
         String target = payload.substring(targetSplitIndex + 1);
         int fishSplitIndex = fishKey.indexOf(58);
         if (fishSplitIndex > 0 && fishSplitIndex != fishKey.length() - 1) {
            String rarityId = fishKey.substring(0, fishSplitIndex);
            String fishName = fishKey.substring(fishSplitIndex + 1);
            UUID uuid = this.resolveTarget(player, target, identifier);
            if (uuid == null) {
               return null;
            } else {
               IFish fish = this.resolveFish(rarityId, fishName);
               if (fish == null) {
                  this.debug(
                     "Placeholder %s received an invalid fish '%s' in rarity '%s' for identifier: %s"
                        .formatted(this.getClass().getSimpleName(), fishName, rarityId, identifier)
                  );
                  return null;
               } else {
                  return new UniqueFishCaughtProgressPlaceholder.ParsedFishTarget(uuid, rarityId, fishName, fish);
               }
            }
         } else {
            this.debug(
               "Placeholder %s received an invalid rarity:fish key '%s' for identifier: %s".formatted(this.getClass().getSimpleName(), fishKey, identifier)
            );
            return null;
         }
      } else {
         this.debug("Placeholder %s received an invalid fish/target payload for identifier: %s".formatted(this.getClass().getSimpleName(), identifier));
         return null;
      }
   }

   private int resolveUserId(@NonNull UUID uuid) {
      if (DatabaseUtil.isDatabaseOffline()) {
         return 0;
      } else {
         EvenMoreFish plugin = EvenMoreFish.getInstance();
         UserManager userManager = plugin.getPluginDataManager().getUserManager();
         if (userManager == null) {
            return 0;
         } else {
            int userId = userManager.getCachedUserId(uuid);
            if (userId == 0) {
               plugin.getPluginDataManager().preloadUserDataAsync(uuid);
               return 0;
            } else if (!plugin.getPluginDataManager().isUserFishStatsPreloaded(userId)) {
               plugin.getPluginDataManager().preloadUserDataAsync(uuid);
               return 0;
            } else {
               return userId;
            }
         }
      }
   }

   @Nullable
   private DataManager<UserFishStats> getUserFishStatsDataManager() {
      return DatabaseUtil.isDatabaseOffline() ? null : EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager();
   }

   @NonNull
   private String key(int userId, @NonNull IFish fish) {
      return UserFishRarityKey.of(userId, fish).toString();
   }

   protected void debug(@NonNull String message) {
      try {
         Logging.debug(message);
      } catch (RuntimeException var3) {
      }
   }

   protected record ParsedFishTarget(@NonNull UUID uuid, @NonNull String rarityId, @NonNull String fishName, @NonNull IFish fish) {
   }

   protected record ParsedRarityTarget(@NonNull UUID uuid, @NonNull String rarityId, @NonNull IRarity rarity, @NonNull List<? extends IFish> fishList) {
   }
}
