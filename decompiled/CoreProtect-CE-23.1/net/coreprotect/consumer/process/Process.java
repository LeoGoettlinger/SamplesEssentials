package net.coreprotect.consumer.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.database.Database;
import net.coreprotect.database.statement.UserStatement;
import org.bukkit.Material;

public class Process {
   public static final int BLOCK_BREAK = 0;
   public static final int BLOCK_PLACE = 1;
   public static final int SIGN_TEXT = 2;
   public static final int CONTAINER_BREAK = 3;
   public static final int PLAYER_INTERACTION = 4;
   public static final int CONTAINER_TRANSACTION = 5;
   public static final int STRUCTURE_GROWTH = 6;
   public static final int ROLLBACK_UPDATE = 7;
   public static final int CONTAINER_ROLLBACK_UPDATE = 8;
   public static final int WORLD_INSERT = 9;
   public static final int SIGN_UPDATE = 10;
   public static final int SKULL_UPDATE = 11;
   public static final int PLAYER_CHAT = 12;
   public static final int PLAYER_COMMAND = 13;
   public static final int PLAYER_LOGIN = 14;
   public static final int PLAYER_LOGOUT = 15;
   public static final int ENTITY_KILL = 16;
   public static final int ENTITY_SPAWN = 17;
   public static final int NATURAL_BLOCK_BREAK = 20;
   public static final int MATERIAL_INSERT = 21;
   public static final int ART_INSERT = 22;
   public static final int ENTITY_INSERT = 23;
   public static final int PLAYER_KILL = 24;
   public static final int BLOCKDATA_INSERT = 25;
   public static final int ITEM_TRANSACTION = 26;
   public static final int INVENTORY_ROLLBACK_UPDATE = 27;
   public static final int INVENTORY_CONTAINER_ROLLBACK_UPDATE = 28;
   public static final int BLOCK_INVENTORY_ROLLBACK_UPDATE = 29;
   public static int lastLockUpdate = 0;
   private static volatile int currentConsumerSize = 0;

   public static int getCurrentConsumerSize() {
      return currentConsumerSize;
   }

   protected static void updateLockTable(Statement statement, int locked) {
      try {
         int unixTimestamp = (int)(System.currentTimeMillis() / 1000L);
         int timeSinceLastUpdate = unixTimestamp - lastLockUpdate;
         if (timeSinceLastUpdate >= 15 || locked == 0) {
            statement.executeUpdate(
               "UPDATE " + ConfigHandler.prefix + "database_lock SET status = '" + locked + "', time = '" + unixTimestamp + "' WHERE rowid = '1'"
            );
            lastLockUpdate = unixTimestamp;
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   protected static void processConsumer(int processId, boolean lastRun) {
      try {
         label185: {
            try (Connection connection = Database.getConnection(false, 500)) {
               if (connection != null) {
                  Statement statement = connection.createStatement();
                  Database.performCheckpoint(statement, Config.getGlobal().MYSQL);
                  Consumer.isPaused = true;
                  ArrayList<Object[]> consumerData = Consumer.consumer.get(processId);
                  Map<Integer, String[]> users = Consumer.consumerUsers.get(processId);
                  Map<Integer, Object> consumerObject = Consumer.consumerObjects.get(processId);
                  int consumerDataSize = consumerData.size();
                  currentConsumerSize = consumerDataSize;
                  if (currentConsumerSize == 0) {
                     updateLockTable(statement, lastRun ? 0 : 1);
                     statement.close();
                     Consumer.consumer_id.put(processId, new Integer[]{0, 0});
                     Consumer.isPaused = false;
                     return;
                  }

                  Database.beginTransaction(statement, Config.getGlobal().MYSQL);

                  for (Entry<Integer, String[]> entry : users.entrySet()) {
                     String[] data = entry.getValue();
                     if (data != null) {
                        String user = data[0];
                        String uuid = data[1];
                        if (user != null && ConfigHandler.playerIdCache.get(user.toLowerCase(Locale.ROOT)) == null) {
                           UserStatement.loadId(connection, user, uuid);
                        }
                     }
                  }

                  updateLockTable(statement, lastRun ? 0 : 1);
                  Database.commitTransaction(statement, Config.getGlobal().MYSQL);
                  PreparedStatement preparedStmtSigns = Database.prepareStatement(connection, 0, false);
                  PreparedStatement preparedStmtBlocks = Database.prepareStatement(connection, 1, false);
                  PreparedStatement preparedStmtSkulls = Database.prepareStatement(connection, 2, true);
                  PreparedStatement preparedStmtContainers = Database.prepareStatement(connection, 3, false);
                  PreparedStatement preparedStmtItems = Database.prepareStatement(connection, 13, false);
                  PreparedStatement preparedStmtWorlds = Database.prepareStatement(connection, 4, false);
                  PreparedStatement preparedStmtChat = Database.prepareStatement(connection, 5, false);
                  PreparedStatement preparedStmtCommand = Database.prepareStatement(connection, 6, false);
                  PreparedStatement preparedStmtSession = Database.prepareStatement(connection, 7, false);
                  PreparedStatement preparedStmtEntities = Database.prepareStatement(connection, 8, true);
                  PreparedStatement preparedStmtMaterials = Database.prepareStatement(connection, 9, false);
                  PreparedStatement preparedStmtArt = Database.prepareStatement(connection, 10, false);
                  PreparedStatement preparedStmtEntity = Database.prepareStatement(connection, 11, false);
                  PreparedStatement preparedStmtBlockdata = Database.prepareStatement(connection, 12, false);
                  Database.beginTransaction(statement, Config.getGlobal().MYSQL);

                  for (int i = 0; i < consumerDataSize; i++) {
                     Object[] data = consumerData.get(i);
                     if (data != null) {
                        int id = (Integer)data[0];
                        int action = (Integer)data[1];
                        Material blockType = (Material)data[2];
                        int blockData = (Integer)data[3];
                        Material replaceType = (Material)data[4];
                        int replaceData = (Integer)data[5];
                        int forceData = (Integer)data[6];
                        if (users.get(id) != null && consumerObject.get(id) != null) {
                           String user = users.get(id)[0];
                           Object object = consumerObject.get(id);

                           try {
                              switch (action) {
                                 case 0:
                                    BlockBreakProcess.process(
                                       preparedStmtBlocks,
                                       preparedStmtSkulls,
                                       i,
                                       processId,
                                       id,
                                       blockType,
                                       blockData,
                                       replaceType,
                                       forceData,
                                       user,
                                       object,
                                       (String)data[7]
                                    );
                                    break;
                                 case 1:
                                    BlockPlaceProcess.process(
                                       preparedStmtBlocks,
                                       preparedStmtSkulls,
                                       i,
                                       blockType,
                                       blockData,
                                       replaceType,
                                       replaceData,
                                       forceData,
                                       user,
                                       object,
                                       (String)data[7],
                                       (String)data[8]
                                    );
                                    break;
                                 case 2:
                                    SignTextProcess.process(preparedStmtSigns, i, processId, id, forceData, user, object, replaceData, blockData);
                                    break;
                                 case 3:
                                    ContainerBreakProcess.process(preparedStmtContainers, i, processId, id, blockType, user, object);
                                    break;
                                 case 4:
                                    PlayerInteractionProcess.process(preparedStmtBlocks, i, user, object, blockType);
                                    break;
                                 case 5:
                                    ContainerTransactionProcess.process(
                                       preparedStmtContainers, preparedStmtItems, i, processId, id, blockType, forceData, user, object
                                    );
                                    break;
                                 case 6:
                                    StructureGrowthProcess.process(statement, preparedStmtBlocks, i, processId, id, user, object, forceData);
                                    break;
                                 case 7:
                                    RollbackUpdateProcess.process(statement, processId, id, forceData, 0);
                                    break;
                                 case 8:
                                    RollbackUpdateProcess.process(statement, processId, id, forceData, 1);
                                    break;
                                 case 9:
                                    WorldInsertProcess.process(preparedStmtWorlds, i, statement, object, forceData);
                                    break;
                                 case 10:
                                    SignUpdateProcess.process(statement, object, user, blockData, forceData);
                                    break;
                                 case 11:
                                    SkullUpdateProcess.process(statement, object, forceData);
                                    break;
                                 case 12:
                                    PlayerChatProcess.process(preparedStmtChat, i, processId, id, object, user);
                                    break;
                                 case 13:
                                    PlayerCommandProcess.process(preparedStmtCommand, i, processId, id, object, user);
                                    break;
                                 case 14:
                                    PlayerLoginProcess.process(
                                       connection, preparedStmtSession, i, processId, id, object, blockData, replaceData, forceData, user
                                    );
                                    break;
                                 case 15:
                                    PlayerLogoutProcess.process(preparedStmtSession, i, object, forceData, user);
                                    break;
                                 case 16:
                                    EntityKillProcess.process(preparedStmtBlocks, preparedStmtEntities, i, processId, id, object, user);
                                    break;
                                 case 17:
                                    EntitySpawnProcess.process(statement, object, forceData);
                                 case 18:
                                 case 19:
                                 default:
                                    break;
                                 case 20:
                                    NaturalBlockBreakProcess.process(
                                       statement, preparedStmtBlocks, i, processId, id, user, object, blockType, blockData, (String)data[7]
                                    );
                                    break;
                                 case 21:
                                    MaterialInsertProcess.process(preparedStmtMaterials, statement, i, object, forceData);
                                    break;
                                 case 22:
                                    ArtInsertProcess.process(preparedStmtArt, statement, i, object, forceData);
                                    break;
                                 case 23:
                                    EntityInsertProcess.process(preparedStmtEntity, statement, i, object, forceData);
                                    break;
                                 case 24:
                                    PlayerKillProcess.process(preparedStmtBlocks, i, id, object, user);
                                    break;
                                 case 25:
                                    BlockDataInsertProcess.process(preparedStmtBlockdata, statement, i, object, forceData);
                                    break;
                                 case 26:
                                    ItemTransactionProcess.process(preparedStmtItems, i, processId, id, forceData, replaceData, blockData, user, object);
                                    break;
                                 case 27:
                                    RollbackUpdateProcess.process(statement, processId, id, forceData, 2);
                                    break;
                                 case 28:
                                    RollbackUpdateProcess.process(statement, processId, id, forceData, 3);
                                    break;
                                 case 29:
                                    RollbackUpdateProcess.process(statement, processId, id, forceData, 4);
                              }

                              if (statement.isClosed()) {
                                 for (int index = i - 1; index >= 0; index--) {
                                    consumerData.remove(index);
                                 }

                                 currentConsumerSize = 0;
                                 Consumer.consumer_id.put(processId, new Integer[]{0, 0});
                                 Consumer.isPaused = false;
                                 return;
                              }

                              if (Consumer.interrupt) {
                                 commit(
                                    statement,
                                    preparedStmtSigns,
                                    preparedStmtBlocks,
                                    preparedStmtSkulls,
                                    preparedStmtContainers,
                                    preparedStmtItems,
                                    preparedStmtWorlds,
                                    preparedStmtChat,
                                    preparedStmtCommand,
                                    preparedStmtSession,
                                    preparedStmtEntities,
                                    preparedStmtMaterials,
                                    preparedStmtArt,
                                    preparedStmtEntity,
                                    preparedStmtBlockdata
                                 );
                                 Thread.sleep(500L);
                                 Database.beginTransaction(statement, Config.getGlobal().MYSQL);
                              }
                           } catch (Exception var35) {
                              var35.printStackTrace();
                           }
                        }
                     }

                     currentConsumerSize--;
                  }

                  commit(
                     statement,
                     preparedStmtSigns,
                     preparedStmtBlocks,
                     preparedStmtSkulls,
                     preparedStmtContainers,
                     preparedStmtItems,
                     preparedStmtWorlds,
                     preparedStmtChat,
                     preparedStmtCommand,
                     preparedStmtSession,
                     preparedStmtEntities,
                     preparedStmtMaterials,
                     preparedStmtArt,
                     preparedStmtEntity,
                     preparedStmtBlockdata
                  );
                  preparedStmtSigns.close();
                  preparedStmtBlocks.close();
                  preparedStmtSkulls.close();
                  preparedStmtContainers.close();
                  preparedStmtItems.close();
                  preparedStmtWorlds.close();
                  preparedStmtChat.close();
                  preparedStmtCommand.close();
                  preparedStmtSession.close();
                  preparedStmtEntities.close();
                  preparedStmtMaterials.close();
                  preparedStmtArt.close();
                  preparedStmtEntity.close();
                  preparedStmtBlockdata.close();
                  statement.close();
                  users.clear();
                  consumerObject.clear();
                  consumerData.clear();
                  break label185;
               }
            }

            return;
         }
      } catch (Exception var37) {
         var37.printStackTrace();
      }

      Consumer.consumer_id.put(processId, new Integer[]{0, 0});
      Consumer.isPaused = false;
   }

   private static void commit(
      Statement statement,
      PreparedStatement preparedStmtSigns,
      PreparedStatement preparedStmtBlocks,
      PreparedStatement preparedStmtSkulls,
      PreparedStatement preparedStmtContainers,
      PreparedStatement preparedStmtItems,
      PreparedStatement preparedStmtWorlds,
      PreparedStatement preparedStmtChat,
      PreparedStatement preparedStmtCommand,
      PreparedStatement preparedStmtSession,
      PreparedStatement preparedStmtEntities,
      PreparedStatement preparedStmtMaterials,
      PreparedStatement preparedStmtArt,
      PreparedStatement preparedStmtEntity,
      PreparedStatement preparedStmtBlockdata
   ) {
      try {
         preparedStmtSigns.executeBatch();
         preparedStmtBlocks.executeBatch();
         preparedStmtSkulls.executeBatch();
         preparedStmtContainers.executeBatch();
         preparedStmtItems.executeBatch();
         preparedStmtWorlds.executeBatch();
         preparedStmtChat.executeBatch();
         preparedStmtCommand.executeBatch();
         preparedStmtSession.executeBatch();
         preparedStmtEntities.executeBatch();
         preparedStmtMaterials.executeBatch();
         preparedStmtArt.executeBatch();
         preparedStmtEntity.executeBatch();
         preparedStmtBlockdata.executeBatch();
         Database.commitTransaction(statement, Config.getGlobal().MYSQL);
      } catch (Exception var16) {
         var16.printStackTrace();
      }
   }
}
