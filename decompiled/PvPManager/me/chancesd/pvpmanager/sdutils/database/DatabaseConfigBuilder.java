package me.chancesd.pvpmanager.sdutils.database;

import java.io.File;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.configuration.ConfigurationSection;

public class DatabaseConfigBuilder {
   private static final String DATABASE_FIELD = "Database";
   private static final String USERNAME_FIELD = "Username";
   private static final String PASSWORD_FIELD = "Password";
   private String driver;
   private String url;
   private String database;
   private String user;
   private String password;
   private String file;
   private DatabaseConfigBuilder.DatabaseType type;

   public DatabaseConfigBuilder(File sqliteFile) {
      this.driver("org.sqlite.SQLiteDataSource").type(DatabaseConfigBuilder.DatabaseType.SQLITE).sqlite(sqliteFile);
   }

   public DatabaseConfigBuilder(ConfigurationSection section) {
      DatabaseConfigBuilder.DatabaseType dbType = getDBTypeFrom(section);
      String newURL = String.format("%s:%d", section.getString("Host"), section.getInt("Port"));
      if (dbType == DatabaseConfigBuilder.DatabaseType.MYSQL) {
         this.driver("com.mysql.jdbc.Driver")
            .type(DatabaseConfigBuilder.DatabaseType.MYSQL)
            .url(newURL)
            .database(section.getString("Database"))
            .user(section.getString("Username"))
            .password(section.getString("Password"));
      }
   }

   public DatabaseConfigBuilder(ConfigurationSection section, File backup) {
      this(section, backup, getDBTypeFrom(section));
   }

   public DatabaseConfigBuilder(ConfigurationSection section, File backup, DatabaseConfigBuilder.DatabaseType dbType) {
      if (dbType == DatabaseConfigBuilder.DatabaseType.MYSQL) {
         ConfigurationSection mysql = section.getConfigurationSection("MySQL");
         if (mysql == null) {
            throw new IllegalArgumentException("The MySQL config section is missing");
         }

         String newURL = String.format("%s:%d", mysql.getString("Host"), mysql.getInt("Port"));
         this.driver("com.mysql.jdbc.Driver")
            .type(DatabaseConfigBuilder.DatabaseType.MYSQL)
            .url(newURL)
            .database(mysql.getString("Database"))
            .user(mysql.getString("Username"))
            .password(mysql.getString("Password"));
      } else {
         this.driver("org.sqlite.SQLiteDataSource").type(DatabaseConfigBuilder.DatabaseType.SQLITE).sqlite(backup);
      }
   }

   private static DatabaseConfigBuilder.DatabaseType getDBTypeFrom(ConfigurationSection section) {
      String dbType = section.getString("Type", "SQLite").toUpperCase();

      DatabaseConfigBuilder.DatabaseType databaseType;
      try {
         databaseType = DatabaseConfigBuilder.DatabaseType.valueOf(dbType);
      } catch (IllegalArgumentException var4) {
         Log.severe("The database type you specified in the config does not exist: " + dbType + ". Defaulting to SQLite");
         databaseType = DatabaseConfigBuilder.DatabaseType.SQLITE;
      }

      return databaseType;
   }

   public DatabaseConfigBuilder driver(String newDriver) {
      this.driver = newDriver;
      return this;
   }

   public DatabaseConfigBuilder url(String newURL) {
      this.url = newURL;
      return this;
   }

   public DatabaseConfigBuilder database(String newDatabase) {
      this.database = newDatabase;
      return this;
   }

   public DatabaseConfigBuilder user(String newUser) {
      this.user = newUser;
      return this;
   }

   public DatabaseConfigBuilder password(String newPassword) {
      this.password = newPassword;
      return this;
   }

   public DatabaseConfigBuilder sqlite(File newFile) {
      this.file = newFile.getPath();
      return this;
   }

   public DatabaseConfigBuilder type(DatabaseConfigBuilder.DatabaseType newType) {
      this.type = newType;
      return this;
   }

   public String getFile() {
      return this.file;
   }

   public String getDriver() {
      return this.driver;
   }

   public String getUrl() {
      return this.url;
   }

   public String getDatabase() {
      return this.database;
   }

   public String getUser() {
      return this.user;
   }

   public String getPassword() {
      return this.password;
   }

   public DatabaseConfigBuilder.DatabaseType getType() {
      return this.type;
   }

   public static enum DatabaseType {
      SQLITE,
      MYSQL;
   }
}
