package de.mistrx.buildpaste.util;

import com.google.gson.JsonObject;
import de.mistrx.buildpaste.firebase.Firebase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Functions {
   private static JavaPlugin plugin;
   private static final int BLOCKS_PER_TICK = 20000;
   public static Integer[] pasteBlockBlackListArray = new Integer[0];
   public static ArrayList<Integer> pasteBlockBlackList = new ArrayList<>(Arrays.asList(pasteBlockBlackListArray));
   public static String[] blockDataBlacklist = new String[]{"level=0", "snowy=false", "waterlogged=true"};
   public static String[] blocksArray = AllBlocksArray.blocksArray;
   public static boolean everyoneAllowed = false;
   public static HashMap<String, String> blockEqualsItemHashmap = new HashMap<>();

   public static void init(JavaPlugin pl) {
      plugin = pl;
   }

   private static JavaPlugin getPlugin() {
      if (plugin == null) {
         throw new IllegalStateException("Functions.init(plugin) was not called!");
      } else {
         return plugin;
      }
   }

   public static void setItemEqualsBlockHashmap() {
      String toAdd = "";
      toAdd = toAdd + "attached_melon_stem=melon_seeds\n";
      toAdd = toAdd + "attached_pumpkin_stem=pumpkin_seeds\n";
      toAdd = toAdd + "melon_stem=melon_seeds\n";
      toAdd = toAdd + "pumpkin_stem=pumpkin_seeds\n";
      toAdd = toAdd + "bamboo_sapling=bamboo\n";
      toAdd = toAdd + "chorus_plant=chorus_fruit\n";
      toAdd = toAdd + "cocoa=cocoa_beans\n";
      toAdd = toAdd + "farmland=dirt\n";
      toAdd = toAdd + "frosted_ice=ice\n";
      toAdd = toAdd + "grass_path=grass\n";
      toAdd = toAdd + "kelp_plant=kelp\n";
      toAdd = toAdd + "moving_piston=piston\n";
      toAdd = toAdd + "redstone_wire=redstone\n";
      toAdd = toAdd + "snow=snowball\n";
      toAdd = toAdd + "sweet_berry_bush=sweet_berries\n";
      toAdd = toAdd + "beetroots=beetroot\n";
      toAdd = toAdd + "potatoes=potato\n";
      toAdd = toAdd + "carrots=carrot\n";
      toAdd = toAdd + "wheat=wheat_seeds\n";
      String[] toAddArray = toAdd.split("\n");

      for (int i = 0; i < toAddArray.length; i++) {
         String[] keyAndValue = toAddArray[i].split("=");
         blockEqualsItemHashmap.put("minecraft:" + keyAndValue[0], "minecraft:" + keyAndValue[1]);
      }
   }

   public static String getItemFromBlock(String block) {
      if (blockEqualsItemHashmap.containsKey(block)) {
         return blockEqualsItemHashmap.get(block);
      } else if (block.contains("wall_")) {
         return block.replace("wall_", "");
      } else if (block.contains("infested_")) {
         return block.replace("infested_", "");
      } else {
         return block.contains("potted_") ? "flower_pot" : block;
      }
   }

   public static Player getPlayerByUuid(String strUUID) {
      UUID uuid = UUID.fromString(strUUID);

      for (Player p : Bukkit.getServer().getOnlinePlayers()) {
         if (p.getUniqueId().equals(uuid)) {
            return p;
         }
      }

      throw new IllegalArgumentException();
   }

   public static void sendNotConnectedMessage(Player player) {
      TextComponent msg = new TextComponent("Accounts not connected or other issue: If you want to use the /paste account then you need a ");
      TextComponent buildpasteBtn = new TextComponent("[Buildpaste]");
      buildpasteBtn.setColor(ChatColor.GREEN);
      buildpasteBtn.setClickEvent(new ClickEvent(Action.OPEN_URL, "https://buildpaste.net"));
      buildpasteBtn.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Open buildpaste.net")}));
      msg.addExtra(buildpasteBtn);
      msg.addExtra(" account. But you can also paste a command in the chat like this: ");
      TextComponent pasteBtn = new TextComponent("[Paste]");
      pasteBtn.setColor(ChatColor.LIGHT_PURPLE);
      pasteBtn.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/paste p8LQhxa0lO7CxjafflKL"));
      pasteBtn.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Paste a build")}));
      msg.addExtra(pasteBtn);
      player.spigot().sendMessage(msg);
   }

   public static boolean checkPermissions(Player player, String permissionType) {
      if (!player.isOp() && !everyoneAllowed) {
         if (!permissionType.equals("paste") || !Variables.permissions_paste.contains(player) && !player.hasPermission("buildpaste.paste")) {
            if (!permissionType.equals("upload") || !Variables.permissions_upload.contains(player) && !player.hasPermission("buildpaste.upload")) {
               if (!permissionType.equals("construct") || !Variables.permissions_construct.contains(player) && !player.hasPermission("buildpaste.construct")) {
                  player.sendMessage(
                     ChatColor.RED
                        + "You are not allowed to use this command. Ask a server operator to OP you, give you the proper permission using '/buildpaste <permission-type> <player> allow' or let them type /buildpaste allowall"
                  );
                  return false;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public static void SetFirstPos(Player player, Location pos_) {
      Location pos = new Location(player.getWorld(), pos_.getBlockX(), pos_.getBlockY(), pos_.getBlockZ());
      String uuid = player.getUniqueId().toString();
      Variables.SetPos1(uuid, pos);
      player.sendMessage("Pos1 has been set to (" + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ() + ")");
      if (Variables.GetPos1(uuid) != null && Variables.GetPos2(uuid) != null) {
         TextComponent message = new TextComponent("Look at the front of your build and type ");
         message.setColor(ChatColor.GREEN);
         TextComponent uploadText = new TextComponent("/upload");
         uploadText.setColor(ChatColor.GREEN);
         uploadText.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/upload"));
         uploadText.setHoverEvent(
            new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("You can specify a name using /upload <name>")})
         );
         message.addExtra(uploadText);
         player.spigot().sendMessage(message);
      }
   }

   public static void SetSecondPos(Player player, Location pos_) {
      Location pos = new Location(player.getWorld(), pos_.getBlockX(), pos_.getBlockY(), pos_.getBlockZ());
      String uuid = player.getUniqueId().toString();
      Variables.SetPos2(uuid, pos);
      player.sendMessage("Pos2 has been set to (" + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ() + ")");
      if (Variables.GetPos1(uuid) != null && Variables.GetPos2(uuid) != null) {
         TextComponent message = new TextComponent("Look at the front of your build and type ");
         message.setColor(ChatColor.GREEN);
         TextComponent uploadText = new TextComponent("/upload");
         uploadText.setColor(ChatColor.GREEN);
         uploadText.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/upload"));
         uploadText.setHoverEvent(
            new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("You can specify a name using /upload <name>")})
         );
         message.addExtra(uploadText);
         player.spigot().sendMessage(message);
      }
   }

   public static void executeCommand(Player player, String command) {
      if (player.getWorld().equals(Bukkit.getServer().getWorlds().get(0))) {
         Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
      } else {
         Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "execute at " + player.getName() + " run " + command);
      }
   }

   public static String getData(World world, Vector pos) {
      Block block = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
      String data = block.getBlockData().getAsString();
      if (!data.contains("[")) {
         return null;
      } else {
         data = data.substring(data.indexOf("["), data.length());
         Debug.Warning("Raw data: " + data);
         data = data.replace("[", "").replace("]", "");
         new ArrayList();
         List dataList;
         if (data.contains(",")) {
            dataList = Arrays.asList(data.split(","));
         } else {
            dataList = Arrays.asList(data);
         }

         for (int i = 0; i < dataList.size(); i++) {
            for (int j = 0; j < blockDataBlacklist.length; j++) {
               if (((String)dataList.get(i)).equalsIgnoreCase(blockDataBlacklist[j])) {
                  dataList.set(i, "");
               }
            }
         }

         data = "[" + String.join(",", dataList) + "]";
         if (data.equals("[]")) {
            data = null;
         }

         return data;
      }
   }

   public static String getSetblockCommand(Vector pos, String blockname, String data, String nbt) {
      Integer x = Math.toIntExact(Math.round(pos.getX()));
      Integer y = Math.toIntExact(Math.round(pos.getY()));
      Integer z = Math.toIntExact(Math.round(pos.getZ()));
      if (data == null || data.equals("null") || !data.startsWith("[")) {
         data = "";
      }

      String command = "";
      return "setblock " + x + " " + y + " " + z + " " + blockname + data + nbt.replaceAll("'", "\"");
   }

   public static String setBuilding(String uuid, Vector pos, String dir, String[] args, Boolean placeBlocksOnFinish) {
      Debug.Log("Args at setBuilding: " + args[0] + ", " + args[1]);
      String buildIDinArgs = null;
      String buildArgs = "nopastemodifier";

      for (int i = 0; i < args.length; i++) {
         if (args[i] != null) {
            if (!args[i].equals(Variables.suggestion)) {
               buildIDinArgs = args[i];
            } else {
               buildArgs = args[i];
            }
         }
      }

      String getPlayerBuildResponse = null;
      if (buildIDinArgs == null) {
         getPlayerBuildResponse = Firebase.getSelectedBuildingID(uuid);
      } else {
         getPlayerBuildResponse = buildIDinArgs;
      }

      Debug.Log(getPlayerBuildResponse + " sent");
      if (getPlayerBuildResponse != "error") {
         Debug.Log("success, Firebase.getBuilding executed");
         String getBuildResponse = Firebase.getBuilding(uuid, getPlayerBuildResponse, placeBlocksOnFinish, pos, dir, buildArgs);
         Debug.Log("getBuildResponse: " + getBuildResponse);
         Debug.Log("After Firebase.getBuilding");
         if (getBuildResponse == "error-404") {
            return "error-404";
         } else if (getPlayerBuildResponse != "error" && getBuildResponse == "success") {
            return "success";
         } else {
            return getPlayerBuildResponse == "error" ? "error" : "random-error";
         }
      } else {
         return getPlayerBuildResponse;
      }
   }

   public static void addToLastBlocks(String uuid, Vector currentPos) {
      Player player = getPlayerByUuid(uuid);
      String blockName = player.getWorld().getBlockAt(currentPos.getBlockX(), currentPos.getBlockY(), currentPos.getBlockZ()).getType().name().toLowerCase();
      if (getBlockIdByName(blockName) == null) {
         Variables.AddLastBlockIDs(uuid, blockName);
      } else {
         Variables.AddLastBlockIDs(uuid, getBlockIdByName(blockName));
      }

      Variables.AddLastBlockData(uuid, getData(player.getWorld(), currentPos));
   }

   public static void setBlock(
      String uuid, Vector currentPos, String id, String data, String nbt, String uploadDirection, String pasteDirection, String pasteModifier
   ) {
      Player player = getPlayerByUuid(uuid);
      String placeBlockName = getBlockByIdOrName(id);
      Block worldBlock = player.getWorld().getBlockAt(currentPos.getBlockX(), currentPos.getBlockY(), currentPos.getBlockZ());
      addToLastBlocks(uuid, currentPos);
      Material currentMat = worldBlock.getType();
      String key = placeBlockName.startsWith("minecraft:") ? placeBlockName.substring("minecraft:".length()) : placeBlockName;
      Material targetMat = Material.matchMaterial(key);
      if (targetMat == null) {
         Debug.Warning("Unknown target material in setBlock(): " + placeBlockName);
      } else {
         boolean sameType = currentMat == targetMat;
         boolean targetIsAir = targetMat == Material.AIR;
         String rotatedData = rotateBlock(data, uploadDirection, pasteDirection);
         if (Variables.suggestion.equals(pasteModifier)) {
            if (!sameType && !targetIsAir) {
               applyBlock(player, currentPos, placeBlockName, rotatedData, nbt);
            }
         } else if ("nopastemodifier".equals(pasteModifier)) {
            if (!sameType) {
               applyBlock(player, currentPos, placeBlockName, rotatedData, nbt);
            }
         } else {
            Debug.Warning("Unknown pasteModifier: " + pasteModifier);
         }
      }
   }

   private static void applyBlock(Player player, Vector pos, String namespacedId, String data, String nbt) {
      World world = player.getWorld();
      int x = pos.getBlockX();
      int y = pos.getBlockY();
      int z = pos.getBlockZ();
      String key = namespacedId;
      if (namespacedId.startsWith("minecraft:")) {
         key = namespacedId.substring("minecraft:".length());
      }

      Material material = Material.matchMaterial(key);
      if (material == null) {
         Debug.Warning("Unknown material: " + namespacedId);
      } else {
         Block block = world.getBlockAt(x, y, z);
         if (material == Material.AIR) {
            block.setType(Material.AIR, false);
         } else {
            try {
               BlockData blockData;
               if (data != null && !data.isEmpty() && !"null".equalsIgnoreCase(data)) {
                  blockData = material.createBlockData(data);
               } else {
                  blockData = material.createBlockData();
               }

               block.setBlockData(blockData, false);
            } catch (IllegalArgumentException var13) {
               Debug.Warning("Invalid block data for " + namespacedId + " " + data + ": " + var13.getMessage());
            }
         }
      }
   }

   private static List<Vector> buildPlacementOrder(Vector pos, Vector size, String uploadDirection, String direction) {
      List<Vector> result = new ArrayList<>();
      int posX = pos.getBlockX();
      int posY = pos.getBlockY();
      int posZ = pos.getBlockZ();
      int sizeX = size.getBlockX();
      int sizeY = size.getBlockY();
      int sizeZ = size.getBlockZ();
      boolean uploadNS = "north".equals(uploadDirection) || "south".equals(uploadDirection);
      if (uploadNS) {
         if ("north".equals(direction)) {
            for (int x = sizeX; x > 0; x--) {
               for (int y = 0; y < sizeY; y++) {
                  for (int z = sizeZ; z > 0; z--) {
                     result.add(new Vector(posX + x, posY + y, posZ + z - sizeZ));
                  }
               }
            }
         } else if ("south".equals(direction)) {
            for (int x = 0; x < sizeX; x++) {
               for (int y = 0; y < sizeY; y++) {
                  for (int z = 0; z < sizeZ; z++) {
                     result.add(new Vector(posX + x - sizeX, posY + y, posZ + z));
                  }
               }
            }
         } else if ("east".equals(direction)) {
            for (int x = sizeX; x > 0; x--) {
               for (int y = 0; y < sizeY; y++) {
                  for (int z = 0; z < sizeZ; z++) {
                     result.add(new Vector(posX + z, posY + y, posZ + x));
                  }
               }
            }
         } else if ("west".equals(direction)) {
            for (int x = 0; x < sizeX; x++) {
               for (int y = 0; y < sizeY; y++) {
                  for (int z = sizeZ; z > 0; z--) {
                     result.add(new Vector(posX + z - sizeZ, posY + y, posZ + x - sizeX));
                  }
               }
            }
         }
      } else if ("north".equals(direction)) {
         for (int x = sizeX; x > 0; x--) {
            for (int y = 0; y < sizeY; y++) {
               for (int z = sizeZ; z > 0; z--) {
                  result.add(new Vector(posX + z, posY + y, posZ + x - sizeX));
               }
            }
         }
      } else if ("south".equals(direction)) {
         for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
               for (int z = 0; z < sizeZ; z++) {
                  result.add(new Vector(posX + z - sizeZ, posY + y, posZ + x));
               }
            }
         }
      } else if ("east".equals(direction)) {
         for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
               for (int z = sizeZ; z > 0; z--) {
                  result.add(new Vector(posX + x, posY + y, posZ + z));
               }
            }
         }
      } else if ("west".equals(direction)) {
         for (int x = sizeX; x > 0; x--) {
            for (int y = 0; y < sizeY; y++) {
               for (int z = 0; z < sizeZ; z++) {
                  result.add(new Vector(posX + x - sizeX, posY + y, posZ + z - sizeZ));
               }
            }
         }
      }

      return result;
   }

   public static String pasteCurrentBuilding(
      String uuid,
      Vector pos,
      Vector sizeArg,
      String uploadDirection,
      List<Object> blockIDs,
      List<String> blockData,
      JsonObject blockNBT,
      String direction,
      String pasteModifier,
      boolean undoPaste,
      boolean isConstructing
   ) {
      Debug.Log("Start pasting");
      Vector size;
      List<Object> ids;
      List<String> data;
      JsonObject nbt;
      if (!undoPaste) {
         size = sizeArg;
         ids = blockIDs;
         data = blockData;
         nbt = blockNBT;
      } else {
         size = Variables.GetLastBlockSize(uuid);
         ids = Variables.GetLastBlockIDs(uuid);
         data = Variables.GetLastBlockData(uuid);
         nbt = blockNBT;
      }

      if (size != null && ids != null && data != null) {
         Variables.SetLastPos(uuid, pos);
         Variables.SetLastPasteDirection(uuid, direction);
         Variables.SetLastBlockSize(uuid, sizeArg);
         Variables.lastBlockIDs.put(uuid, new ArrayList<>());
         Variables.lastBlockData.put(uuid, new ArrayList<>());
         Variables.observerDirection.put(uuid, new ArrayList<>());
         Variables.observerLocation.put(uuid, new ArrayList<>());
         List<Vector> positions = buildPlacementOrder(pos, size, uploadDirection, direction);
         new Functions.PasteTask(uuid, uploadDirection, direction, pasteModifier, isConstructing, positions, ids, data, nbt).start();
         return "scheduled";
      } else {
         Debug.Warning("pasteCurrentBuilding: no previous data for undo.");
         return "error-no-data";
      }
   }

   public static String rotateBlock(String thisData, String uploadDirection, String pasteDirection, Boolean returnDirection) {
      if (thisData != null) {
         if (thisData.contains("facing=") && thisData.length() >= 11) {
            String blockDirection = thisData.substring(thisData.indexOf("facing=") + 7);
            if (blockDirection.contains(",")) {
               blockDirection = blockDirection.substring(0, blockDirection.indexOf(","));
            } else {
               blockDirection = blockDirection.substring(0, blockDirection.indexOf("]"));
            }

            return calculateBlockRotation(blockDirection, pasteDirection, uploadDirection);
         } else {
            return thisData;
         }
      } else {
         return "";
      }
   }

   public static String rotateBlock(String thisData, String uploadDirection, String pasteDirection) {
      if (thisData != null && !thisData.isEmpty()) {
         if (thisData.startsWith("[") && thisData.endsWith("]")) {
            String inner = thisData.substring(1, thisData.length() - 1).trim();
            if (inner.isEmpty()) {
               return thisData;
            } else {
               Map<String, String> props = new LinkedHashMap<>();
               String[] parts = inner.split(",");

               for (String part : parts) {
                  part = part.trim();
                  if (!part.isEmpty()) {
                     int eq = part.indexOf(61);
                     if (eq > 0) {
                        String key = part.substring(0, eq);
                        String value = part.substring(eq + 1);
                        props.put(key, value);
                     }
                  }
               }

               int uploadDeg = facingToDegrees(uploadDirection);
               int pasteDeg = facingToDegrees(pasteDirection);
               int diff = (360 + pasteDeg - uploadDeg) % 360;
               if (props.containsKey("facing")) {
                  String facing = props.get("facing");
                  String rotatedFacing = calculateBlockRotation(facing, pasteDirection, uploadDirection);
                  props.put("facing", rotatedFacing);
               }

               String[] dirKeys = new String[]{"north", "east", "south", "west"};
               Map<String, Boolean> originalBoolConnections = new LinkedHashMap<>();
               Map<String, String> originalHeightConnections = new LinkedHashMap<>();

               for (String dir : dirKeys) {
                  String value = props.get(dir);
                  if (value != null) {
                     String lowerVal = value.toLowerCase(Locale.ROOT);
                     if ("true".equals(lowerVal) || "false".equals(lowerVal)) {
                        originalBoolConnections.put(dir, Boolean.parseBoolean(lowerVal));
                        props.remove(dir);
                     } else if ("none".equals(lowerVal) || "low".equals(lowerVal) || "tall".equals(lowerVal)) {
                        originalHeightConnections.put(dir, lowerVal);
                        props.remove(dir);
                     }
                  }
               }

               if (!originalBoolConnections.isEmpty()) {
                  if (diff == 0) {
                     for (Entry<String, Boolean> e : originalBoolConnections.entrySet()) {
                        props.put(e.getKey(), Boolean.toString(e.getValue()));
                     }
                  } else {
                     Map<String, Boolean> rotatedBool = new LinkedHashMap<>();

                     for (String d : dirKeys) {
                        rotatedBool.put(d, Boolean.FALSE);
                     }

                     for (Entry<String, Boolean> e : originalBoolConnections.entrySet()) {
                        if (e.getValue()) {
                           String dirx = e.getKey();
                           int dirDeg = facingToDegrees(dirx);
                           int rotatedDeg = (360 + dirDeg + diff) % 360;
                           String rotatedDir = degreesToFacing(rotatedDeg);
                           if (rotatedBool.containsKey(rotatedDir)) {
                              rotatedBool.put(rotatedDir, Boolean.TRUE);
                           }
                        }
                     }

                     for (Entry<String, Boolean> ex : rotatedBool.entrySet()) {
                        props.put(ex.getKey(), Boolean.toString(ex.getValue()));
                     }
                  }
               }

               if (!originalHeightConnections.isEmpty()) {
                  if (diff == 0) {
                     for (Entry<String, String> ex : originalHeightConnections.entrySet()) {
                        props.put(ex.getKey(), ex.getValue());
                     }
                  } else {
                     Map<String, String> rotatedHeight = new LinkedHashMap<>();

                     for (Entry<String, String> ex : originalHeightConnections.entrySet()) {
                        String dirx = ex.getKey();
                        String height = ex.getValue();
                        int dirDeg = facingToDegrees(dirx);
                        int rotatedDeg = (360 + dirDeg + diff) % 360;
                        String rotatedDir = degreesToFacing(rotatedDeg);
                        rotatedHeight.put(rotatedDir, height);
                     }

                     for (Entry<String, String> ex : rotatedHeight.entrySet()) {
                        props.put(ex.getKey(), ex.getValue());
                     }
                  }
               }

               if (props.containsKey("axis")) {
                  String axis = props.get("axis");
                  if (axis != null) {
                     String axisLower = axis.toLowerCase(Locale.ROOT);
                     if (!"y".equals(axisLower)) {
                        int diffMod180 = diff % 180;
                        if (diffMod180 != 0) {
                           if ("x".equals(axisLower)) {
                              axisLower = "z";
                           } else if ("z".equals(axisLower)) {
                              axisLower = "x";
                           }
                        }
                     }

                     props.put("axis", axisLower);
                  }
               }

               StringBuilder sb = new StringBuilder();
               sb.append("[");
               boolean first = true;

               for (Entry<String, String> ex : props.entrySet()) {
                  if (!first) {
                     sb.append(",");
                  }

                  first = false;
                  sb.append(ex.getKey()).append("=").append(ex.getValue());
               }

               sb.append("]");
               return sb.toString();
            }
         } else {
            return thisData;
         }
      } else {
         return thisData;
      }
   }

   public static String calculateBlockRotation(String blockDir, String pasteDir, String uploadDir) {
      String[] possibleBlockDirectionsArray = new String[]{"north", "south", "east", "west"};
      List<String> possibleBlockDirections = Arrays.asList(possibleBlockDirectionsArray);
      if (possibleBlockDirections.contains(blockDir)) {
         Integer uploadDirectionInDegrees = facingToDegrees(uploadDir);
         Integer pasteDirInt = facingToDegrees(pasteDir);
         Integer differenceUploadPasteDirection = pasteDirInt - uploadDirectionInDegrees;
         Integer result = (360 + facingToDegrees(blockDir) + differenceUploadPasteDirection) % 360;
         return degreesToFacing(result);
      } else {
         return blockDir;
      }
   }

   public static Integer facingToDegrees(String facing) {
      if (facing.equals("north")) {
         return 0;
      } else if (facing.equals("east")) {
         return 90;
      } else if (facing.equals("south")) {
         return 180;
      } else if (facing.equals("west")) {
         return 270;
      } else {
         Debug.Log("No rotation is passend, return 0");
         return 0;
      }
   }

   public static String degreesToFacing(Integer degrees) {
      if (degrees == 0) {
         return "north";
      } else if (degrees == 90) {
         return "east";
      } else if (degrees == 180) {
         return "south";
      } else {
         return degrees == 270 ? "west" : "north";
      }
   }

   public static String getLookDirection(float playerYaw) {
      float yaw = playerYaw;
      if (playerYaw < 0.0F) {
         yaw = playerYaw + 360.0F;
      }

      if (yaw >= 315.0F || yaw < 45.0F) {
         return "south";
      } else if (yaw < 135.0F) {
         return "west";
      } else if (yaw < 225.0F) {
         return "north";
      } else {
         return yaw < 315.0F ? "east" : "north";
      }
   }

   public static String getBlockByID(Integer id) {
      return "minecraft:" + blocksArray[id];
   }

   public static Integer getBlockIdByName(String name) {
      return Arrays.asList(blocksArray).contains(name) ? ArrayUtils.indexOf(blocksArray, name.replace("minecraft:", "")) : null;
   }

   public static String getBlockByIdOrName(Object nameOrId) {
      try {
         return "minecraft:" + blocksArray[Math.round(Float.parseFloat(nameOrId.toString()))];
      } catch (NumberFormatException var2) {
         return nameOrId.toString();
      }
   }

   private static class PasteTask implements Runnable {
      private final String uuid;
      private final String uploadDirection;
      private final String direction;
      private final String pasteModifier;
      private final boolean isConstructing;
      private final List<Vector> positions;
      private final List<Object> ids;
      private final List<String> data;
      private final JsonObject nbt;
      private final HashMap<String, Integer> usedMaterials = new HashMap<>();
      private int index = 0;
      private final Player player;
      private BukkitTask task;

      PasteTask(
         String uuid,
         String uploadDirection,
         String direction,
         String pasteModifier,
         boolean isConstructing,
         List<Vector> positions,
         List<Object> ids,
         List<String> data,
         JsonObject nbt
      ) {
         this.uuid = uuid;
         this.uploadDirection = uploadDirection;
         this.direction = direction;
         this.pasteModifier = pasteModifier;
         this.isConstructing = isConstructing;
         this.positions = positions;
         this.ids = ids;
         this.data = data;
         this.nbt = nbt;
         this.player = Functions.getPlayerByUuid(uuid);
      }

      void start() {
         this.task = Bukkit.getScheduler().runTaskTimer(Functions.getPlugin(), this, 0L, 1L);
      }

      @Override
      public void run() {
         if (this.player != null && this.player.isOnline()) {
            int total = this.positions.size();

            for (int blocksThisTick = 0; this.index < total && blocksThisTick < 20000; blocksThisTick++) {
               int i = this.index++;
               Vector currentPos = this.positions.get(i);
               String blockName = Functions.getBlockByIdOrName(this.ids.get(i).toString());
               String nbtData = "";
               if (this.nbt != null && this.nbt.has(Integer.toString(i))) {
                  nbtData = this.nbt.get(Integer.toString(i)).getAsString();
               }

               if (this.isConstructing) {
                  String itemFromBlock = Functions.getItemFromBlock(blockName);
                  HashMap<String, Integer> playerItems = Variables.GetPlayerItems(this.uuid);
                  Integer amount = playerItems.get(itemFromBlock);
                  if (amount != null && amount > 0) {
                     Functions.setBlock(this.uuid, currentPos, blockName, this.data.get(i), nbtData, this.uploadDirection, this.direction, this.pasteModifier);
                     Variables.MergePlayerItem(this.uuid, itemFromBlock, -1);
                     this.usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                  } else {
                     Functions.addToLastBlocks(this.uuid, currentPos);
                  }
               } else {
                  Functions.setBlock(this.uuid, currentPos, blockName, this.data.get(i), nbtData, this.uploadDirection, this.direction, this.pasteModifier);
               }
            }

            if (this.index >= total) {
               this.finish();
               if (this.task != null) {
                  this.task.cancel();
               }
            }
         } else {
            Debug.Warning("PasteTask canceled: player offline.");
            if (this.task != null) {
               this.task.cancel();
            }
         }
      }

      private void finish() {
         Debug.Log("PasteTask finished, placing observers and clearing items if needed.");
         World world = this.player.getWorld();
         List<Location> observerLocations = Variables.GetObserverLocation(this.uuid);
         List<BlockFace> observerDirections = Variables.GetObserverDirection(this.uuid);

         for (int j = 0; j < observerLocations.size(); j++) {
            Location loc = observerLocations.get(j);
            if (loc != null && loc.getWorld() != null && loc.getWorld().equals(world)) {
               int y = loc.getBlockY();
               if (y >= world.getMinHeight() && y < world.getMaxHeight()) {
                  BlockData bd = Material.OBSERVER.createBlockData();
                  if (bd instanceof Directional) {
                     Directional dir = (Directional)bd;
                     dir.setFacing(observerDirections.get(j));
                     world.getBlockAt(loc).setBlockData(dir, false);
                  } else {
                     world.getBlockAt(loc).setType(Material.OBSERVER, false);
                  }
               }
            }
         }

         if (this.isConstructing) {
            for (String item : this.usedMaterials.keySet()) {
               Integer amount = this.usedMaterials.get(item);
               if (amount != null && amount > 0) {
                  String command = "clear " + this.player.getName() + " " + item + " " + amount;
                  Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
               }
            }
         }
      }
   }
}
