package de.oliver.fancylib.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class PostgreSqlDatabase implements Database {
   protected final String host;
   protected final String port;
   protected final String database;
   protected final String username;
   protected final String password;
   protected Connection connection;

   public PostgreSqlDatabase(String host, String port, String database, String username, String password) {
      this.host = host;
      this.port = port;
      this.database = database;
      this.username = username;
      this.password = password;
      this.connection = null;
   }

   @Override
   public boolean connect() {
      try {
         if (this.isConnected()) {
            return true;
         } else {
            this.connection = DriverManager.getConnection(
               "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password
            );
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
         return false;
      }
   }

   @Override
   public boolean close() {
      try {
         if (!this.isConnected()) {
            return true;
         } else {
            this.connection.close();
            this.connection = null;
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
         return false;
      }
   }

   @Override
   public boolean isConnected() {
      return this.connection != null;
   }

   @Override
   public Connection getConnection() {
      return this.connection;
   }

   @Override
   public boolean executeNonQuery(String sql) {
      try {
         this.connection.createStatement().execute(sql);
         return true;
      } catch (Exception var3) {
         var3.printStackTrace();
         return false;
      }
   }

   @Override
   public ResultSet executeQuery(String query) {
      try {
         return this.connection.createStatement().executeQuery(query);
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }
}
