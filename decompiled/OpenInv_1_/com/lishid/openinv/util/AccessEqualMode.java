package com.lishid.openinv.util;

import com.lishid.openinv.util.config.Config;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AccessEqualMode {
   DENY,
   ALLOW,
   VIEW;

   @NotNull
   public static AccessEqualMode of(@Nullable String value) {
      if (value == null) {
         return VIEW;
      } else {
         String var1 = value.toLowerCase(Locale.ENGLISH);

         return switch (var1) {
            case "deny", "false" -> DENY;
            case "allow", "true" -> ALLOW;
            default -> VIEW;
         };
      }
   }

   @NotNull
   public static AccessEqualMode getByPerm(@NotNull Permissible permissible, @NotNull Config config) {
      if (Permissions.ACCESS_EQUAL_EDIT.hasPermission(permissible)) {
         return ALLOW;
      } else if (Permissions.ACCESS_EQUAL_VIEW.hasPermission(permissible)) {
         return VIEW;
      } else {
         return Permissions.ACCESS_EQUAL_DENY.hasPermission(permissible) ? DENY : config.getAccessEqualMode();
      }
   }
}
