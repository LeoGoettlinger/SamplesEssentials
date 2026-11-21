package net.coreprotect.utility;

import java.util.Locale;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldUtils extends Queue {
   private WorldUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static int getWorldId(String name) {
      int id = -1;

      try {
         if (ConfigHandler.worlds.get(name) == null) {
            id = ConfigHandler.reloadAndGetId(ConfigHandler.CacheType.WORLDS, name);
            if (id != -1) {
               return id;
            }

            int wid = ConfigHandler.worldId + 1;
            ConfigHandler.worlds.put(name, wid);
            ConfigHandler.worldsReversed.put(wid, name);
            ConfigHandler.worldId = wid;
            Queue.queueWorldInsert(wid, name);
         }

         id = ConfigHandler.worlds.get(name);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return id;
   }

   public static String getWorldName(int id) {
      String name = "";

      try {
         if (ConfigHandler.worldsReversed.get(id) != null) {
            name = ConfigHandler.worldsReversed.get(id);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return name;
   }

   public static int matchWorld(String name) {
      int id = -1;

      try {
         if (name.startsWith("wid:")) {
            String nameWid = name.replaceFirst("wid:", "");
            if (nameWid.length() > 0 && nameWid.equals(nameWid.replaceAll("[^0-9]", ""))) {
               nameWid = getWorldName(Integer.parseInt(nameWid));
               if (nameWid.length() > 0) {
                  name = nameWid;
               }
            }
         }

         String result = "";
         name = name.replaceFirst("#", "").toLowerCase(Locale.ROOT).trim();

         for (World world : Bukkit.getServer().getWorlds()) {
            String worldName = world.getName();
            if (worldName.toLowerCase(Locale.ROOT).equals(name)) {
               result = world.getName();
               break;
            }

            if (worldName.toLowerCase(Locale.ROOT).endsWith(name)) {
               result = world.getName();
            } else if (worldName.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9]", "").endsWith(name)) {
               result = world.getName();
            }
         }

         if (result.length() > 0) {
            id = getWorldId(result);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return id;
   }

   public static String getWidIndex(String queryTable) {
      String index = "";
      boolean isMySQL = Config.getGlobal().MYSQL;
      if (isMySQL) {
         index = "USE INDEX(wid) ";
      } else {
         switch (queryTable) {
            case "block":
               index = "INDEXED BY block_index ";
               break;
            case "container":
               index = "INDEXED BY container_index ";
               break;
            case "item":
               index = "INDEXED BY item_index ";
               break;
            case "sign":
               index = "INDEXED BY sign_index ";
               break;
            case "chat":
               index = "INDEXED BY chat_wid_index ";
               break;
            case "command":
               index = "INDEXED BY command_wid_index ";
               break;
            case "session":
               index = "INDEXED BY session_index ";
         }
      }

      return index;
   }
}
