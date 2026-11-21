package net.coreprotect.database.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MaterialStatement {
   private MaterialStatement() {
      throw new IllegalStateException("Database class");
   }

   public static void insert(PreparedStatement preparedStmt, int batchCount, int id, String name) {
      try {
         preparedStmt.setInt(1, id);
         preparedStmt.setString(2, name);
         preparedStmt.addBatch();
         if (batchCount > 0 && batchCount % 1000 == 0) {
            preparedStmt.executeBatch();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static boolean hasMaterial(Statement statement, String query) {
      boolean result = false;

      try {
         ResultSet resultSet = statement.executeQuery(query);
         if (resultSet.next()) {
            result = true;
         }

         resultSet.close();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return result;
   }
}
