package de.mistrx.buildpaste.util;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Variables {
   public static String url = "https://buildpaste.net";
   public static String suggestion = "dontplaceair";
   public static HashMap<String, String> mcnames = new HashMap<>();
   public static HashMap<String, String> buildIDs = new HashMap<>();
   public static HashMap<String, Location> pos1 = new HashMap<>();
   public static HashMap<String, Location> pos2 = new HashMap<>();
   public static HashMap<String, String> uploadedBuildID = new HashMap<>();
   public static HashMap<String, String> buildUploadDirection = new HashMap<>();
   public static HashMap<String, Vector> buildSize = new HashMap<>();
   public static HashMap<String, List<Object>> buildBlockIDs = new HashMap<>();
   public static HashMap<String, List<String>> buildBlockData = new HashMap<>();
   public static HashMap<String, JsonObject> buildNBT = new HashMap<>();
   public static HashMap<String, List<Object>> lastBlockIDs = new HashMap<>();
   public static HashMap<String, List<String>> lastBlockData = new HashMap<>();
   public static HashMap<String, Vector> lastPos = new HashMap<>();
   public static HashMap<String, Vector> lastBlockSize = new HashMap<>();
   public static HashMap<String, String> lastPasteDirection = new HashMap<>();
   public static HashMap<String, List<Location>> observerLocation = new HashMap<>();
   public static HashMap<String, List<BlockFace>> observerDirection = new HashMap<>();
   public static HashMap<String, HashMap<String, Integer>> playerItems = new HashMap<>();
   public static HashMap<String, HashMap<String, Integer>> buildBlocks = new HashMap<>();
   public static ArrayList<Player> permissions_paste = new ArrayList<>();
   public static ArrayList<Player> permissions_upload = new ArrayList<>();
   public static ArrayList<Player> permissions_construct = new ArrayList<>();
   public static int maxAllowedPasteBlocks = 5000000;

   public static void CreatePlayerData(Player player) {
      String mcname = player.getName().toString();
      String uuid = player.getUniqueId().toString();
      SetBuildID(uuid, "");
      SetMcname(uuid, mcname);
      SetPos1(uuid, null);
      SetPos2(uuid, null);
      buildUploadDirection.put(uuid, "");
      buildSize.put(uuid, null);
      buildBlockIDs.put(uuid, new ArrayList<>());
      buildBlockData.put(uuid, new ArrayList<>());
      buildNBT.put(uuid, null);
      lastBlockIDs.put(uuid, new ArrayList<>());
      lastBlockData.put(uuid, new ArrayList<>());
      lastPos.put(uuid, null);
      lastBlockSize.put(uuid, null);
      lastPasteDirection.put(uuid, null);
      uploadedBuildID.put(uuid, "");
      observerLocation.put(uuid, new ArrayList<>());
      observerDirection.put(uuid, new ArrayList<>());
      playerItems.put(uuid, new HashMap<>());
      buildBlocks.put(uuid, new HashMap<>());
   }

   public static void SetMcname(String uuid, String value) {
      mcnames.put(uuid, value);
   }

   public static String GetMcname(String uuid) {
      return mcnames.get(uuid);
   }

   public static void SetBuildID(String uuid, String value) {
      buildIDs.put(uuid, value);
   }

   public static String GetBuildID(String uuid) {
      return buildIDs.get(uuid);
   }

   public static void SetPos1(String uuid, Location pos) {
      pos1.put(uuid, pos);
   }

   public static Location GetPos1(String uuid) {
      return pos1.get(uuid);
   }

   public static void SetPos2(String uuid, Location pos) {
      pos2.put(uuid, pos);
   }

   public static Location GetPos2(String uuid) {
      return pos2.get(uuid);
   }

   public static void SetUploadedBuildID(String uuid, String id) {
      uploadedBuildID.put(uuid, id);
   }

   public static String GetUploadedBuildID(String uuid) {
      return uploadedBuildID.get(uuid);
   }

   public static void SetBuildDirection(String uuid, String direction) {
      buildUploadDirection.put(uuid, direction);
   }

   public static String GetBuildDirection(String uuid) {
      return buildUploadDirection.get(uuid);
   }

   public static void SetBuildSize(String uuid, Vector size) {
      buildSize.put(uuid, size);
   }

   public static Vector GetBuildSize(String uuid) {
      return buildSize.get(uuid);
   }

   public static void SetBuildIDs(String uuid, List<Object> blockIDs) {
      buildBlockIDs.put(uuid, blockIDs);
   }

   public static List<Object> GetBuildIDs(String uuid) {
      return buildBlockIDs.get(uuid);
   }

   public static void SetBuildData(String uuid, List<String> blockData) {
      buildBlockData.put(uuid, blockData);
   }

   public static List<String> GetBuildData(String uuid) {
      return buildBlockData.get(uuid);
   }

   public static void SetBuildNBT(String uuid, JsonObject nbt) {
      buildNBT.put(uuid, nbt);
   }

   public static JsonObject GetBuildNBT(String uuid) {
      return buildNBT.get(uuid);
   }

   public static void AddLastBlockIDs(String uuid, Object item) {
      lastBlockIDs.get(uuid).add(item);
   }

   public static List<Object> GetLastBlockIDs(String uuid) {
      return lastBlockIDs.get(uuid);
   }

   public static void AddLastBlockData(String uuid, String item) {
      lastBlockData.get(uuid).add(item);
   }

   public static List<String> GetLastBlockData(String uuid) {
      return lastBlockData.get(uuid);
   }

   public static void SetLastPos(String uuid, Vector pos) {
      lastPos.put(uuid, pos);
   }

   public static Vector GetLastPos(String uuid) {
      return lastPos.get(uuid);
   }

   public static void SetLastBlockSize(String uuid, Vector pos) {
      lastBlockSize.put(uuid, pos);
   }

   public static Vector GetLastBlockSize(String uuid) {
      return lastBlockSize.get(uuid);
   }

   public static void SetLastPasteDirection(String uuid, String direction) {
      lastPasteDirection.put(uuid, direction);
   }

   public static String GetLastPasteDirection(String uuid) {
      return lastPasteDirection.get(uuid);
   }

   public static void AddObserverLocation(String uuid, Location location) {
      observerLocation.get(uuid).add(location);
   }

   public static List<Location> GetObserverLocation(String uuid) {
      return observerLocation.get(uuid);
   }

   public static void AddObserverDirection(String uuid, BlockFace direction) {
      observerDirection.get(uuid).add(direction);
   }

   public static List<BlockFace> GetObserverDirection(String uuid) {
      return observerDirection.get(uuid);
   }

   public static void SetPlayerItems(String uuid, HashMap<String, Integer> map) {
      playerItems.put(uuid, map);
   }

   public static void RemoveFromPlayerItems(String uuid, String item) {
      playerItems.get(uuid).remove(item);
   }

   public static void MergePlayerItem(String uuid, String item, Integer amount) {
      playerItems.get(uuid).merge(item, amount, Integer::sum);
   }

   public static HashMap<String, Integer> GetPlayerItems(String uuid) {
      return playerItems.get(uuid);
   }

   public static void SetBuildBlocks(String uuid, HashMap<String, Integer> map) {
      buildBlocks.put(uuid, map);
   }

   public static void RemoveFromBuildBlocks(String uuid, String item) {
      buildBlocks.get(uuid).remove(item);
   }

   public static void MergeBuildBlocks(String uuid, String block) {
      buildBlocks.get(uuid).merge(block, 1, Integer::sum);
   }

   public static HashMap<String, Integer> GetBuildBlocks(String uuid) {
      return buildBlocks.get(uuid);
   }
}
