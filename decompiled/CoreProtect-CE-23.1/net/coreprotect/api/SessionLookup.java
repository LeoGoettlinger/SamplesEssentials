package net.coreprotect.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.statement.UserStatement;

public class SessionLookup {
   public static final int ID = 0;

   private SessionLookup() {
      throw new IllegalStateException("API class");
   }

   public static List<String[]> performLookup(String user, int offset) {
      List<String[]> result = new ArrayList<>();
      if (!Config.getGlobal().API_ENABLED) {
         return result;
      } else if (user == null) {
         return result;
      } else {
         try {
            label121: {
               Object var20;
               try (Connection connection = Database.getConnection(false, 1000)) {
                  if (connection != null) {
                     String type = String.valueOf(0);
                     int time = (int)(System.currentTimeMillis() / 1000L);
                     int checkTime = calculateCheckTime(time, offset);
                     int userId = getUserId(connection, user);

                     try (Statement statement = connection.createStatement()) {
                        String query = buildSessionQuery(userId, checkTime);

                        try (ResultSet results = statement.executeQuery(query)) {
                           while (results.next()) {
                              String[] sessionData = extractSessionData(connection, results, type);
                              result.add(sessionData);
                           }
                           break label121;
                        }
                     }
                  }

                  var20 = result;
               }

               return (List<String[]>)var20;
            }
         } catch (Exception var19) {
            var19.printStackTrace();
         }

         return result;
      }
   }

   private static int calculateCheckTime(int currentTime, int offset) {
      return offset > 0 ? currentTime - offset : 0;
   }

   private static int getUserId(Connection connection, String username) {
      String lowerUsername = username.toLowerCase(Locale.ROOT);
      if (ConfigHandler.playerIdCache.get(lowerUsername) == null) {
         UserStatement.loadId(connection, username, null);
      }

      return ConfigHandler.playerIdCache.get(lowerUsername);
   }

   private static String buildSessionQuery(int userId, int checkTime) {
      return "SELECT time,user,wid,x,y,z,action FROM "
         + ConfigHandler.prefix
         + "session WHERE user = '"
         + userId
         + "' AND time > '"
         + checkTime
         + "' ORDER BY rowid DESC";
   }

   private static String[] extractSessionData(Connection connection, ResultSet results, String type) throws Exception {
      String resultTime = results.getString("time");
      int resultUserId = results.getInt("user");
      String resultWorldId = results.getString("wid");
      String resultX = results.getString("x");
      String resultY = results.getString("y");
      String resultZ = results.getString("z");
      String resultAction = results.getString("action");
      if (ConfigHandler.playerIdCacheReversed.get(resultUserId) == null) {
         UserStatement.loadName(connection, resultUserId);
      }

      String resultUser = ConfigHandler.playerIdCacheReversed.get(resultUserId);
      return new String[]{resultTime, resultUser, resultX, resultY, resultZ, resultWorldId, type, resultAction};
   }
}
