package net.coreprotect.command;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coreprotect.command.lookup.BlockLookupThread;
import net.coreprotect.command.lookup.ChestTransactionLookupThread;
import net.coreprotect.command.lookup.StandardLookupThread;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatMessage;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LookupCommand {
   public static void runCommand(CommandSender player, Command command, boolean permission, String[] args) {
      int resultc = args.length;
      args = CommandParser.parsePage(args);
      Location lo = CommandParser.parseLocation(player, args);
      List<String> argUsers = CommandParser.parseUsers(args);
      Integer[] argRadius = CommandParser.parseRadius(args, player, lo);
      int argNoisy = CommandParser.parseNoisy(args);
      List<Integer> argAction = CommandParser.parseAction(args);
      List<Object> argBlocks = CommandParser.parseRestricted(player, args, argAction);
      Map<Object, Boolean> argExclude = CommandParser.parseExcluded(player, args, argAction);
      List<String> argExcludeUsers = CommandParser.parseExcludedUsers(player, args);
      String ts = CommandParser.parseTimeString(args);
      long[] argTime = CommandParser.parseTime(args);
      long startTime = argTime[0];
      long endTime = argTime[1];
      int argWid = CommandParser.parseWorld(args, true, true);
      int parseRows = CommandParser.parseRows(args);
      boolean count = CommandParser.parseCount(args);
      boolean worldedit = CommandParser.parseWorldEdit(args);
      boolean forceglobal = CommandParser.parseForceGlobal(args);
      boolean pageLookup = false;
      if (argBlocks != null && argExclude != null && argExcludeUsers != null) {
         if (!args[0].toLowerCase(Locale.ROOT).equals("page") || args.length == 2 && args[1].equals(args[1].replaceAll("[^0-9]", ""))) {
            int argExcluded = argExclude.size();
            int argRestricted = argBlocks.size();
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
            } else if (argWid == -1) {
               String worldName = CommandParser.parseWorldName(args, true);
               Chat.sendMessage(player, new ChatMessage(Phrase.build(Phrase.WORLD_NOT_FOUND, worldName)).build());
            } else {
               int type = 0;
               if (ConfigHandler.lookupType.get(player.getName()) != null) {
                  type = ConfigHandler.lookupType.get(player.getName());
               }

               if (type == 0 && resultc > 1) {
                  type = 4;
               } else if (resultc > 2) {
                  type = 4;
               } else if (resultc > 1) {
                  pageLookup = true;
                  String dat = args[1];
                  if (dat.contains(":")) {
                     String[] split = dat.split(":");
                     String check1 = split[0].replaceAll("[^a-zA-Z_]", "");
                     String check2 = "";
                     if (split.length > 1) {
                        check2 = split[1].replaceAll("[^a-zA-Z_]", "");
                     }

                     if (check1.length() > 0 || check2.length() > 0) {
                        type = 4;
                        pageLookup = false;
                     }
                  } else {
                     String check1x = dat.replaceAll("[^a-zA-Z_]", "");
                     if (check1x.length() > 0) {
                        type = 4;
                        pageLookup = false;
                     }
                  }
               }

               if (argAction.contains(6) || argAction.contains(7) || argAction.contains(8) || argAction.contains(9) || argAction.contains(10)) {
                  pageLookup = true;
               }

               if (permission || pageLookup && player.hasPermission("coreprotect.inspect")) {
                  if (ConfigHandler.converterRunning) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
                  } else if (ConfigHandler.purgeRunning) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
                  } else if (resultc < 2) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co l <params>"));
                  } else if (argAction.contains(-1)) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_ACTION));
                  } else if (worldedit && argRadius == null) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_SELECTION, "WorldEdit"));
                  } else if (argRadius != null && argRadius[0] == -1) {
                     Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_RADIUS));
                  } else {
                     if (ConfigHandler.lookupThrottle.get(player.getName()) != null) {
                        Object[] lookupThrottle = ConfigHandler.lookupThrottle.get(player.getName());
                        if ((Boolean)lookupThrottle[0] || System.currentTimeMillis() - (Long)lookupThrottle[1] < 50L) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                           return;
                        }
                     }

                     boolean allPermission = false;
                     if (args[0].equals("near") && player.hasPermission("coreprotect.lookup.near")) {
                        allPermission = true;
                     }

                     if (!allPermission) {
                        if (!pageLookup
                           && (argAction.size() == 0 || argAction.size() == 1 && (argAction.contains(0) || argAction.contains(1)))
                           && !player.hasPermission("coreprotect.lookup.block")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(2) && !player.hasPermission("coreprotect.lookup.click")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(3) && !player.hasPermission("coreprotect.lookup.kill")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(4) && !argAction.contains(11) && !player.hasPermission("coreprotect.lookup.container")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(6) && !player.hasPermission("coreprotect.lookup.chat")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(7) && !player.hasPermission("coreprotect.lookup.command")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(8) && !player.hasPermission("coreprotect.lookup.session")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(9) && !player.hasPermission("coreprotect.lookup.username")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(10) && !player.hasPermission("coreprotect.lookup.sign")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(11) && !argAction.contains(4) && !player.hasPermission("coreprotect.lookup.item")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }

                        if (argAction.contains(4) && argAction.contains(11) && !player.hasPermission("coreprotect.lookup.inventory")) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                           return;
                        }
                     }

                     if (argAction.contains(6) || argAction.contains(7) || argAction.contains(8) || argAction.contains(9) || argAction.contains(10)) {
                        if (argAction.contains(9) && (argRadius != null || argWid > 0 || worldedit)) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INCOMPATIBLE_ACTION, "r:"));
                           return;
                        }

                        if (argBlocks.size() > 0) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INCOMPATIBLE_ACTION, "i:"));
                           return;
                        }

                        if (argExclude.size() > 0) {
                           Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INCOMPATIBLE_ACTION, "e:"));
                           return;
                        }
                     }

                     if (startTime > 0L || pageLookup || type != 4 || argBlocks.size() <= 0 && argUsers.size() <= 0) {
                        if (argAction.contains(4) && argAction.contains(11)) {
                           if (argUsers.size() == 0) {
                              Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_ACTION_USER));
                              return;
                           }

                           argExclude.put(Material.FIRE, false);
                           argExclude.put(Material.WATER, false);
                           argExclude.put(Material.FARMLAND, false);
                           argExcludeUsers.add("#hopper");
                        }

                        if (type == 1) {
                           boolean defaultRe = true;
                           int p = 0;
                           int re = 7;
                           if (parseRows > 0) {
                              re = parseRows;
                           }

                           if (resultc > 1) {
                              String pages = args[1];
                              if (pages.contains(":")) {
                                 String[] data = pages.split(":");
                                 pages = data[0];
                                 String results = "";
                                 if (data.length > 1) {
                                    results = data[1];
                                 }

                                 results = results.replaceAll("[^0-9]", "");
                                 if (results.length() > 0 && results.length() < 10) {
                                    int r = Integer.parseInt(results);
                                    if (r > 0) {
                                       re = r;
                                       defaultRe = false;
                                    }
                                 }
                              }

                              pages = pages.replaceAll("[^0-9]", "");
                              if (pages.length() > 0 && pages.length() < 10) {
                                 int pa = Integer.parseInt(pages);
                                 if (pa > 0) {
                                    p = pa;
                                 }
                              }
                           }

                           if (re > 1000) {
                              re = 1000;
                           }

                           if (re > 100 && !(player instanceof ConsoleCommandSender)) {
                              re = 100;
                           }

                           if (p <= 0) {
                              p = 1;
                           }

                           String lcommand = ConfigHandler.lookupCommand.get(player.getName());
                           String[] datax = lcommand.split("\\.");
                           int x = Integer.parseInt(datax[0]);
                           int y = Integer.parseInt(datax[1]);
                           int z = Integer.parseInt(datax[2]);
                           int wid = Integer.parseInt(datax[3]);
                           int x2 = Integer.parseInt(datax[4]);
                           int y2 = Integer.parseInt(datax[5]);
                           int z2 = Integer.parseInt(datax[6]);
                           if (defaultRe) {
                              re = Integer.parseInt(datax[7]);
                           }

                           String bc = x + "." + y + "." + z + "." + wid + "." + x2 + "." + y2 + "." + z2 + "." + re;
                           ConfigHandler.lookupCommand.put(player.getName(), bc);
                           String world = WorldUtils.getWorldName(wid);
                           double dx = 0.5 * (x + x2);
                           double dy = 0.5 * (y + y2);
                           double dz = 0.5 * (z + z2);
                           Location location = new Location(Bukkit.getServer().getWorld(world), dx, dy, dz);
                           Runnable runnable = new ChestTransactionLookupThread(player, command, location, p, re);
                           Thread thread = new Thread(runnable);
                           thread.start();
                        } else if (type == 2 || type == 3 || type == 7 || type == 8) {
                           boolean defaultRex = true;
                           int page = 1;
                           int rex = 7;
                           if (parseRows > 0) {
                              rex = parseRows;
                           }

                           if (resultc > 1) {
                              String pagesx = args[1];
                              if (pagesx.contains(":")) {
                                 String[] datax = pagesx.split(":");
                                 pagesx = datax[0];
                                 String resultsx = "";
                                 if (datax.length > 1) {
                                    resultsx = datax[1];
                                 }

                                 resultsx = resultsx.replaceAll("[^0-9]", "");
                                 if (resultsx.length() > 0 && resultsx.length() < 10) {
                                    int r = Integer.parseInt(resultsx);
                                    if (r > 0) {
                                       rex = r;
                                       defaultRex = false;
                                    }
                                 }
                              }

                              pagesx = pagesx.replaceAll("[^0-9]", "");
                              if (pagesx.length() > 0 && pagesx.length() < 10) {
                                 int px = Integer.parseInt(pagesx);
                                 if (px > 0) {
                                    page = px;
                                 }
                              }
                           }

                           if (rex > 1000) {
                              rex = 1000;
                           }

                           if (rex > 100 && !(player instanceof ConsoleCommandSender)) {
                              rex = 100;
                           }

                           String lcommand = ConfigHandler.lookupCommand.get(player.getName());
                           String[] dataxx = lcommand.split("\\.");
                           int x = Integer.parseInt(dataxx[0]);
                           int y = Integer.parseInt(dataxx[1]);
                           int z = Integer.parseInt(dataxx[2]);
                           int wid = Integer.parseInt(dataxx[3]);
                           int lookupType = Integer.parseInt(dataxx[4]);
                           if (defaultRex) {
                              rex = Integer.parseInt(dataxx[5]);
                           }

                           String bc = x + "." + y + "." + z + "." + wid + "." + lookupType + "." + rex;
                           ConfigHandler.lookupCommand.put(player.getName(), bc);
                           String world = WorldUtils.getWorldName(wid);
                           Block block = Bukkit.getServer().getWorld(world).getBlockAt(x, y, z);
                           BlockState blockState = block.getState();
                           Runnable runnable = new BlockLookupThread(player, command, block, blockState, page, rex, type);
                           Thread thread = new Thread(runnable);
                           thread.start();
                        } else if (type != 4 && type != 5) {
                           Chat.sendMessage(
                              player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co l <params>")
                           );
                        } else {
                           boolean defaultRexx = true;
                           int pa = 1;
                           int rexx = 4;
                           if (argAction.contains(6) || argAction.contains(7) || argAction.contains(9) || argAction.contains(4) && argAction.contains(11)) {
                              rexx = 7;
                           }

                           if (parseRows > 0) {
                              rexx = parseRows;
                           }

                           if (type == 5 && resultc > 1) {
                              String pagesxx = args[1];
                              if (pagesxx.contains(":")) {
                                 String[] dataxx = pagesxx.split(":");
                                 pagesxx = dataxx[0];
                                 String resultsxx = "";
                                 if (dataxx.length > 1) {
                                    resultsxx = dataxx[1];
                                 }

                                 resultsxx = resultsxx.replaceAll("[^0-9]", "");
                                 if (resultsxx.length() > 0 && resultsxx.length() < 10) {
                                    int r = Integer.parseInt(resultsxx);
                                    if (r > 0) {
                                       rexx = r;
                                       defaultRexx = false;
                                    }
                                 }
                              }

                              pagesxx = pagesxx.replaceAll("[^0-9]", "");
                              if (pagesxx.length() > 0 && pagesxx.length() < 10) {
                                 int px = Integer.parseInt(pagesxx);
                                 if (px > 0) {
                                    pa = px;
                                 }
                              }
                           }

                           if (rexx > 1000) {
                              rexx = 1000;
                           }

                           if (rexx > 100 && !(player instanceof ConsoleCommandSender)) {
                              rexx = 100;
                           }

                           int g = 1;
                           if (argUsers.contains("#global") && argRadius == null) {
                              g = 0;
                           }

                           if (g == 1 && (pageLookup || argBlocks.size() > 0 || argUsers.size() > 0 || argUsers.size() == 0 && argRadius != null)) {
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
                                          + Phrase.build(Phrase.MAXIMUM_RADIUS, MAX_RADIUS.toString(), "{1}")
                                    );
                                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.GLOBAL_LOOKUP));
                                    return;
                                 }
                              }

                              if (argUsers.size() == 0) {
                                 argUsers.add("#global");
                              }

                              List<String> rollbackusers = argUsers;
                              int c = 0;

                              for (String ruser : argUsers) {
                                 if (Bukkit.getServer() != null) {
                                    for (Player px : Bukkit.getServer().matchPlayer(ruser)) {
                                       if (px.getName().equalsIgnoreCase(ruser)) {
                                          rollbackusers.set(c, px.getName());
                                       }
                                    }
                                 }

                                 c++;
                                 if (argAction.contains(4) && argAction.contains(11) && ruser.startsWith("#")) {
                                    Chat.sendMessage(
                                       player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_USERNAME, ruser)
                                    );
                                    return;
                                 }
                              }

                              long timeStart = -1L;
                              long timeEnd = 0L;
                              int x = 0;
                              int y = 0;
                              int z = 0;
                              int wid = 0;
                              if (type == 5) {
                                 String lcommand = ConfigHandler.lookupCommand.get(player.getName());
                                 String[] dataxxx = lcommand.split("\\.");
                                 x = Integer.parseInt(dataxxx[0]);
                                 y = Integer.parseInt(dataxxx[1]);
                                 z = Integer.parseInt(dataxxx[2]);
                                 wid = Integer.parseInt(dataxxx[3]);
                                 timeStart = Long.parseLong(dataxxx[4]);
                                 timeEnd = Long.parseLong(dataxxx[5]);
                                 argNoisy = Integer.parseInt(dataxxx[6]);
                                 argExcluded = Integer.parseInt(dataxxx[7]);
                                 argRestricted = Integer.parseInt(dataxxx[8]);
                                 argWid = Integer.parseInt(dataxxx[9]);
                                 if (defaultRexx) {
                                    rexx = Integer.parseInt(dataxxx[10]);
                                 }

                                 rollbackusers = ConfigHandler.lookupUlist.get(player.getName());
                                 argBlocks = ConfigHandler.lookupBlist.get(player.getName());
                                 argExclude = ConfigHandler.lookupElist.get(player.getName());
                                 argExcludeUsers = ConfigHandler.lookupEUserlist.get(player.getName());
                                 argAction = ConfigHandler.lookupAlist.get(player.getName());
                                 argRadius = ConfigHandler.lookupRadius.get(player.getName());
                                 ts = ConfigHandler.lookupTime.get(player.getName());
                                 startTime = 1L;
                                 endTime = 0L;
                              } else {
                                 if (lo != null) {
                                    x = lo.getBlockX();
                                    z = lo.getBlockZ();
                                    if (lo.getWorld() != null) {
                                       wid = WorldUtils.getWorldId(lo.getWorld().getName());
                                    }
                                 }

                                 if (rollbackusers.size() == 1 && rollbackusers.contains("#global") && argAction.contains(9)) {
                                    Chat.sendMessage(
                                       player,
                                       Color.DARK_AQUA
                                          + "CoreProtect "
                                          + Color.WHITE
                                          + "- "
                                          + Phrase.build(Phrase.MISSING_PARAMETERS, "/co l a:username u:<user>")
                                    );
                                    return;
                                 }

                                 if (rollbackusers.contains("#container")) {
                                    if (argAction.contains(6)
                                       || argAction.contains(7)
                                       || argAction.contains(8)
                                       || argAction.contains(9)
                                       || argAction.contains(10)
                                       || argAction.contains(11)) {
                                       Chat.sendMessage(
                                          player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_USERNAME, "#container")
                                       );
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

                                    if (!player.hasPermission("coreprotect.lookup.container") && !allPermission) {
                                       Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
                                       return;
                                    }

                                    String lcommand = ConfigHandler.lookupCommand.get(player.getName());
                                    String[] dataxxx = lcommand.split("\\.");
                                    x = Integer.parseInt(dataxxx[0]);
                                    y = Integer.parseInt(dataxxx[1]);
                                    z = Integer.parseInt(dataxxx[2]);
                                    wid = Integer.parseInt(dataxxx[3]);
                                    argAction.add(5);
                                    argRadius = null;
                                    argWid = 0;
                                 }
                              }

                              try {
                                 Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- §o" + Phrase.build(Phrase.LOOKUP_SEARCHING));
                                 if (timeStart == -1L) {
                                    if (startTime <= 0L) {
                                       timeStart = 0L;
                                    } else {
                                       timeStart = System.currentTimeMillis() / 1000L - startTime;
                                    }

                                    if (endTime <= 0L) {
                                       timeEnd = 0L;
                                    } else {
                                       timeEnd = System.currentTimeMillis() / 1000L - endTime;
                                    }
                                 }

                                 Runnable runnable = new StandardLookupThread(
                                    player,
                                    command,
                                    rollbackusers,
                                    argBlocks,
                                    argExclude,
                                    argExcludeUsers,
                                    argAction,
                                    argRadius,
                                    lo,
                                    x,
                                    y,
                                    z,
                                    wid,
                                    argWid,
                                    timeStart,
                                    timeEnd,
                                    argNoisy,
                                    argExcluded,
                                    argRestricted,
                                    pa,
                                    rexx,
                                    type,
                                    ts,
                                    count
                                 );
                                 Thread thread = new Thread(runnable);
                                 thread.start();
                              } catch (Exception var54) {
                                 var54.printStackTrace();
                              }
                           } else {
                              if (argUsers.size() == 0 && argBlocks.size() == 0 && (argWid > 0 || forceglobal)) {
                                 Chat.sendMessage(
                                    player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_LOOKUP_USER, "{1}")
                                 );
                                 return;
                              }

                              if (argUsers.size() == 0 && argBlocks.size() == 0 && argRadius == null) {
                                 Chat.sendMessage(
                                    player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_LOOKUP_USER, "{2}")
                                 );
                                 return;
                              }

                              Chat.sendMessage(
                                 player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co l <params>")
                              );
                           }
                        }
                     } else {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_LOOKUP_TIME, "{1}"));
                     }
                  }
               } else {
                  Chat.sendMessage(player, new ChatMessage(Phrase.build(Phrase.NO_PERMISSION)).build());
               }
            }
         } else {
            Chat.sendMessage(
               player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, Color.WHITE, "/co page <page>")
            );
         }
      }
   }
}
