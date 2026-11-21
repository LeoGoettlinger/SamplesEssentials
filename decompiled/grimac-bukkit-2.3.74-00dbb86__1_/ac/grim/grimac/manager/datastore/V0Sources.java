package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class V0Sources {
   private V0Sources() {
   }

   @Nullable
   public static V0Sources.V0Source detect(@NotNull Path dataFolder, @NotNull ConfigManager cfg) {
      String rawType = cfg.getStringElse("history.database.type", "SQLITE").toUpperCase(Locale.ROOT);

      return switch (rawType) {
         case "MYSQL" -> mysqlSource(cfg);
         case "POSTGRESQL" -> postgresqlSource(cfg);
         case "NOOP" -> null;
         default -> sqliteSource(dataFolder);
      };
   }

   @Nullable
   public static V0Sources.V0Source sqliteSource(@NotNull Path dataFolder) {
      Path legacy = dataFolder.resolve("violations.sqlite");
      if (!Files.isRegularFile(legacy)) {
         Path fallback = dataFolder.resolve("data").resolve("violations.sqlite");
         if (!Files.isRegularFile(fallback)) {
            return null;
         }

         legacy = fallback;
      }

      return new V0Sources.V0Source("sqlite", "jdbc:sqlite:" + legacy.toAbsolutePath(), null, null, "sqlite file " + legacy.getFileName());
   }

   private static V0Sources.V0Source mysqlSource(@NotNull ConfigManager cfg) {
      String host = cfg.getStringElse("history.database.host", "localhost");
      int port = cfg.getIntElse("history.database.port", 3306);
      String database = cfg.getStringElse("history.database.database", "grimac");
      String username = cfg.getStringElse("history.database.username", "root");
      String password = cfg.getStringElse("history.database.password", "");
      String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
      return new V0Sources.V0Source("mysql", url, username, password, "mysql " + host + ":" + port + "/" + database + " as " + username);
   }

   private static V0Sources.V0Source postgresqlSource(@NotNull ConfigManager cfg) {
      String host = cfg.getStringElse("history.database.host", "localhost");
      int port = cfg.getIntElse("history.database.port", 5432);
      String database = cfg.getStringElse("history.database.database", "grimac");
      String username = cfg.getStringElse("history.database.username", "postgres");
      String password = cfg.getStringElse("history.database.password", "");
      String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
      return new V0Sources.V0Source("postgresql", url, username, password, "postgresql " + host + ":" + port + "/" + database + " as " + username);
   }

   public record V0Source(String type, String jdbcUrl, @Nullable String username, @Nullable String password, String summary) {
   }
}
