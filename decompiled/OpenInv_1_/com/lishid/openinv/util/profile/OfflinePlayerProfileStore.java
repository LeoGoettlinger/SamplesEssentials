package com.lishid.openinv.util.profile;

import com.lishid.openinv.util.StringMetric;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OfflinePlayerProfileStore implements ProfileStore {
   @NotNull
   private final Logger logger;

   public OfflinePlayerProfileStore(@NotNull Logger logger) {
      this.logger = logger;
   }

   @Override
   public void addProfile(@NotNull Profile profile) {
   }

   @Override
   public void setup() {
   }

   @Override
   public void shutdown() {
   }

   @Override
   public void tryImport() {
   }

   @Nullable
   @Override
   public Profile getProfileExact(@NotNull String name) {
      ProfileStore.warnMainThread(this.logger);
      OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
      if (!offline.hasPlayedBefore()) {
         return null;
      } else {
         String realName = offline.getName();
         if (realName == null) {
            realName = name;
         }

         return new Profile(realName, offline.getUniqueId());
      }
   }

   @Nullable
   @Override
   public Profile getProfileInexact(@NotNull String search) {
      ProfileStore.warnMainThread(this.logger);
      float bestMatch = 0.0F;
      Profile bestProfile = null;

      for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
         String name = player.getName();
         if (name == null) {
            return null;
         }

         float currentMatch = StringMetric.compareJaroWinkler(name, name);
         if (currentMatch > bestMatch) {
            bestMatch = currentMatch;
            bestProfile = new Profile(name, player.getUniqueId());
         }

         if (currentMatch == 1.0F) {
            break;
         }
      }

      return bestProfile;
   }
}
