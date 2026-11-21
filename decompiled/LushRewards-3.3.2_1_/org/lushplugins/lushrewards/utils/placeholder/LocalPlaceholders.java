package org.lushplugins.lushrewards.utils.placeholder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTrackerModule;

public class LocalPlaceholders {
   private static LocalDateTime nextDay = LocalDate.now().plusDays(1L).atStartOfDay();
   private final ConcurrentHashMap<String, Placeholder> placeholders = new ConcurrentHashMap<>();

   public LocalPlaceholders() {
      this.registerPlaceholder(new SimplePlaceholder("countdown", (params, player) -> {
         LocalDateTime now = LocalDateTime.now();
         long secondsUntil = now.until(nextDay, ChronoUnit.SECONDS);
         if (secondsUntil < 0L) {
            nextDay = LocalDate.now().plusDays(1L).atStartOfDay();
            secondsUntil = now.until(nextDay, ChronoUnit.SECONDS);
         }

         long hours = secondsUntil / 3600L;
         long minutes = secondsUntil % 3600L / 60L;
         long seconds = secondsUntil % 60L;
         return String.format("%02d:%02d:%02d", hours, minutes, seconds);
      }));
      this.registerPlaceholder(
         new SimplePlaceholder(
            "global_playtime",
            (params, player) -> {
               if (player == null) {
                  return null;
               } else {
                  Optional<Module> optionalPlaytimeTracker = LushRewards.getInstance().getModule("playtime-tracker");
                  return optionalPlaytimeTracker.isPresent() && optionalPlaytimeTracker.get() instanceof PlaytimeTrackerModule playtimeTrackerModule
                     ? String.valueOf(playtimeTrackerModule.getPlaytimeTracker(player.getUniqueId()).getGlobalPlaytime())
                     : null;
               }
            }
         )
      );
      this.registerPlaceholder(
         new SimplePlaceholder(
            "session_playtime",
            (params, player) -> {
               if (player == null) {
                  return null;
               } else {
                  Optional<Module> optionalPlaytimeTracker = LushRewards.getInstance().getModule("playtime-tracker");
                  return optionalPlaytimeTracker.isPresent() && optionalPlaytimeTracker.get() instanceof PlaytimeTrackerModule playtimeTrackerModule
                     ? String.valueOf(playtimeTrackerModule.getPlaytimeTracker(player.getUniqueId()).getSessionPlaytime())
                     : null;
               }
            }
         )
      );
      this.registerPlaceholder(
         new SimplePlaceholder(
            "total_session_playtime",
            (params, player) -> {
               if (player == null) {
                  return null;
               } else {
                  Optional<Module> optionalPlaytimeTracker = LushRewards.getInstance().getModule("playtime-tracker");
                  return optionalPlaytimeTracker.isPresent() && optionalPlaytimeTracker.get() instanceof PlaytimeTrackerModule playtimeTrackerModule
                     ? String.valueOf(playtimeTrackerModule.getPlaytimeTracker(player.getUniqueId()).getTotalSessionPlaytime())
                     : null;
               }
            }
         )
      );
   }

   public String parsePlaceholder(String params, Player player) {
      String[] paramsArr = params.split("_");
      Placeholder currentPlaceholder = null;
      String currParams = params;

      for (int i = 0; i < paramsArr.length; i++) {
         boolean found = false;

         for (Placeholder subPlaceholder : currentPlaceholder != null ? currentPlaceholder.getChildren() : this.placeholders.values()) {
            if (subPlaceholder.matches(currParams)) {
               currentPlaceholder = subPlaceholder;
               currParams = currParams.replace(subPlaceholder.getContent() + "_", "");
               found = true;
               break;
            }
         }

         if (!found) {
            break;
         }
      }

      if (currentPlaceholder != null) {
         try {
            return currentPlaceholder.parse(paramsArr, player);
         } catch (Exception var10) {
            var10.printStackTrace();
            return null;
         }
      } else {
         return null;
      }
   }

   public void registerPlaceholder(Placeholder placeholder) {
      this.placeholders.put(placeholder.getContent(), placeholder);
   }

   public void unregisterPlaceholder(String content) {
      this.placeholders.remove(content);
   }

   @FunctionalInterface
   public interface PlaceholderFunction {
      String apply(String[] var1, Player var2);
   }
}
