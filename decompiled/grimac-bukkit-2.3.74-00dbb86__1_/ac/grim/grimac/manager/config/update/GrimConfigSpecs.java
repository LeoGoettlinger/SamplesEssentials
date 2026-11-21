package ac.grim.grimac.manager.config.update;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Locale;
import lombok.Generated;

public final class GrimConfigSpecs {
   @NotNull
   public static ConfigUpdater.Spec mainConfig() {
      return ConfigUpdater.Spec.builder("/config/", 11, ConfigUpdater.ConfigFlavor.V2).migration(10, ctx -> {
         String typeRaw = ctx.input().getString("history.database.type");
         String type = typeRaw == null ? null : typeRaw.trim().toUpperCase(Locale.ROOT);
         String backendId = backendIdFor(type);
         if (backendId != null) {
            for (String cat : new String[]{"violation", "session", "player-identity", "setting"}) {
               ctx.otherFile("database.yml").put("database.routing." + cat, backendId);
            }
         }

         Integer entriesPerPage = ctx.input().getInt("history.entries-per-page");
         if (entriesPerPage != null) {
            ctx.otherFile("database.yml").put("database.history.entries-per-page", entriesPerPage);
         }

         String serverName = ctx.input().getString("history.server-name");
         if (serverName != null) {
            ctx.otherFile("database.yml").put("database.server-name", serverName);
         }

         if (backendId != null && !backendId.equals("sqlite")) {
            String target = "databases/" + backendId + ".yml";
            Object host = ctx.input().get("history.database.host");
            Object port = ctx.input().get("history.database.port");
            Object db = ctx.input().get("history.database.database");
            Object user = ctx.input().get("history.database.username");
            Object pass = ctx.input().get("history.database.password");
            if (host != null) {
               ctx.otherFile(target).put(backendId + ".host", host);
            }

            if (port != null) {
               ctx.otherFile(target).put(backendId + ".port", port);
            }

            if (db != null) {
               ctx.otherFile(target).put(backendId + ".database", db);
            }

            if (user != null) {
               ctx.otherFile(target).put(backendId + ".user", user);
            }

            if (pass != null) {
               ctx.otherFile(target).put(backendId + ".password", pass);
            }
         }
      }).build();
   }

   @Nullable
   private static String backendIdFor(@Nullable String legacyType) {
      if (legacyType == null) {
         return null;
      } else {
         return switch (legacyType) {
            case "SQLITE" -> "sqlite";
            case "MYSQL" -> "mysql";
            case "POSTGRESQL", "POSTGRES" -> "postgres";
            case "NOOP", "NONE", "DISABLED" -> null;
            default -> null;
         };
      }
   }

   @NotNull
   public static ConfigUpdater.Spec discord() {
      return ConfigUpdater.Spec.builder("/discord/", 1, ConfigUpdater.ConfigFlavor.V2).build();
   }

   @NotNull
   public static ConfigUpdater.Spec messages() {
      return ConfigUpdater.Spec.builder("/messages/", 2, ConfigUpdater.ConfigFlavor.V2).build();
   }

   @NotNull
   public static ConfigUpdater.Spec database() {
      return ConfigUpdater.Spec.builder("/database/", 1, ConfigUpdater.ConfigFlavor.V2).build();
   }

   @NotNull
   public static ConfigUpdater.Spec backend(@NotNull String backendId) {
      return ConfigUpdater.Spec.builder("/databases/" + backendId + "/", 1, ConfigUpdater.ConfigFlavor.V2).build();
   }

   @Generated
   private GrimConfigSpecs() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
