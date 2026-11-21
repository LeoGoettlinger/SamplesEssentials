package dev.cwhead.GravesX.manager.db;

import com.ranull.graves.Graves;
import com.ranull.graves.libraries.hikari.HikariCredentialsProvider;
import com.ranull.graves.libraries.hikari.util.Credentials;
import com.ranull.graves.manager.DataManager;
import java.util.Objects;
import org.bukkit.configuration.file.FileConfiguration;

public final class CredentialsProviderFactory {
   private CredentialsProviderFactory() {
   }

   public static HikariCredentialsProvider forType(Graves plugin, DataManager.Type type) {
      return switch (type) {
         case POSTGRESQL -> new CredentialsProviderFactory.ConfigPathProvider(
            plugin, "settings.storage.postgresql.username", "settings.storage.postgresql.password"
         );
         case MYSQL, MARIADB -> new CredentialsProviderFactory.ConfigPathProvider(plugin, "settings.storage.mysql.username", "settings.storage.mysql.password");
         case MSSQL -> new CredentialsProviderFactory.ConfigPathProvider(plugin, "settings.storage.mssql.username", "settings.storage.mssql.password");
         case H2 -> new CredentialsProviderFactory.ConfigPathProvider(plugin, "settings.storage.h2.username", "settings.storage.h2.password");
         case SQLITE, INVALID -> null;
      };
   }

   public static class ConfigPathProvider implements HikariCredentialsProvider {
      private final Graves plugin;
      private final String userPath;
      private final String passPath;

      public ConfigPathProvider(Graves plugin, String userPath, String passPath) {
         this.plugin = Objects.requireNonNull(plugin, "plugin");
         this.userPath = Objects.requireNonNull(userPath, "userPath");
         this.passPath = Objects.requireNonNull(passPath, "passPath");
      }

      public Credentials getCredentials() {
         FileConfiguration cfg = this.plugin.getConfig();
         String user = cfg.getString(this.userPath, "");
         String pass = cfg.getString(this.passPath, "");
         return Credentials.of(user, pass);
      }
   }
}
