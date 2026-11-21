package net.coreprotect.command;

import java.lang.invoke.StringConcatFactory;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.ContainerRollback;
import net.coreprotect.database.Database;
import net.coreprotect.database.lookup.PlayerLookup;
import net.coreprotect.database.rollback.Rollback;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class RollbackRestoreCommand {
   public static void runCommand(
      final CommandSender player, Command command, boolean permission, final String[] args, Location argLocation, long forceStart, long forceEnd
   ) {
      final Location lo = argLocation != null ? argLocation : CommandParser.parseLocation(player, args);
      final List<String> argUuids = new ArrayList<>();
      List<String> argUsers = CommandParser.parseUsers(args);
      Integer[] argRadius = CommandParser.parseRadius(args, player, lo);
      final int argNoisy = CommandParser.parseNoisy(args);
      final List<Integer> argAction = CommandParser.parseAction(args);
      final List<Object> argBlocks = CommandParser.parseRestricted(player, args, argAction);
      final Map<Object, Boolean> argExclude = CommandParser.parseExcluded(player, args, argAction);
      final List<String> argExcludeUsers = CommandParser.parseExcludedUsers(player, args);
      final String ts = CommandParser.parseTimeString(args);
      long[] argTime = CommandParser.parseTime(args);
      long startTime = argTime[0];
      long endTime = argTime[1];
      final int argWid = CommandParser.parseWorld(args, true, true);
      boolean count = CommandParser.parseCount(args);
      boolean worldedit = CommandParser.parseWorldEdit(args);
      boolean forceglobal = CommandParser.parseForceGlobal(args);
      final int preview = CommandParser.parsePreview(args);
      String corecommand = args[0].toLowerCase(Locale.ROOT);
      if (argBlocks != null && argExclude != null && argExcludeUsers != null) {
         boolean hasBlock = false;
         boolean hasEntity = false;

         for (Object arg : argBlocks) {
            if (arg instanceof Material) {
               hasBlock = true;
            } else if (arg instanceof EntityType) {
               hasEntity = true;
               if (argAction.size() == 0) {
                  argAction.add(3);
               } else if (!argAction.contains(3)) {
                  Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE_COMBO));
                  return;
               }
            }
         }

         for (Object argx : argExclude.keySet()) {
            if (argx instanceof Material) {
               hasBlock = true;
            } else if (argx instanceof EntityType) {
               hasEntity = true;
               if (argAction.size() == 0) {
                  argAction.add(3);
               } else if (!argAction.contains(3)) {
                  Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE_COMBO));
                  return;
               }
            }
         }

         if (hasBlock && hasEntity) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE_COMBO));
         } else if (count) {
            LookupCommand.runCommand(player, command, permission, args);
         } else if (ConfigHandler.converterRunning) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
         } else if (ConfigHandler.purgeRunning) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
         } else if (argWid == -1) {
            String worldName = CommandParser.parseWorldName(args, true);
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.WORLD_NOT_FOUND, worldName));
         } else if (preview > 0 && !(player instanceof Player)) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PREVIEW_IN_GAME));
         } else if (argAction.contains(-1)) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_ACTION));
         } else if (worldedit && argRadius == null) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_SELECTION, "WorldEdit"));
         } else if (argRadius != null && argRadius[0] == -1) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_RADIUS));
         } else if (ConfigHandler.activeRollbacks.get(player.getName()) != null) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_IN_PROGRESS));
         } else {
            if (ConfigHandler.lookupThrottle.get(player.getName()) != null) {
               Object[] lookupThrottle = ConfigHandler.lookupThrottle.get(player.getName());
               if ((Boolean)lookupThrottle[0] || System.currentTimeMillis() - (Long)lookupThrottle[1] < 100L) {
                  Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                  return;
               }
            }

            if (preview > 1 && forceStart <= 0L) {
               preview = 1;
            }

            if (!permission) {
               Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
            } else {
               int a = 0;
               if (corecommand.equals("restore") || corecommand.equals("rs") || corecommand.equals("re")) {
                  a = 1;
               }

               final int finalAction = a;
               int DEFAULT_RADIUS = Config.getGlobal().DEFAULT_RADIUS;
               if ((player instanceof Player || player instanceof BlockCommandSender)
                  && argRadius == null
                  && DEFAULT_RADIUS > 0
                  && !forceglobal
                  && !argAction.contains(11)) {
                  int xmin = lo.getBlockX() - DEFAULT_RADIUS;
                  int xmax = lo.getBlockX() + DEFAULT_RADIUS;
                  int zmin = lo.getBlockZ() - DEFAULT_RADIUS;
                  int zmax = lo.getBlockZ() + DEFAULT_RADIUS;
                  argRadius = new Integer[]{DEFAULT_RADIUS, xmin, xmax, null, null, zmin, zmax, 0};
               }

               int g = 1;
               if (argUsers.contains("#global") && argRadius == null) {
                  g = 0;
               }

               if (argUsers.size() == 0 && (argWid > 0 || forceglobal) && argRadius == null) {
                  if (a == 0) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ROLLBACK_USER, "{1}"));
                  } else {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ROLLBACK_USER, "{2}"));
                  }

                  return;
               }

               if (argAction.contains(4) && argAction.contains(11)) {
                  if (argUsers.size() == 0) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ACTION_USER));
                     return;
                  }

                  argExclude.put(Material.FIRE, false);
                  argExclude.put(Material.WATER, false);
                  argExclude.put(Material.FARMLAND, false);
                  argExcludeUsers.add("#hopper");
               } else if (!argAction.contains(4)
                  && Config.getGlobal().EXCLUDE_TNT
                  && !argExclude.containsKey(Material.TNT)
                  && !argBlocks.contains(Material.TNT)) {
                  argExclude.put(Material.TNT, true);
               }

               if (g == 1 && (argUsers.size() > 0 || argUsers.size() == 0 && argRadius != null)) {
                  Integer MAX_RADIUS = Config.getGlobal().MAX_RADIUS;
                  if (argRadius != null) {
                     int radiusValue = argRadius[0];
                     if (radiusValue > MAX_RADIUS && MAX_RADIUS > 0) {
                        Chat.sendMessage(
                           player,
                           Color.DARK_AQUA
                              + "CoreProtect "
                              + Color.WHITE
                              + "- "
                              + Phrase.build(Phrase.MAXIMUM_RADIUS, MAX_RADIUS.toString(), a == 0 ? "{2}" : "{3}")
                        );
                        Chat.sendMessage(
                           player,
                           Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.GLOBAL_ROLLBACK, "r:#global", a == 0 ? "{1}" : "{2}")
                        );
                        return;
                     }
                  }

                  if (argAction.size() > 0) {
                     if (argAction.contains(4)) {
                        if (argUsers.contains("#global") || argUsers.size() == 0 && argRadius == null) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ACTION_USER));
                           return;
                        }

                        if (preview > 0) {
                           Chat.sendMessage(
                              player,
                              Color.DARK_AQUA
                                 + "CoreProtect "
                                 + Color.WHITE
                                 + "- "
                                 + Phrase.build(Phrase.PREVIEW_TRANSACTION, !argAction.contains(11) ? "{1}" : "{2}")
                           );
                           return;
                        }
                     }

                     if (argAction.contains(8)
                        || argAction.contains(11) && !argAction.contains(4)
                        || !argAction.contains(0) && !argAction.contains(1) && !argAction.contains(3) && !argAction.contains(4)) {
                        if (a == 0) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ACTION_NOT_SUPPORTED));
                        } else {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ACTION_NOT_SUPPORTED));
                        }

                        return;
                     }
                  }

                  if (argUsers.size() == 0) {
                     argUsers.add("#global");
                  }

                  List<String> rollbackusers = argUsers;
                  int c = 0;

                  for (String ruser : argUsers) {
                     for (Player p : Bukkit.getServer().matchPlayer(ruser)) {
                        if (p.getName().equalsIgnoreCase(ruser)) {
                           ruser = p.getName();
                           rollbackusers.set(c, ruser);
                        }
                     }

                     c++;
                     if (argAction.contains(4) && argAction.contains(11)) {
                        Player onlineUser = Bukkit.getServer().getPlayer(ruser);
                        if (onlineUser == null || !onlineUser.isOnline()) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.USER_OFFLINE, ruser));
                           return;
                        }
                     }
                  }

                  int wid = 0;
                  int x = 0;
                  int y = 0;
                  int z = 0;
                  if (rollbackusers.contains("#container")) {
                     if (argAction.contains(11)) {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_USERNAME, "#container"));
                        return;
                     }

                     boolean valid = false;
                     if (ConfigHandler.lookupType.get(player.getName()) != null) {
                        int lookupType = ConfigHandler.lookupType.get(player.getName());
                        if (lookupType == 1) {
                           valid = true;
                        } else if (lookupType == 5 && ConfigHandler.lookupUlist.get(player.getName()).contains("#container")) {
                           valid = true;
                        }
                     }

                     if (!valid) {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_CONTAINER));
                        return;
                     }

                     if (preview > 0) {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PREVIEW_TRANSACTION, "{1}"));
                        return;
                     }

                     String lcommand = ConfigHandler.lookupCommand.get(player.getName());
                     String[] data = lcommand.split("\\.");
                     x = Integer.parseInt(data[0]);
                     y = Integer.parseInt(data[1]);
                     z = Integer.parseInt(data[2]);
                     wid = Integer.parseInt(data[3]);
                     argAction.add(5);
                     argRadius = null;
                     argWid = 0;
                     lo = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(wid)), x, y, z);
                     Block block = lo.getBlock();
                     if (block.getState() instanceof Chest) {
                        BlockFace[] blockFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

                        for (BlockFace face : blockFaces) {
                           if (block.getRelative(face, 1).getState() instanceof Chest) {
                              Block relative = block.getRelative(face, 1);
                              int x2 = relative.getX();
                              int z2 = relative.getZ();
                              double newX = (x + x2) / 2.0;
                              double newZ = (z + z2) / 2.0;
                              lo.setX(newX);
                              lo.setZ(newZ);
                              break;
                           }
                        }
                     }
                  }

                  final List<String> rollbackusers2 = rollbackusers;
                  if (startTime > 0L) {
                     long unixtimestamp = System.currentTimeMillis() / 1000L;
                     long timeStart = unixtimestamp - startTime;
                     long timeEnd = endTime > 0L ? unixtimestamp - endTime : 0L;
                     if (forceStart > 0L) {
                        timeStart = forceStart;
                        timeEnd = forceEnd;
                     }

                     final long finalTimeStart = timeStart;
                     final long finalTimeEnd = timeEnd;
                     final Integer[] radius = argRadius;

                     try {
                        ConfigHandler.activeRollbacks.put(player.getName(), true);

                        class BasicThread2 implements Runnable {
                           @Override
                           public void run() {
                              try (Connection connection = Database.getConnection(false, 1000)) {
                                 ConfigHandler.lookupThrottle.put(player.getName(), new Object[]{true, System.currentTimeMillis()});
                                 int action = finalAction;
                                 Location location = lo;
                                 if (connection == null) {
                                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                                 } else {
                                    Statement statement = connection.createStatement();
                                    String baduser = "";
                                    boolean exists = false;

                                    for (String check : rollbackusers2) {
                                       if (!check.equals("#global") && !check.equals("#container")) {
                                          exists = PlayerLookup.playerExists(connection, check);
                                          if (!exists) {
                                             baduser = check;
                                             break;
                                          }
                                       } else {
                                          exists = true;
                                       }
                                    }

                                    if (exists) {
                                       for (String checkx : argExcludeUsers) {
                                          if (!checkx.equals("#global") && !checkx.equals("#hopper")) {
                                             exists = PlayerLookup.playerExists(connection, checkx);
                                             if (!exists) {
                                                baduser = checkx;
                                                break;
                                             }
                                          } else if (checkx.equals("#global")) {
                                             baduser = "#global";
                                             exists = false;
                                          }
                                       }
                                    }

                                    if (exists) {
                                       boolean restrictWorld = false;
                                       if (radius != null) {
                                          restrictWorld = true;
                                       }

                                       if (location == null) {
                                          restrictWorld = false;
                                       }

                                       if (argWid > 0) {
                                          restrictWorld = true;
                                          location = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(argWid)), 0.0, 0.0, 0.0);
                                       }

                                       boolean verbose = false;
                                       if (argNoisy == 1) {
                                          verbose = true;
                                       }

                                       String users = "";

                                       for (String value : rollbackusers2) {
                                          if (users.length() == 0) {
                                             users = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value);
                                          } else {
                                             users = users + ", " + value;
                                          }
                                       }

                                       if (users.equals("#global") && restrictWorld) {
                                          users = "#" + location.getWorld().getName();
                                       }

                                       if (preview == 2) {
                                          Chat.sendMessage(
                                             player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PREVIEW_CANCELLING)
                                          );
                                       } else if (preview == 1) {
                                          Chat.sendMessage(
                                             player,
                                             Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_STARTED, users, "{3}")
                                          );
                                       } else if (action == 0) {
                                          Chat.sendMessage(
                                             player,
                                             Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_STARTED, users, "{1}")
                                          );
                                       } else {
                                          Chat.sendMessage(
                                             player,
                                             Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_STARTED, users, "{2}")
                                          );
                                       }

                                       if (argAction.contains(5)) {
                                          ContainerRollback.performContainerRollbackRestore(
                                             statement,
                                             player,
                                             argUuids,
                                             rollbackusers2,
                                             ts,
                                             argBlocks,
                                             argExclude,
                                             argExcludeUsers,
                                             argAction,
                                             location,
                                             radius,
                                             finalTimeStart,
                                             finalTimeEnd,
                                             restrictWorld,
                                             false,
                                             verbose,
                                             action
                                          );
                                       } else {
                                          Rollback.performRollbackRestore(
                                             statement,
                                             player,
                                             argUuids,
                                             rollbackusers2,
                                             ts,
                                             argBlocks,
                                             argExclude,
                                             argExcludeUsers,
                                             argAction,
                                             location,
                                             radius,
                                             finalTimeStart,
                                             finalTimeEnd,
                                             restrictWorld,
                                             false,
                                             verbose,
                                             action,
                                             preview
                                          );
                                       }

                                       if (preview < 2) {
                                          List<Object> list = new ArrayList<>();
                                          list.add(finalTimeStart);
                                          list.add(finalTimeEnd);
                                          list.add(args);
                                          list.add(lo);
                                          ConfigHandler.lastRollback.put(player.getName(), list);
                                       }
                                    } else {
                                       Chat.sendMessage(
                                          player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.USER_NOT_FOUND, baduser)
                                       );
                                    }

                                    statement.close();
                                 }
                              } catch (Exception var14) {
                                 var14.printStackTrace();
                              }

                              if (ConfigHandler.activeRollbacks.get(player.getName()) != null) {
                                 ConfigHandler.activeRollbacks.remove(player.getName());
                              }

                              ConfigHandler.lookupThrottle.put(player.getName(), new Object[]{false, System.currentTimeMillis()});
                           }
                        }

                        Runnable runnable = new BasicThread2();
                        Thread thread = new Thread(runnable);
                        thread.start();
                     } catch (Exception var69) {
                        var69.printStackTrace();
                     }
                  } else if (a == 0) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_LOOKUP_TIME, "{2}"));
                  } else {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_LOOKUP_TIME, "{3}"));
                  }
               } else if (a == 0) {
                  Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ROLLBACK_RADIUS, "{1}"));
               } else {
                  Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ROLLBACK_RADIUS, "{2}"));
               }
            }
         }
      }
   }
}
