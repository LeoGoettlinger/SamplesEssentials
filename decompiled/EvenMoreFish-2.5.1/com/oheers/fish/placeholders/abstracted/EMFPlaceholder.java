package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.plugin.PluginDataManager;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public interface EMFPlaceholder {
   boolean shouldProcess(@NonNull String identifier);

   @Nullable
   String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier);

   @Nullable
   default CompetitionEntry fetchEntry(@NonNull Competition active, @NonNull String identifier, int prefixLength) {
      Integer place = FishUtils.getInteger(identifier.substring(prefixLength));
      return place != null && place > 0 ? active.getLeaderboard().getEntry(place) : null;
   }

   @Nullable
   default UUID fetchPlayerOrUUIDString(@Nullable OfflinePlayer player, @NonNull String substring) {
      if (substring.equalsIgnoreCase("player")) {
         return player == null ? null : player.getUniqueId();
      } else {
         try {
            return UUID.fromString(substring);
         } catch (IllegalArgumentException var4) {
            return null;
         }
      }
   }

   @Nullable
   default UserReport fetchUserReport(@NonNull UUID uuid) {
      if (DatabaseUtil.isDatabaseOffline()) {
         return null;
      } else {
         PluginDataManager dataManager = EvenMoreFish.getInstance().getPluginDataManager();
         UserReport report = dataManager.getUserReportDataManager().peek(uuid.toString());
         if (report == null) {
            dataManager.preloadUserDataAsync(uuid);
         }

         return report;
      }
   }
}
