package net.coreprotect.command.lookup;

import com.google.common.base.Strings;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.database.lookup.PlayerLookup;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.channel.PluginChannelHandshakeListener;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.StringUtils;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class StandardLookupThread implements Runnable {
   private final CommandSender player;
   private final Command command;
   private final List<String> rollbackUsers;
   private final List<Object> blockList;
   private final Map<Object, Boolean> excludedBlocks;
   private final List<String> excludedUsers;
   private final List<Integer> actions;
   private final Integer[] radius;
   private final Location location;
   private final int x;
   private final int y;
   private final int z;
   private final int worldId;
   private final int argWorldId;
   private final long timeStart;
   private final long timeEnd;
   private final int noisy;
   private final int excluded;
   private final int restricted;
   private final int page;
   private final int displayResults;
   private final int typeLookup;
   private final String rtime;
   private final boolean count;

   public StandardLookupThread(
      CommandSender player,
      Command command,
      List<String> rollbackUsers,
      List<Object> blockList,
      Map<Object, Boolean> excludedBlocks,
      List<String> excludedUsers,
      List<Integer> actions,
      Integer[] radius,
      Location location,
      int x,
      int y,
      int z,
      int worldId,
      int argWorldId,
      long timeStart,
      long timeEnd,
      int noisy,
      int excluded,
      int restricted,
      int page,
      int displayResults,
      int typeLookup,
      String rtime,
      boolean count
   ) {
      this.player = player;
      this.command = command;
      this.rollbackUsers = rollbackUsers;
      this.blockList = blockList;
      this.excludedBlocks = excludedBlocks;
      this.excludedUsers = excludedUsers;
      this.actions = actions;
      this.radius = radius;
      this.location = location;
      this.x = x;
      this.y = y;
      this.z = z;
      this.worldId = worldId;
      this.argWorldId = argWorldId;
      this.timeStart = timeStart;
      this.timeEnd = timeEnd;
      this.noisy = noisy;
      this.excluded = excluded;
      this.restricted = restricted;
      this.page = page;
      this.displayResults = displayResults;
      this.typeLookup = typeLookup;
      this.rtime = rtime;
      this.count = count;
   }

   @Override
   public void run() {
      try (Connection connection = Database.getConnection(true)) {
         ConfigHandler.lookupThrottle.put(this.player.getName(), new Object[]{true, System.currentTimeMillis()});
         List<String> uuidList = new ArrayList<>();
         Location finalLocation = this.location;
         boolean exists = false;
         String bc = this.x
            + "."
            + this.y
            + "."
            + this.z
            + "."
            + this.worldId
            + "."
            + this.timeStart
            + "."
            + this.timeEnd
            + "."
            + this.noisy
            + "."
            + this.excluded
            + "."
            + this.restricted
            + "."
            + this.argWorldId
            + "."
            + this.displayResults;
         ConfigHandler.lookupCommand.put(this.player.getName(), bc);
         ConfigHandler.lookupPage.put(this.player.getName(), this.page);
         ConfigHandler.lookupTime.put(this.player.getName(), this.rtime);
         ConfigHandler.lookupType.put(this.player.getName(), 5);
         ConfigHandler.lookupElist.put(this.player.getName(), this.excludedBlocks);
         ConfigHandler.lookupEUserlist.put(this.player.getName(), this.excludedUsers);
         ConfigHandler.lookupBlist.put(this.player.getName(), this.blockList);
         ConfigHandler.lookupUlist.put(this.player.getName(), this.rollbackUsers);
         ConfigHandler.lookupAlist.put(this.player.getName(), this.actions);
         ConfigHandler.lookupRadius.put(this.player.getName(), this.radius);
         if (connection == null) {
            Chat.sendMessage(this.player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
         } else {
            Statement statement = connection.createStatement();
            String baduser = "";

            for (String check : this.rollbackUsers) {
               if ((check.equals("#global") || check.equals("#container")) && !this.actions.contains(9)) {
                  exists = true;
               } else {
                  exists = PlayerLookup.playerExists(connection, check);
                  if (!exists) {
                     baduser = check;
                     break;
                  }

                  if (this.actions.contains(9) && ConfigHandler.uuidCache.get(check.toLowerCase(Locale.ROOT)) != null) {
                     String uuid = ConfigHandler.uuidCache.get(check.toLowerCase(Locale.ROOT));
                     uuidList.add(uuid);
                  }
               }
            }

            if (exists) {
               for (String checkx : this.excludedUsers) {
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
               List<String> userList = new ArrayList<>();
               if (!this.actions.contains(9)) {
                  userList = this.rollbackUsers;
               }

               int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
               boolean restrict_world = false;
               if (this.radius != null) {
                  restrict_world = true;
               }

               if (finalLocation == null) {
                  restrict_world = false;
               }

               if (this.argWorldId > 0) {
                  restrict_world = true;
                  finalLocation = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(this.argWorldId)), this.x, this.y, this.z);
               } else if (finalLocation != null) {
                  finalLocation = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(this.worldId)), this.x, this.y, this.z);
               }

               Long[] rowData = new Long[]{0L, 0L, 0L, 0L};
               long rowMax = (long)this.page * this.displayResults;
               long pageStart = rowMax - this.displayResults;
               long rows = 0L;
               boolean checkRows = true;
               if (this.typeLookup == 5 && this.page > 1) {
                  rowData = ConfigHandler.lookupRows.get(this.player.getName());
                  rows = rowData[3];
                  if (pageStart < rows) {
                     checkRows = false;
                  }
               }

               if (checkRows) {
                  rows = Lookup.countLookupRows(
                     statement,
                     this.player,
                     uuidList,
                     userList,
                     this.blockList,
                     this.excludedBlocks,
                     this.excludedUsers,
                     this.actions,
                     finalLocation,
                     this.radius,
                     rowData,
                     this.timeStart,
                     this.timeEnd,
                     restrict_world,
                     true
                  );
                  rowData[3] = rows;
                  ConfigHandler.lookupRows.put(this.player.getName(), rowData);
               }

               if (this.count) {
                  String row_format = NumberFormat.getInstance().format(rows);
                  Chat.sendMessage(
                     this.player,
                     Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.LOOKUP_ROWS_FOUND, row_format, rows == 1L ? "{1}" : "{2}")
                  );
               } else if (pageStart < rows) {
                  List<String[]> lookupList = Lookup.performPartialLookup(
                     statement,
                     this.player,
                     uuidList,
                     userList,
                     this.blockList,
                     this.excludedBlocks,
                     this.excludedUsers,
                     this.actions,
                     finalLocation,
                     this.radius,
                     rowData,
                     this.timeStart,
                     this.timeEnd,
                     (int)pageStart,
                     this.displayResults,
                     restrict_world,
                     true
                  );
                  Chat.sendMessage(
                     this.player,
                     Color.WHITE
                        + "----- "
                        + Color.DARK_AQUA
                        + Phrase.build(Phrase.LOOKUP_HEADER, "CoreProtect" + Color.WHITE + " | " + Color.DARK_AQUA)
                        + Color.WHITE
                        + " -----"
                  );
                  if (!this.actions.contains(6) && !this.actions.contains(7)) {
                     if (this.actions.contains(8)) {
                        for (String[] data : lookupList) {
                           String time = data[0];
                           String dplayer = data[1];
                           int wid = Integer.parseInt(data[2]);
                           int dataX = Integer.parseInt(data[3]);
                           int dataY = Integer.parseInt(data[4]);
                           int dataZ = Integer.parseInt(data[5]);
                           int action = Integer.parseInt(data[6]);
                           String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
                           int timeLength = 50 + ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, false).replaceAll("[^0-9]", "").length() * 6;
                           String leftPadding = "§l" + Strings.padStart("", 10, ' ');
                           if (timeLength % 4 == 0) {
                              leftPadding = Strings.padStart("", timeLength / 4, ' ');
                           } else {
                              leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
                           }

                           String tag = action != 0 ? Color.GREEN + "+" : Color.RED + "-";
                           Chat.sendComponent(
                              this.player,
                              timeago
                                 + " "
                                 + tag
                                 + " "
                                 + Color.DARK_AQUA
                                 + Phrase.build(Phrase.LOOKUP_LOGIN, Color.DARK_AQUA + dplayer + Color.WHITE, action != 0 ? "{1}" : "{2}")
                           );
                           Chat.sendComponent(
                              this.player,
                              Color.WHITE
                                 + leftPadding
                                 + Color.GREY
                                 + "^ "
                                 + ChatUtils.getCoordinates(this.command.getName(), wid, dataX, dataY, dataZ, true, true)
                           );
                           PluginChannelListener.getInstance()
                              .sendInfoData(
                                 this.player, Integer.parseInt(time), Phrase.LOOKUP_LOGIN, action != 0 ? "{1}" : "{2}", dplayer, -1, dataX, dataY, dataZ, wid
                              );
                        }
                     } else if (this.actions.contains(9)) {
                        for (String[] data : lookupList) {
                           String time = data[0];
                           String user = ConfigHandler.uuidCacheReversed.get(data[1]);
                           String username = data[2];
                           String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
                           Chat.sendComponent(
                              this.player,
                              timeago
                                 + " "
                                 + Color.WHITE
                                 + "- "
                                 + Phrase.build(Phrase.LOOKUP_USERNAME, Color.DARK_AQUA + user + Color.WHITE, Color.DARK_AQUA + username + Color.WHITE)
                           );
                           PluginChannelListener.getInstance().sendUsernameData(this.player, Integer.parseInt(time), user, username);
                        }
                     } else if (this.actions.contains(10)) {
                        for (String[] data : lookupList) {
                           String time = data[0];
                           String dplayer = data[1];
                           int wid = Integer.parseInt(data[2]);
                           int dataX = Integer.parseInt(data[3]);
                           int dataY = Integer.parseInt(data[4]);
                           int dataZ = Integer.parseInt(data[5]);
                           String message = data[6];
                           String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
                           int timeLength = 50 + ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, false).replaceAll("[^0-9]", "").length() * 6;
                           String leftPadding = "§l" + Strings.padStart("", 10, ' ');
                           if (timeLength % 4 == 0) {
                              leftPadding = Strings.padStart("", timeLength / 4, ' ');
                           } else {
                              leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
                           }

                           Chat.sendComponent(this.player, timeago + " " + Color.WHITE + "- " + Color.DARK_AQUA + dplayer + ": " + Color.WHITE, message);
                           Chat.sendComponent(
                              this.player,
                              Color.WHITE
                                 + leftPadding
                                 + Color.GREY
                                 + "^ "
                                 + ChatUtils.getCoordinates(this.command.getName(), wid, dataX, dataY, dataZ, true, true)
                           );
                           PluginChannelListener.getInstance()
                              .sendMessageData(this.player, Integer.parseInt(time), dplayer, message, true, dataX, dataY, dataZ, wid);
                        }
                     } else if (this.actions.contains(4) && this.actions.contains(11)) {
                        for (String[] data : lookupList) {
                           String time = data[0];
                           String dplayer = data[1];
                           int dtype = Integer.parseInt(data[5]);
                           int ddata = Integer.parseInt(data[6]);
                           int daction = Integer.parseInt(data[7]);
                           int amount = Integer.parseInt(data[10]);
                           int wid = Integer.parseInt(data[9]);
                           int dataX = Integer.parseInt(data[2]);
                           int dataY = Integer.parseInt(data[3]);
                           int dataZ = Integer.parseInt(data[4]);
                           String rbd = Integer.parseInt(data[8]) != 2 && Integer.parseInt(data[8]) != 3 ? "" : "§m";
                           String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
                           Material blockType = ItemUtils.itemFilter(MaterialUtils.getType(dtype), Integer.parseInt(data[13]) == 0);
                           String dname = StringUtils.nameFilter(blockType.name().toLowerCase(Locale.ROOT), ddata);
                           byte[] metadata = data[11] == null ? null : data[11].getBytes(StandardCharsets.ISO_8859_1);
                           String tooltip = ItemUtils.getEnchantments(metadata, dtype, amount);
                           String selector = "{1}";
                           String tag = Color.WHITE + "-";
                           if (daction == 2 || daction == 3) {
                              selector = daction != 2 ? "{1}" : "{2}";
                              tag = daction != 2 ? Color.GREEN + "+" : Color.RED + "-";
                           } else if (daction == 4 || daction == 5) {
                              selector = daction == 4 ? "{1}" : "{2}";
                              tag = daction == 4 ? Color.GREEN + "+" : Color.RED + "-";
                           } else if (daction == 6 || daction == 7) {
                              selector = "{2}";
                              tag = Color.RED + "-";
                           } else if (daction == 8 || daction == 9 || daction == 10) {
                              selector = daction == 10 ? "{1}" : "{2}";
                              tag = daction == 10 ? Color.GREEN + "+" : Color.RED + "-";
                           } else if (daction != 11 && daction != 12) {
                              selector = daction == 0 ? "{1}" : "{2}";
                              tag = daction == 0 ? Color.GREEN + "+" : Color.RED + "-";
                           } else {
                              selector = daction == 12 ? "{1}" : "{2}";
                              tag = daction == 12 ? Color.GREEN + "+" : Color.RED + "-";
                           }

                           Chat.sendComponent(
                              this.player,
                              timeago
                                 + " "
                                 + tag
                                 + " "
                                 + Phrase.build(
                                    Phrase.LOOKUP_CONTAINER,
                                    Color.DARK_AQUA + rbd + dplayer + Color.WHITE + rbd,
                                    "x" + amount,
                                    ChatUtils.createTooltip(Color.DARK_AQUA + rbd + dname, tooltip) + Color.WHITE,
                                    selector
                                 )
                           );
                           PluginChannelListener.getInstance()
                              .sendData(
                                 this.player,
                                 Integer.parseInt(time),
                                 Phrase.LOOKUP_CONTAINER,
                                 selector,
                                 dplayer,
                                 dname,
                                 amount,
                                 dataX,
                                 dataY,
                                 dataZ,
                                 wid,
                                 rbd,
                                 true,
                                 tag.contains("+")
                              );
                        }
                     } else {
                        for (String[] data : lookupList) {
                           int drb = Integer.parseInt(data[8]);
                           String rbd = "";
                           if (drb == 1 || drb == 3) {
                              rbd = "§m";
                           }

                           String time = data[0];
                           String dplayer = data[1];
                           int dataX = Integer.parseInt(data[2]);
                           int dataY = Integer.parseInt(data[3]);
                           int dataZ = Integer.parseInt(data[4]);
                           int dtype = Integer.parseInt(data[5]);
                           int ddata = Integer.parseInt(data[6]);
                           int daction = Integer.parseInt(data[7]);
                           int wid = Integer.parseInt(data[9]);
                           int amount = Integer.parseInt(data[10]);
                           String tag = Color.WHITE + "-";
                           String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
                           int timeLength = 50 + ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, false).replaceAll("[^0-9]", "").length() * 6;
                           String leftPadding = "§l" + Strings.padStart("", 10, ' ');
                           if (timeLength % 4 == 0) {
                              leftPadding = Strings.padStart("", timeLength / 4, ' ');
                           } else {
                              leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
                           }

                           String dname = "";
                           boolean isPlayer = false;
                           if (daction != 3 || this.actions.contains(11) || amount != -1) {
                              dname = MaterialUtils.getType(dtype).name().toLowerCase(Locale.ROOT);
                              dname = StringUtils.nameFilter(dname, ddata);
                           } else if (dtype == 0) {
                              if (ConfigHandler.playerIdCacheReversed.get(ddata) == null) {
                                 UserStatement.loadName(connection, ddata);
                              }

                              dname = ConfigHandler.playerIdCacheReversed.get(ddata);
                              isPlayer = true;
                           } else {
                              dname = EntityUtils.getEntityType(dtype).name();
                           }

                           if (dname.length() > 0 && !isPlayer) {
                              dname = "minecraft:" + dname.toLowerCase(Locale.ROOT);
                           }

                           if (dname.contains("minecraft:")) {
                              String[] blockNameSplit = dname.split(":");
                              dname = blockNameSplit[1];
                           }

                           Phrase phrase = Phrase.LOOKUP_BLOCK;
                           String selector = "{1}";
                           String action = "a:block";
                           if (!this.actions.contains(4) && !this.actions.contains(5) && !this.actions.contains(11) && amount <= -1) {
                              if (daction != 2 && daction != 3) {
                                 phrase = Phrase.LOOKUP_BLOCK;
                                 selector = daction != 0 ? "{1}" : "{2}";
                                 tag = daction != 0 ? Color.GREEN + "+" : Color.RED + "-";
                              } else {
                                 phrase = Phrase.LOOKUP_INTERACTION;
                                 selector = daction != 3 ? "{1}" : "{2}";
                                 tag = daction != 3 ? Color.WHITE + "-" : Color.RED + "-";
                                 action = daction == 2 ? "a:click" : "a:kill";
                              }

                              Chat.sendComponent(
                                 this.player,
                                 timeago
                                    + " "
                                    + tag
                                    + " "
                                    + Phrase.build(
                                       phrase, Color.DARK_AQUA + rbd + dplayer + Color.WHITE + rbd, Color.DARK_AQUA + rbd + dname + Color.WHITE, selector
                                    )
                              );
                              PluginChannelListener.getInstance()
                                 .sendData(
                                    this.player,
                                    Integer.parseInt(time),
                                    phrase,
                                    selector,
                                    dplayer,
                                    dname,
                                    tag.contains("+") ? 1 : -1,
                                    dataX,
                                    dataY,
                                    dataZ,
                                    wid,
                                    rbd,
                                    false,
                                    tag.contains("+")
                                 );
                           } else {
                              byte[] metadata = data[11] == null ? null : data[11].getBytes(StandardCharsets.ISO_8859_1);
                              String tooltip = ItemUtils.getEnchantments(metadata, dtype, amount);
                              if (daction == 2 || daction == 3) {
                                 phrase = Phrase.LOOKUP_ITEM;
                                 selector = daction != 2 ? "{1}" : "{2}";
                                 tag = daction != 2 ? Color.GREEN + "+" : Color.RED + "-";
                                 action = "a:item";
                              } else if (daction == 4 || daction == 5) {
                                 phrase = Phrase.LOOKUP_STORAGE;
                                 selector = daction != 4 ? "{1}" : "{2}";
                                 tag = daction != 4 ? Color.RED + "-" : Color.GREEN + "+";
                                 action = "a:item";
                              } else if (daction != 6 && daction != 7) {
                                 phrase = Phrase.LOOKUP_CONTAINER;
                                 selector = daction != 0 ? "{1}" : "{2}";
                                 tag = daction != 0 ? Color.GREEN + "+" : Color.RED + "-";
                                 action = "a:container";
                              } else {
                                 phrase = Phrase.LOOKUP_PROJECTILE;
                                 selector = daction != 7 ? "{1}" : "{2}";
                                 tag = Color.RED + "-";
                                 action = "a:item";
                              }

                              Chat.sendComponent(
                                 this.player,
                                 timeago
                                    + " "
                                    + tag
                                    + " "
                                    + Phrase.build(
                                       phrase,
                                       Color.DARK_AQUA + rbd + dplayer + Color.WHITE + rbd,
                                       "x" + amount,
                                       ChatUtils.createTooltip(Color.DARK_AQUA + rbd + dname, tooltip) + Color.WHITE,
                                       selector
                                    )
                              );
                              PluginChannelListener.getInstance()
                                 .sendData(
                                    this.player,
                                    Integer.parseInt(time),
                                    phrase,
                                    selector,
                                    dplayer,
                                    dname,
                                    tag.contains("+") ? 1 : -1,
                                    dataX,
                                    dataY,
                                    dataZ,
                                    wid,
                                    rbd,
                                    action.contains("container"),
                                    tag.contains("+")
                                 );
                           }

                           action = this.actions.size() == 0 ? " (" + action + ")" : "";
                           Chat.sendComponent(
                              this.player,
                              Color.WHITE
                                 + leftPadding
                                 + Color.GREY
                                 + "^ "
                                 + ChatUtils.getCoordinates(this.command.getName(), wid, dataX, dataY, dataZ, true, true)
                                 + Color.GREY
                                 + "§o"
                                 + action
                           );
                        }
                     }
                  } else {
                     for (String[] data : lookupList) {
                        String timex = data[0];
                        String dplayerx = data[1];
                        String message = data[2];
                        String timeagox = ChatUtils.getTimeSince(Integer.parseInt(timex), unixtimestamp, true);
                        Chat.sendComponent(this.player, timeagox + " " + Color.WHITE + "- " + Color.DARK_AQUA + dplayerx + ": " + Color.WHITE, message);
                        if (PluginChannelHandshakeListener.getInstance().isPluginChannelPlayer(this.player)) {
                           int widx = Integer.parseInt(data[3]);
                           int dataXx = Integer.parseInt(data[4]);
                           int dataYx = Integer.parseInt(data[5]);
                           int dataZx = Integer.parseInt(data[6]);
                           PluginChannelListener.getInstance()
                              .sendMessageData(this.player, Integer.parseInt(timex), dplayerx, message, false, dataXx, dataYx, dataZx, widx);
                        }
                     }
                  }

                  if (rows > this.displayResults) {
                     int total_pages = (int)Math.ceil(rows / (this.displayResults + 0.0));
                     if (this.actions.contains(6)
                        || this.actions.contains(7)
                        || this.actions.contains(9)
                        || this.actions.contains(4) && this.actions.contains(11)) {
                        Chat.sendMessage(this.player, "-----");
                     }

                     Chat.sendComponent(this.player, ChatUtils.getPageNavigation(this.command.getName(), this.page, total_pages));
                  }
               } else if (rows > 0L) {
                  Chat.sendMessage(this.player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_RESULTS_PAGE, "{1}"));
               } else {
                  Chat.sendMessage(this.player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_RESULTS));
               }
            } else {
               Chat.sendMessage(this.player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.USER_NOT_FOUND, baduser));
            }

            statement.close();
         }
      } catch (Exception var47) {
         var47.printStackTrace();
      }

      ConfigHandler.lookupThrottle.put(this.player.getName(), new Object[]{false, System.currentTimeMillis()});
   }
}
