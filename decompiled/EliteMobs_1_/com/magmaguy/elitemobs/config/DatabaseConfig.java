package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class DatabaseConfig extends ConfigurationFile {
   public static boolean useMySQL;
   public static String mysqlHost;
   public static String mysqlPort;
   public static String mysqlDatabaseName;
   public static String mysqlUsername;
   public static String mysqlPassword;
   public static boolean useSSL;

   public DatabaseConfig() {
      super("Database.yml");
   }

   @Override
   public void initializeValues() {
      useMySQL = ConfigurationEngine.setBoolean(
         List.of("Sets whether MySQL will be used. By default EliteMobs uses SQLite. If you wish to use MySQL you will need a valid MySQL configuration."),
         this.fileConfiguration,
         "useMySQL",
         false
      );
      mysqlHost = ConfigurationEngine.setString(List.of("The host of your MySQL database"), this.file, this.fileConfiguration, "mysqlHost", "localhost", false);
      mysqlPort = ConfigurationEngine.setString(List.of("The port of your MySQL database"), this.file, this.fileConfiguration, "mysqlPort", "3306", false);
      mysqlDatabaseName = ConfigurationEngine.setString(
         List.of("The name of the database"), this.file, this.fileConfiguration, "mysqlDatabaseName", "elitemobs", false
      );
      mysqlUsername = ConfigurationEngine.setString(
         List.of("The username for MySQl"), this.file, this.fileConfiguration, "mysqlUsername", "your_username_mysql_here", false
      );
      mysqlPassword = ConfigurationEngine.setString(
         List.of("The password for your MysSQL database"), this.file, this.fileConfiguration, "mysqlPassword", "your_mysql_password_here", false
      );
      useSSL = ConfigurationEngine.setBoolean(List.of("Whether to use SSL"), this.fileConfiguration, "useSSL", true);
   }

   @Generated
   public static boolean isUseMySQL() {
      return useMySQL;
   }

   @Generated
   public static String getMysqlHost() {
      return mysqlHost;
   }

   @Generated
   public static String getMysqlPort() {
      return mysqlPort;
   }

   @Generated
   public static String getMysqlDatabaseName() {
      return mysqlDatabaseName;
   }

   @Generated
   public static String getMysqlUsername() {
      return mysqlUsername;
   }

   @Generated
   public static String getMysqlPassword() {
      return mysqlPassword;
   }

   @Generated
   public static boolean isUseSSL() {
      return useSSL;
   }
}
