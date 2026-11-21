package de.oliver.fancylib.databases;

import java.sql.Connection;
import java.sql.ResultSet;

public interface Database {
   boolean connect();

   boolean close();

   boolean isConnected();

   Connection getConnection();

   boolean executeNonQuery(String var1);

   ResultSet executeQuery(String var1);
}
