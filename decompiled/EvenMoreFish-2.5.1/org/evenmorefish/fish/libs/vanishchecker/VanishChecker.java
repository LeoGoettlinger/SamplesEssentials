package org.evenmorefish.fish.libs.vanishchecker;

import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.quantiom.advancedvanish.util.AdvancedVanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sayandev.sayanvanish.api.SayanVanishAPI;

public class VanishChecker {
   public static boolean isVanished(@Nullable Player player) {
      return player == null
         ? false
         : isMetadataVanished(player) || isEssentialsVanished(player) || isCMIVanished(player) || isSayanVanished(player) || isAdvancedVanished(player);
   }

   public static boolean isEssentialsVanished(@Nullable Player player) {
      if (player != null && isPluginEnabled("Essentials") && classExists("com.earth2me.essentials.Essentials")) {
         if (!(Bukkit.getPluginManager().getPlugin("Essentials") instanceof Essentials essentials)) {
            return false;
         } else {
            User user = essentials.getUser(player);
            return user != null && user.isVanished();
         }
      } else {
         return false;
      }
   }

   public static boolean isCMIVanished(@Nullable Player player) {
      if (player != null && isPluginEnabled("CMI")) {
         CMIUser user = CMIUser.getUser(player);
         return user != null && user.isCMIVanished();
      } else {
         return false;
      }
   }

   public static boolean isSayanVanished(@Nullable Player player) {
      if (player != null && isPluginEnabled("SayanVanish")) {
         org.sayandev.sayanvanish.api.User user = SayanVanishAPI.user(player.getUniqueId());
         return user != null && user.isVanished();
      } else {
         return false;
      }
   }

   public static boolean isAdvancedVanished(@Nullable Player player) {
      return player != null && isPluginEnabled("AdvancedVanish") ? AdvancedVanishAPI.INSTANCE.isPlayerVanished(player) : false;
   }

   public static boolean isMetadataVanished(@Nullable Player player) {
      if (player == null) {
         return false;
      } else {
         for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) {
               return true;
            }
         }

         return false;
      }
   }

   @NotNull
   public static List<Player> getVanishedPlayersFromList(@Nullable List<Player> players) {
      return (List<Player>)(players == null
         ? new ArrayList<>()
         : players.stream().filter(VanishChecker::isVanished).collect(Collectors.toCollection(ArrayList::new)));
   }

   @NotNull
   public static List<Player> getVanishedOnlinePlayers() {
      return Bukkit.getOnlinePlayers().stream().filter(VanishChecker::isVanished).collect(Collectors.toCollection(ArrayList::new));
   }

   @NotNull
   public static List<Player> getVisiblePlayersFromList(@Nullable List<Player> players) {
      return (List<Player>)(players == null
         ? new ArrayList<>()
         : players.stream().filter(player -> !isVanished(player)).collect(Collectors.toCollection(ArrayList::new)));
   }

   @NotNull
   public static List<Player> getVisibleOnlinePlayers() {
      return Bukkit.getOnlinePlayers().stream().filter(player -> !isVanished(player)).collect(Collectors.toCollection(ArrayList::new));
   }

   private static boolean classExists(@NotNull String name) {
      try {
         Class.forName(name);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   private static boolean isPluginEnabled(@NotNull String name) {
      return Bukkit.getPluginManager().isPluginEnabled(name);
   }
}
