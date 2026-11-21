package net.coreprotect.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

public class DatabaseUtils {
   private DatabaseUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static <K, V extends Comparable<? super V>> SortedSet<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
      SortedSet<Entry<K, V>> sortedEntries = new TreeSet<>((e1, e2) -> {
         int res = e1.getValue().compareTo(e2.getValue());
         return res != 0 ? res : 1;
      });
      sortedEntries.addAll(map.entrySet());
      return sortedEntries;
   }

   public static boolean successfulQuery(Connection connection, String query) {
      boolean result = false;

      try {
         PreparedStatement preparedStmt = connection.prepareStatement(query);
         ResultSet resultSet = preparedStmt.executeQuery();
         if (resultSet.isBeforeFirst()) {
            result = true;
         }

         resultSet.close();
         preparedStmt.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return result;
   }
}
