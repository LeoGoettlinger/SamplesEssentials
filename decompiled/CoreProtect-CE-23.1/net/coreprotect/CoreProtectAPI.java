package net.coreprotect;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coreprotect.api.BlockAPI;
import net.coreprotect.api.QueueLookup;
import net.coreprotect.api.SessionLookup;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.database.rollback.Rollback;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.player.InventoryChangeListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.MaterialUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CoreProtectAPI extends Queue {
   private static final int API_VERSION = 11;

   private static Map<Object, Boolean> parseList(List<Object> list) {
      Map<Object, Boolean> result = new HashMap<>();
      if (list != null) {
         for (Object value : list) {
            if (value instanceof Material || value instanceof EntityType) {
               result.put(value, false);
            } else if (value instanceof Integer) {
               Material material = MaterialUtils.getType((Integer)value);
               result.put(material, false);
            }
         }
      }

      return result;
   }

   public int APIVersion() {
      return 11;
   }

   public List<String[]> blockLookup(Block block, int time) {
      return this.isEnabled() ? BlockAPI.performLookup(block, time) : null;
   }

   public List<String[]> queueLookup(Block block) {
      return QueueLookup.performLookup(block);
   }

   public List<String[]> sessionLookup(String user, int time) {
      return SessionLookup.performLookup(user, time);
   }

   public boolean hasPlaced(String user, Block block, int time, int offset) {
      if (!this.isEnabled()) {
         return false;
      } else {
         long timestamp = this.getCurrentTimeMillis();
         long offsetTime = timestamp - offset * 1000L;

         for (String[] value : this.blockLookup(block, time)) {
            CoreProtectAPI.ParseResult result = this.parseResult(value);
            if (user.equalsIgnoreCase(result.getPlayer()) && result.getActionId() == 1 && result.getTimestamp() <= offsetTime) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean hasRemoved(String user, Block block, int time, int offset) {
      if (!this.isEnabled()) {
         return false;
      } else {
         long timestamp = this.getCurrentTimeMillis();
         long offsetTime = timestamp - offset * 1000L;

         for (String[] value : this.blockLookup(block, time)) {
            CoreProtectAPI.ParseResult result = this.parseResult(value);
            if (user.equalsIgnoreCase(result.getPlayer()) && result.getActionId() == 0 && result.getTimestamp() <= offsetTime) {
               return true;
            }
         }

         return false;
      }
   }

   protected long getCurrentTimeMillis() {
      return System.currentTimeMillis();
   }

   public boolean isEnabled() {
      return Config.getGlobal().API_ENABLED;
   }

   public boolean logChat(Player player, String message) {
      if (!this.isEnabledForPlayer(player) || !Config.getConfig(player.getWorld()).PLAYER_MESSAGES) {
         return false;
      } else if (message != null && !message.isEmpty() && !message.startsWith("/")) {
         long timestamp = System.currentTimeMillis() / 1000L;
         Queue.queuePlayerChat(player, message, timestamp);
         return true;
      } else {
         return false;
      }
   }

   public boolean logCommand(Player player, String command) {
      if (!this.isEnabledForPlayer(player) || !Config.getConfig(player.getWorld()).PLAYER_COMMANDS) {
         return false;
      } else if (command != null && !command.isEmpty() && command.startsWith("/")) {
         long timestamp = System.currentTimeMillis() / 1000L;
         Queue.queuePlayerCommand(player, command, timestamp);
         return true;
      } else {
         return false;
      }
   }

   public boolean logInteraction(String user, Location location) {
      if (this.isEnabled() && this.isValidUserAndLocation(user, location)) {
         Queue.queuePlayerInteraction(user, location.getBlock().getState(), location.getBlock().getType());
         return true;
      } else {
         return false;
      }
   }

   public boolean logContainerTransaction(String user, Location location) {
      return !this.isEnabled() ? false : InventoryChangeListener.inventoryTransaction(user, location, null);
   }

   public boolean logPlacement(String user, BlockState blockState) {
      if (this.isEnabled() && blockState != null && user != null && !user.isEmpty()) {
         Queue.queueBlockPlace(user, blockState, blockState.getType(), null, blockState.getType(), -1, 0, blockState.getBlockData().getAsString());
         return true;
      } else {
         return false;
      }
   }

   public boolean logPlacement(String user, Location location, Material type, BlockData blockData) {
      if (this.isEnabled() && this.isValidUserAndLocation(user, location)) {
         Block block = location.getBlock();
         BlockState blockState = block.getState();
         String blockDataString = null;
         if (blockData != null) {
            blockDataString = blockData.getAsString();
         }

         Queue.queueBlockPlace(user, blockState, block.getType(), null, type, -1, 0, blockDataString);
         return true;
      } else {
         return false;
      }
   }

   @Deprecated
   public boolean logPlacement(String user, Location location, Material type, byte data) {
      if (this.isEnabled() && this.isValidUserAndLocation(user, location)) {
         Queue.queueBlockPlace(user, location.getBlock().getState(), location.getBlock().getType(), null, type, data, 1, null);
         return true;
      } else {
         return false;
      }
   }

   public boolean logRemoval(String user, BlockState blockState) {
      if (this.isEnabled() && blockState != null && user != null && !user.isEmpty()) {
         Queue.queueBlockBreak(user, blockState, blockState.getType(), blockState.getBlockData().getAsString(), 0);
         return true;
      } else {
         return false;
      }
   }

   public boolean logRemoval(String user, Location location, Material type, BlockData blockData) {
      if (this.isEnabled() && this.isValidUserAndLocation(user, location)) {
         String blockDataString = null;
         if (blockData != null) {
            blockDataString = blockData.getAsString();
         }

         Block block = location.getBlock();
         Database.containerBreakCheck(user, block.getType(), block, null, location);
         Queue.queueBlockBreak(user, location.getBlock().getState(), type, blockDataString, 0);
         return true;
      } else {
         return false;
      }
   }

   @Deprecated
   public boolean logRemoval(String user, Location location, Material type, byte data) {
      if (this.isEnabled() && this.isValidUserAndLocation(user, location)) {
         Queue.queueBlockBreak(user, location.getBlock().getState(), type, type.createBlockData().getAsString(), data);
         return true;
      } else {
         return false;
      }
   }

   public CoreProtectAPI.ParseResult parseResult(String[] results) {
      return new CoreProtectAPI.ParseResult(results);
   }

   public List<String[]> performLookup(
      int time,
      List<String> restrictUsers,
      List<String> excludeUsers,
      List<Object> restrictBlocks,
      List<Object> excludeBlocks,
      List<Integer> actionList,
      int radius,
      Location radiusLocation
   ) {
      return !this.isEnabled()
         ? null
         : this.processData(
            time, radius, radiusLocation, parseList(restrictBlocks), parseList(excludeBlocks), restrictUsers, excludeUsers, actionList, 0, 1, -1, -1, false
         );
   }

   @Deprecated
   public List<String[]> performLookup(String user, int time, int radius, Location location, List<Object> restrict, List<Object> exclude) {
      return !this.isEnabled() ? null : this.processData(user, time, radius, location, parseList(restrict), parseList(exclude), 0, 1, -1, -1, false);
   }

   public List<String[]> performPartialLookup(
      int time,
      List<String> restrictUsers,
      List<String> excludeUsers,
      List<Object> restrictBlocks,
      List<Object> excludeBlocks,
      List<Integer> actionList,
      int radius,
      Location radiusLocation,
      int limitOffset,
      int limitCount
   ) {
      return !this.isEnabled()
         ? null
         : this.processData(
            time,
            radius,
            radiusLocation,
            parseList(restrictBlocks),
            parseList(excludeBlocks),
            restrictUsers,
            excludeUsers,
            actionList,
            0,
            1,
            limitOffset,
            limitCount,
            true
         );
   }

   @Deprecated
   public List<String[]> performPartialLookup(
      String user, int time, int radius, Location location, List<Object> restrict, List<Object> exclude, int limitOffset, int limitCount
   ) {
      return !this.isEnabled()
         ? null
         : this.processData(user, time, radius, location, parseList(restrict), parseList(exclude), 0, 1, limitOffset, limitCount, true);
   }

   public void performPurge(int time) {
      Server server = Bukkit.getServer();
      server.dispatchCommand(server.getConsoleSender(), "co purge t:" + time + "s");
   }

   public List<String[]> performRestore(
      int time,
      List<String> restrictUsers,
      List<String> excludeUsers,
      List<Object> restrictBlocks,
      List<Object> excludeBlocks,
      List<Integer> actionList,
      int radius,
      Location radiusLocation
   ) {
      return !this.isEnabled()
         ? null
         : this.processData(
            time, radius, radiusLocation, parseList(restrictBlocks), parseList(excludeBlocks), restrictUsers, excludeUsers, actionList, 1, 2, -1, -1, false
         );
   }

   @Deprecated
   public List<String[]> performRestore(String user, int time, int radius, Location location, List<Object> restrict, List<Object> exclude) {
      return !this.isEnabled() ? null : this.processData(user, time, radius, location, parseList(restrict), parseList(exclude), 1, 2, -1, -1, false);
   }

   public List<String[]> performRollback(
      int time,
      List<String> restrictUsers,
      List<String> excludeUsers,
      List<Object> restrictBlocks,
      List<Object> excludeBlocks,
      List<Integer> actionList,
      int radius,
      Location radiusLocation
   ) {
      return !this.isEnabled()
         ? null
         : this.processData(
            time, radius, radiusLocation, parseList(restrictBlocks), parseList(excludeBlocks), restrictUsers, excludeUsers, actionList, 0, 2, -1, -1, false
         );
   }

   @Deprecated
   public List<String[]> performRollback(String user, int time, int radius, Location location, List<Object> restrict, List<Object> exclude) {
      return !this.isEnabled() ? null : this.processData(user, time, radius, location, parseList(restrict), parseList(exclude), 0, 2, -1, -1, false);
   }

   private List<String[]> processData(
      int time,
      int radius,
      Location location,
      Map<Object, Boolean> restrictBlocksMap,
      Map<Object, Boolean> excludeBlocks,
      List<String> restrictUsers,
      List<String> excludeUsers,
      List<Integer> actionList,
      int action,
      int lookup,
      int offset,
      int rowCount,
      boolean useLimit
   ) {
      List<String[]> result = new ArrayList<>();
      List<String> uuids = new ArrayList<>();
      if (restrictUsers == null) {
         restrictUsers = new ArrayList<>();
      }

      if (excludeUsers == null) {
         excludeUsers = new ArrayList<>();
      }

      if (actionList == null) {
         actionList = new ArrayList<>();
      }

      List<Object> restrictBlocks = new ArrayList<>(restrictBlocksMap.keySet());
      if (actionList.isEmpty() && !restrictBlocks.isEmpty()) {
         boolean addedMaterial = false;
         boolean addedEntity = false;

         for (Object argBlock : restrictBlocks) {
            if (argBlock instanceof Material && !addedMaterial) {
               actionList.add(0);
               actionList.add(1);
               addedMaterial = true;
            } else if (argBlock instanceof EntityType && !addedEntity) {
               actionList.add(3);
               addedEntity = true;
            }
         }
      }

      if (actionList.isEmpty()) {
         actionList.add(0);
         actionList.add(1);
      }

      actionList.removeIf(actionListItem -> actionListItem > 3);
      if (restrictUsers.isEmpty()) {
         restrictUsers.add("#global");
      }

      long timestamp = System.currentTimeMillis() / 1000L;
      long startTime = timestamp - time;
      long endTime = 0L;
      if (radius < 1) {
         radius = -1;
      }

      if (restrictUsers.contains("#global") && radius == -1) {
         return null;
      } else if (radius > -1 && location == null) {
         return null;
      } else {
         try (Connection connection = Database.getConnection(false, 1000)) {
            if (connection != null) {
               Statement statement = connection.createStatement();
               boolean restrictWorld = false;
               if (radius > 0) {
                  restrictWorld = true;
               }

               if (location == null) {
                  restrictWorld = false;
               }

               Integer[] argRadius = null;
               if (location != null && radius > 0) {
                  int xMin = location.getBlockX() - radius;
                  int xMax = location.getBlockX() + radius;
                  int zMin = location.getBlockZ() - radius;
                  int zMax = location.getBlockZ() + radius;
                  argRadius = new Integer[]{radius, xMin, xMax, null, null, zMin, zMax, 0};
               }

               if (lookup == 1) {
                  if (location != null) {
                     restrictWorld = true;
                  }

                  if (useLimit) {
                     result = Lookup.performPartialLookup(
                        statement,
                        null,
                        uuids,
                        restrictUsers,
                        restrictBlocks,
                        excludeBlocks,
                        excludeUsers,
                        actionList,
                        location,
                        argRadius,
                        null,
                        startTime,
                        endTime,
                        offset,
                        rowCount,
                        restrictWorld,
                        true
                     );
                  } else {
                     result = Lookup.performLookup(
                        statement,
                        null,
                        uuids,
                        restrictUsers,
                        restrictBlocks,
                        excludeBlocks,
                        excludeUsers,
                        actionList,
                        location,
                        argRadius,
                        startTime,
                        endTime,
                        restrictWorld,
                        true
                     );
                  }
               } else if (!Bukkit.isPrimaryThread()) {
                  boolean verbose = false;
                  result = Rollback.performRollbackRestore(
                     statement,
                     null,
                     uuids,
                     restrictUsers,
                     null,
                     restrictBlocks,
                     excludeBlocks,
                     excludeUsers,
                     actionList,
                     location,
                     argRadius,
                     startTime,
                     endTime,
                     restrictWorld,
                     false,
                     verbose,
                     action,
                     0
                  );
               } else {
                  Chat.console(Phrase.build(Phrase.PRIMARY_THREAD_ERROR));
               }

               statement.close();
            }
         } catch (Exception var33) {
            var33.printStackTrace();
         }

         return result;
      }
   }

   @Deprecated
   private List<String[]> processData(
      String user,
      int time,
      int radius,
      Location location,
      Map<Object, Boolean> restrictBlocks,
      Map<Object, Boolean> excludeBlocks,
      int action,
      int lookup,
      int offset,
      int rowCount,
      boolean useLimit
   ) {
      ArrayList<String> restrictUsers = new ArrayList<>();
      if (user != null) {
         restrictUsers.add(user);
      }

      return this.processData(time, radius, location, restrictBlocks, excludeBlocks, restrictUsers, null, null, action, lookup, offset, rowCount, useLimit);
   }

   public void testAPI() {
      Chat.console(Phrase.build(Phrase.API_TEST));
   }

   private boolean isEnabledForPlayer(Player player) {
      return this.isEnabled() && player != null;
   }

   private boolean isValidUserAndLocation(String user, Location location) {
      return user != null && location != null && !user.isEmpty();
   }

   public static class ParseResult extends net.coreprotect.api.result.ParseResult {
      public ParseResult(String[] data) {
         super(data);
      }
   }
}
