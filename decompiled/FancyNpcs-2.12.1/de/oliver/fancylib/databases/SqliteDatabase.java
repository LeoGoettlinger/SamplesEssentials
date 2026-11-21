package de.oliver.fancylib.databases;

import java.io.File;
import java.sql.DriverManager;
import org.bukkit.Bukkit;

public class SqliteDatabase extends MySqlDatabase {
   protected final String path;
   protected File file;

   public SqliteDatabase(String path) {
      super(null, null, null, null, null);
      this.path = path;
      this.file = new File(path);
   }

   @Override
   public boolean connect() {
      try {
         if (this.isConnected()) {
            return true;
         } else if (!this.file.exists() && !this.file.createNewFile()) {
            Bukkit.getLogger().warning("Could not create database file.");
            return false;
         } else {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.file.getPath());
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
         return false;
      }
   }
}
