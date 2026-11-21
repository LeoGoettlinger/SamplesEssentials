package net.coreprotect.command;

import java.io.File;
import java.lang.invoke.StringConcatFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.database.Database;
import net.coreprotect.language.Phrase;
import net.coreprotect.patch.Patch;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatMessage;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.VersionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PurgeCommand extends Consumer {
   protected static void runCommand(final CommandSender player, boolean permission, final String[] args) {
      int resultc = args.length;
      Location location = CommandParser.parseLocation(player, args);
      Integer[] argRadius = CommandParser.parseRadius(args, player, location);
      List<Integer> argAction = CommandParser.parseAction(args);
      List<Object> argBlocks = CommandParser.parseRestricted(player, args, argAction);
      Map<Object, Boolean> argExclude = CommandParser.parseExcluded(player, args, argAction);
      List<String> argExcludeUsers = CommandParser.parseExcludedUsers(player, args);
      long[] argTime = CommandParser.parseTime(args);
      final int argWid = CommandParser.parseWorld(args, false, false);
      List<Integer> supportedActions = Arrays.asList();
      final long startTime = argTime[1] > 0L ? argTime[0] : 0L;
      final long endTime = argTime[1] > 0L ? argTime[1] : argTime[0];
      if (argBlocks != null && argExclude != null && argExcludeUsers != null) {
         if (ConfigHandler.converterRunning || ConfigHandler.migrationRunning) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
         } else if (ConfigHandler.purgeRunning) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
         } else if (!permission) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
         } else if (resultc <= 1) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co purge t:<time>"));
         } else if (endTime <= 0L) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co purge t:<time>"));
         } else if (argRadius != null) {
            Chat.sendMessage(player, new ChatMessage(Phrase.build(Phrase.INVALID_WORLD)).build());
         } else if (argWid == -1) {
            String worldName = CommandParser.parseWorldName(args, false);
            Chat.sendMessage(player, new ChatMessage(Phrase.build(Phrase.WORLD_NOT_FOUND, worldName)).build());
         } else if (player instanceof Player && endTime < 2592000L) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_MINIMUM_TIME, "30", "{1}"));
         } else if (endTime < 86400L) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_MINIMUM_TIME, "24", "{2}"));
         } else {
            for (int action : argAction) {
               if (!supportedActions.contains(action)) {
                  Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ACTION_NOT_SUPPORTED));
                  return;
               }
            }

            final StringBuilder restrict = new StringBuilder();
            final String includeBlock = "";
            String includeEntity = "";
            final boolean hasBlock = false;
            boolean item = false;
            boolean entity = false;
            final int restrictCount = 0;
            if (argBlocks.size() > 0) {
               StringBuilder includeListMaterial = new StringBuilder();
               StringBuilder includeListEntity = new StringBuilder();

               for (Object restrictTarget : argBlocks) {
                  String targetName = "";
                  if (restrictTarget instanceof Material) {
                     targetName = ((Material)restrictTarget).name();
                     if (includeListMaterial.length() == 0) {
                        includeListMaterial = includeListMaterial.append(MaterialUtils.getBlockId(targetName, false));
                     } else {
                        includeListMaterial.append(",").append(MaterialUtils.getBlockId(targetName, false));
                     }

                     int legacyId = BukkitAdapter.ADAPTER.getLegacyBlockId((Material)restrictTarget);
                     if (legacyId > 0) {
                        includeListMaterial.append(",").append(legacyId);
                     }

                     targetName = ((Material)restrictTarget).name().toLowerCase(Locale.ROOT);
                     item = !item ? !((Material)restrictTarget).isBlock() : item;
                     hasBlock = true;
                  } else if (restrictTarget instanceof EntityType) {
                     targetName = ((EntityType)restrictTarget).name();
                     if (includeListEntity.length() == 0) {
                        includeListEntity = includeListEntity.append(EntityUtils.getEntityId(targetName, false));
                     } else {
                        includeListEntity.append(",").append(EntityUtils.getEntityId(targetName, false));
                     }

                     targetName = ((EntityType)restrictTarget).name().toLowerCase(Locale.ROOT);
                     entity = true;
                  }

                  if (restrictCount == 0) {
                     restrict = restrict.append(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(targetName));
                  } else {
                     restrict.append(", ").append(targetName);
                  }

                  restrictCount++;
               }

               includeBlock = includeListMaterial.toString();
               includeEntity = includeListEntity.toString();
            }

            if (entity) {
               Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ACTION_NOT_SUPPORTED));
            } else {
               final boolean optimizeCheck = false;

               for (String arg : args) {
                  if (arg.trim().equalsIgnoreCase("#optimize")) {
                     optimizeCheck = true;
                     break;
                  }
               }

               class BasicThread implements Runnable {
                  @Override
                  public void run() {
                     try {
                        long timestamp = System.currentTimeMillis() / 1000L;
                        long timeStart = startTime > 0L ? timestamp - startTime : 0L;
                        long timeEnd = timestamp - endTime;
                        long removed = 0L;
                        Connection connection = null;

                        for (int i = 0; i <= 5; i++) {
                           connection = Database.getConnection(false, 500);
                           if (connection != null) {
                              break;
                           }

                           Thread.sleep(1000L);
                        }

                        if (connection == null) {
                           Chat.sendGlobalMessage(player, Phrase.build(Phrase.DATABASE_BUSY));
                           return;
                        }

                        if (argWid > 0) {
                           String worldName = CommandParser.parseWorldName(args, false);
                           Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_STARTED, worldName));
                        } else {
                           Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_STARTED, "#global"));
                        }

                        if (hasBlock) {
                           Chat.sendGlobalMessage(
                              player, Phrase.build(Phrase.ROLLBACK_INCLUDE, restrict.toString(), "{1}", "{1}", restrictCount == 1 ? "{1}" : "{2}")
                           );
                        }

                        Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_NOTICE_1));
                        Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_NOTICE_2));
                        ConfigHandler.purgeRunning = true;

                        while (!PurgeCommand.pausedSuccess) {
                           Thread.sleep(1L);
                        }

                        Consumer.isPaused = true;
                        String query = "";
                        PreparedStatement preparedStmt = null;
                        boolean abort = false;
                        String purgePrefix = "tmp_" + ConfigHandler.prefix;
                        if (!Config.getGlobal().MYSQL) {
                           query = "ATTACH DATABASE '" + ConfigHandler.path + ConfigHandler.sqlite + ".tmp' AS tmp_db";
                           preparedStmt = connection.prepareStatement(query);
                           preparedStmt.execute();
                           preparedStmt.close();
                           purgePrefix = "tmp_db." + ConfigHandler.prefix;
                        }

                        Integer[] lastVersion = Patch.getDatabaseVersion(connection, true);
                        boolean newVersion = VersionUtils.newVersion(lastVersion, VersionUtils.getInternalPluginVersion());
                        if (newVersion && !ConfigHandler.EDITION_BRANCH.contains("-dev")) {
                           Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_FAILED));
                           Consumer.isPaused = false;
                           ConfigHandler.purgeRunning = false;
                           return;
                        }

                        if (!Config.getGlobal().MYSQL) {
                           for (String table : ConfigHandler.databaseTables) {
                              try {
                                 query = "DROP TABLE IF EXISTS " + purgePrefix + table;
                                 preparedStmt = connection.prepareStatement(query);
                                 preparedStmt.execute();
                                 preparedStmt.close();
                              } catch (Exception var33) {
                                 var33.printStackTrace();
                              }
                           }

                           Database.createDatabaseTables(purgePrefix, false, null, Config.getGlobal().MYSQL, true);
                        }

                        List<String> purgeTables = Arrays.asList("sign", "container", "item", "skull", "session", "chat", "command", "entity", "block");
                        List<String> worldTables = Arrays.asList("sign", "container", "item", "session", "chat", "command", "block");
                        List<String> restrictTables = Arrays.asList("block");
                        List<String> excludeTables = Arrays.asList("database_lock");

                        for (String table : ConfigHandler.databaseTables) {
                           String tableName = table.replaceAll("_", " ");
                           Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_PROCESSING, tableName));
                           if (!Config.getGlobal().MYSQL) {
                              String columns = "";
                              ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + purgePrefix + table);
                              ResultSetMetaData resultSetMetaData = rs.getMetaData();
                              int columnCount = resultSetMetaData.getColumnCount();

                              for (int i = 1; i <= columnCount; i++) {
                                 String name = resultSetMetaData.getColumnName(i);
                                 if (columns.length() == 0) {
                                    columns = name;
                                 } else {
                                    columns = columns + "," + name;
                                 }
                              }

                              rs.close();
                              boolean error = false;
                              if (!excludeTables.contains(table)) {
                                 try {
                                    boolean purge = true;
                                    String timeLimit = "";
                                    if (purgeTables.contains(table)) {
                                       String blockRestriction = "(";
                                       if (hasBlock && restrictTables.contains(table)) {
                                          blockRestriction = "type NOT IN(" + includeBlock + ") OR (type IN(" + includeBlock + ") AND ";
                                       } else if (hasBlock) {
                                          purge = false;
                                       }

                                       if (argWid > 0 && worldTables.contains(table)) {
                                          timeLimit = " WHERE ("
                                             + blockRestriction
                                             + "wid = '"
                                             + argWid
                                             + "' AND (time >= '"
                                             + timeEnd
                                             + "' OR time < '"
                                             + timeStart
                                             + "'))) OR (wid != '"
                                             + argWid
                                             + "')";
                                       } else if (argWid == 0 && purge) {
                                          timeLimit = " WHERE " + blockRestriction + "(time >= '" + timeEnd + "' OR time < '" + timeStart + "'))";
                                       }
                                    }

                                    query = "INSERT INTO " + purgePrefix + table + " SELECT " + columns + " FROM " + ConfigHandler.prefix + table + timeLimit;
                                    preparedStmt = connection.prepareStatement(query);
                                    preparedStmt.execute();
                                    preparedStmt.close();
                                 } catch (Exception var38) {
                                    error = true;
                                    var38.printStackTrace();
                                 }
                              }

                              if (error) {
                                 Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_ERROR, tableName));
                                 Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_REPAIRING));

                                 try {
                                    query = "DELETE FROM " + purgePrefix + table;
                                    preparedStmt = connection.prepareStatement(query);
                                    preparedStmt.execute();
                                    preparedStmt.close();
                                 } catch (Exception var32x) {
                                    var32x.printStackTrace();
                                 }

                                 try {
                                    query = "REINDEX " + ConfigHandler.prefix + table;
                                    preparedStmt = connection.prepareStatement(query);
                                    preparedStmt.execute();
                                    preparedStmt.close();
                                 } catch (Exception var31) {
                                    var31.printStackTrace();
                                 }

                                 try {
                                    String index = " NOT INDEXED";
                                    query = "INSERT INTO " + purgePrefix + table + " SELECT " + columns + " FROM " + ConfigHandler.prefix + table + index;
                                    preparedStmt = connection.prepareStatement(query);
                                    preparedStmt.execute();
                                    preparedStmt.close();
                                 } catch (Exception var34x) {
                                    var34x.printStackTrace();
                                    abort = true;
                                    break;
                                 }

                                 try {
                                    boolean purge = purgeTables.contains(table);
                                    String blockRestrictionx = "";
                                    if (hasBlock && restrictTables.contains(table)) {
                                       blockRestrictionx = "type IN(" + includeBlock + ") AND ";
                                    } else if (hasBlock) {
                                       purge = false;
                                    }

                                    String worldRestriction = "";
                                    if (argWid > 0 && worldTables.contains(table)) {
                                       worldRestriction = " AND wid = '" + argWid + "'";
                                    } else if (argWid > 0) {
                                       purge = false;
                                    }

                                    if (purge) {
                                       query = "DELETE FROM "
                                          + purgePrefix
                                          + table
                                          + " WHERE "
                                          + blockRestrictionx
                                          + "time < '"
                                          + timeEnd
                                          + "' AND time >= '"
                                          + timeStart
                                          + "'"
                                          + worldRestriction;
                                       preparedStmt = connection.prepareStatement(query);
                                       preparedStmt.execute();
                                       preparedStmt.close();
                                    }
                                 } catch (Exception var37) {
                                    var37.printStackTrace();
                                 }
                              }

                              if (purgeTables.contains(table)) {
                                 int oldCount = 0;

                                 try {
                                    query = "SELECT COUNT(*) as count FROM " + ConfigHandler.prefix + table + " LIMIT 0, 1";
                                    preparedStmt = connection.prepareStatement(query);
                                    ResultSet resultSet = preparedStmt.executeQuery();

                                    while (resultSet.next()) {
                                       oldCount = resultSet.getInt("count");
                                    }

                                    resultSet.close();
                                    preparedStmt.close();
                                 } catch (Exception var36x) {
                                    var36x.printStackTrace();
                                 }

                                 int new_count = 0;

                                 try {
                                    query = "SELECT COUNT(*) as count FROM " + purgePrefix + table + " LIMIT 0, 1";
                                    preparedStmt = connection.prepareStatement(query);
                                    ResultSet resultSet = preparedStmt.executeQuery();

                                    while (resultSet.next()) {
                                       new_count = resultSet.getInt("count");
                                    }

                                    resultSet.close();
                                    preparedStmt.close();
                                 } catch (Exception var35) {
                                    var35.printStackTrace();
                                 }

                                 removed += oldCount - new_count;
                              }
                           }

                           if (Config.getGlobal().MYSQL) {
                              try {
                                 boolean purgex = purgeTables.contains(table);
                                 String blockRestrictionxx = "";
                                 if (hasBlock && restrictTables.contains(table)) {
                                    blockRestrictionxx = "type IN(" + includeBlock + ") AND ";
                                 } else if (hasBlock) {
                                    purgex = false;
                                 }

                                 String worldRestrictionx = "";
                                 if (argWid > 0 && worldTables.contains(table)) {
                                    worldRestrictionx = " AND wid = '" + argWid + "'";
                                 } else if (argWid > 0) {
                                    purgex = false;
                                 }

                                 if (purgex) {
                                    query = "DELETE FROM "
                                       + ConfigHandler.prefix
                                       + table
                                       + " WHERE "
                                       + blockRestrictionxx
                                       + "time < '"
                                       + timeEnd
                                       + "' AND time >= '"
                                       + timeStart
                                       + "'"
                                       + worldRestrictionx;
                                    preparedStmt = connection.prepareStatement(query);
                                    preparedStmt.execute();
                                    removed += preparedStmt.getUpdateCount();
                                    preparedStmt.close();
                                 }
                              } catch (Exception var39) {
                                 if (!ConfigHandler.serverRunning) {
                                    Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_FAILED));
                                    return;
                                 }

                                 var39.printStackTrace();
                              }
                           }
                        }

                        if (Config.getGlobal().MYSQL && optimizeCheck) {
                           Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_OPTIMIZING));

                           for (String table : ConfigHandler.databaseTables) {
                              query = "OPTIMIZE LOCAL TABLE " + ConfigHandler.prefix + table;
                              preparedStmt = connection.prepareStatement(query);
                              preparedStmt.execute();
                              preparedStmt.close();
                           }
                        }

                        connection.close();
                        if (abort) {
                           if (!Config.getGlobal().MYSQL) {
                              new File(ConfigHandler.path + ConfigHandler.sqlite + ".tmp").delete();
                           }

                           ConfigHandler.loadDatabase();
                           Chat.sendGlobalMessage(player, Color.RED + Phrase.build(Phrase.PURGE_ABORTED));
                           Consumer.isPaused = false;
                           ConfigHandler.purgeRunning = false;
                           return;
                        }

                        if (!Config.getGlobal().MYSQL) {
                           new File(ConfigHandler.path + ConfigHandler.sqlite).delete();
                           new File(ConfigHandler.path + ConfigHandler.sqlite + ".tmp").renameTo(new File(ConfigHandler.path + ConfigHandler.sqlite));
                        }

                        ConfigHandler.loadDatabase();
                        Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_SUCCESS));
                        Chat.sendGlobalMessage(
                           player, Phrase.build(Phrase.PURGE_ROWS, NumberFormat.getInstance().format(removed), removed == 1L ? "{1}" : "{2}")
                        );
                     } catch (Exception var40) {
                        Chat.sendGlobalMessage(player, Phrase.build(Phrase.PURGE_FAILED));
                        var40.printStackTrace();
                     }

                     Consumer.isPaused = false;
                     ConfigHandler.purgeRunning = false;
                  }
               }

               Runnable runnable = new BasicThread();
               Thread thread = new Thread(runnable);
               thread.start();
            }
         }
      }
   }
}
