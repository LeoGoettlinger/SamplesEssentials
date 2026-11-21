package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.api.Logging;
import com.oheers.fish.database.model.user.UserReport;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public abstract class UserReportPlaceholder implements EMFPlaceholder {
   private final String prefix;
   private final int prefixLength;
   private final Function<UserReport, String> func;
   private final String def;

   public UserReportPlaceholder(@NonNull String prefix, @NonNull Function<UserReport, String> func, @NonNull String def) {
      this.prefix = prefix;
      this.prefixLength = prefix.length();
      this.func = func;
      this.def = def;
   }

   @Override
   public boolean shouldProcess(@NonNull String identifier) {
      return identifier.startsWith(this.prefix);
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      String target = identifier.substring(this.prefixLength);
      UUID uuid = this.fetchPlayerOrUUIDString(player, target);
      if (uuid == null) {
         if (target.equalsIgnoreCase("player")) {
            Logging.debug("Placeholder %s could not resolve player context for identifier: %s".formatted(this.getClass().getSimpleName(), identifier));
         } else {
            Logging.debug(
               "Placeholder %s received an invalid UUID target '%s' for identifier: %s".formatted(this.getClass().getSimpleName(), target, identifier)
            );
         }

         return null;
      } else {
         UserReport report = this.fetchUserReport(uuid);
         if (report == null) {
            Logging.debug(
               "Placeholder %s could not find user report for identifier: %s with UUID: %s".formatted(this.getClass().getSimpleName(), identifier, uuid)
            );
            return this.def;
         } else {
            return this.func.apply(report);
         }
      }
   }
}
