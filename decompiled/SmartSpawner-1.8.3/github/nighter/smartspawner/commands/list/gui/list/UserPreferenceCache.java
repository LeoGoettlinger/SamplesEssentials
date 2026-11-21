package github.nighter.smartspawner.commands.list.gui.list;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.commands.list.gui.list.enums.FilterOption;
import github.nighter.smartspawner.commands.list.gui.list.enums.SortOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class UserPreferenceCache {
   private final Map<UUID, Map<String, UserPreferenceCache.UserPreference>> userPreferences = new ConcurrentHashMap<>();
   private final Plugin plugin;
   private static final long EXPIRY_TICKS = 36000L;
   private Scheduler.Task cleanupTask;

   public UserPreferenceCache(Plugin plugin) {
      this.plugin = plugin;
      this.cleanupTask = Scheduler.runTaskTimerAsync(this::cleanupExpiredPreferences, 36000L, 36000L);
   }

   public void savePreference(UUID playerUuid, String worldName, FilterOption filterOption, SortOption sortOption) {
      this.userPreferences
         .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
         .put(worldName, new UserPreferenceCache.UserPreference(filterOption, sortOption));
   }

   public UserPreferenceCache.UserPreference getPreference(UUID playerUuid, String worldName) {
      Map<String, UserPreferenceCache.UserPreference> worldPreferences = this.userPreferences.get(playerUuid);
      if (worldPreferences == null) {
         return null;
      } else {
         UserPreferenceCache.UserPreference preference = worldPreferences.get(worldName);
         if (preference != null && preference.isExpired()) {
            worldPreferences.remove(worldName);
            return null;
         } else {
            return preference;
         }
      }
   }

   private void cleanupExpiredPreferences() {
      long now = System.currentTimeMillis();
      this.userPreferences.forEach((uuid, worldPrefs) -> {
         worldPrefs.entrySet().removeIf(entry -> entry.getValue().isExpired());
         if (worldPrefs.isEmpty()) {
            this.userPreferences.remove(uuid);
         }
      });
   }

   public void clearPreferences(UUID playerUuid) {
      this.userPreferences.remove(playerUuid);
   }

   public FilterOption getUserFilter(Player player, String worldName) {
      UserPreferenceCache.UserPreference preference = this.getPreference(player.getUniqueId(), worldName);
      return preference != null ? preference.getFilterOption() : FilterOption.ALL;
   }

   public SortOption getUserSort(Player player, String worldName) {
      UserPreferenceCache.UserPreference preference = this.getPreference(player.getUniqueId(), worldName);
      return preference != null ? preference.getSortOption() : SortOption.DEFAULT;
   }

   public void shutdown() {
      if (this.cleanupTask != null) {
         this.cleanupTask.cancel();
         this.cleanupTask = null;
      }
   }

   public static class UserPreference {
      private final FilterOption filterOption;
      private final SortOption sortOption;
      private final long timestamp;

      public UserPreference(FilterOption filterOption, SortOption sortOption) {
         this.filterOption = filterOption;
         this.sortOption = sortOption;
         this.timestamp = System.currentTimeMillis();
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.timestamp > 1800000L;
      }

      @Generated
      public FilterOption getFilterOption() {
         return this.filterOption;
      }

      @Generated
      public SortOption getSortOption() {
         return this.sortOption;
      }

      @Generated
      public long getTimestamp() {
         return this.timestamp;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof UserPreferenceCache.UserPreference other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (this.getTimestamp() != other.getTimestamp()) {
            return false;
         } else {
            Object this$filterOption = this.getFilterOption();
            Object other$filterOption = other.getFilterOption();
            if (this$filterOption == null ? other$filterOption == null : this$filterOption.equals(other$filterOption)) {
               Object this$sortOption = this.getSortOption();
               Object other$sortOption = other.getSortOption();
               return this$sortOption == null ? other$sortOption == null : this$sortOption.equals(other$sortOption);
            } else {
               return false;
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof UserPreferenceCache.UserPreference;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         long $timestamp = this.getTimestamp();
         result = result * 59 + (int)($timestamp >>> 32 ^ $timestamp);
         Object $filterOption = this.getFilterOption();
         result = result * 59 + ($filterOption == null ? 43 : $filterOption.hashCode());
         Object $sortOption = this.getSortOption();
         return result * 59 + ($sortOption == null ? 43 : $sortOption.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "UserPreferenceCache.UserPreference(filterOption="
            + this.getFilterOption()
            + ", sortOption="
            + this.getSortOption()
            + ", timestamp="
            + this.getTimestamp()
            + ")";
      }
   }
}
