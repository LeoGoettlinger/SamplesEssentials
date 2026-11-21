package net.coreprotect.utility;

import java.util.Locale;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class EntityUtils extends Queue {
   private static final String NAMESPACE = "minecraft:";

   private EntityUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static int getEntityId(EntityType type) {
      return type == null ? -1 : getEntityId(type.name(), true);
   }

   public static int getEntityId(String name, boolean internal) {
      int id = -1;
      name = name.toLowerCase(Locale.ROOT).trim();
      if (ConfigHandler.entities.get(name) != null) {
         id = ConfigHandler.entities.get(name);
      } else if (internal) {
         id = ConfigHandler.reloadAndGetId(ConfigHandler.CacheType.ENTITIES, name);
         if (id != -1) {
            return id;
         }

         int entityID = ConfigHandler.entityId + 1;
         ConfigHandler.entities.put(name, entityID);
         ConfigHandler.entitiesReversed.put(entityID, name);
         ConfigHandler.entityId = entityID;
         Queue.queueEntityInsert(entityID, name);
         id = ConfigHandler.entities.get(name);
      }

      return id;
   }

   public static Material getEntityMaterial(EntityType type) {
      String var1 = type.name();
      switch (var1) {
         case "ARMOR_STAND":
            return Material.ARMOR_STAND;
         case "ITEM_FRAME":
            return Material.ITEM_FRAME;
         case "END_CRYSTAL":
         case "ENDER_CRYSTAL":
            return Material.END_CRYSTAL;
         case "ENDER_PEARL":
            return Material.ENDER_PEARL;
         case "POTION":
         case "SPLASH_POTION":
            return Material.SPLASH_POTION;
         case "EXPERIENCE_BOTTLE":
         case "THROWN_EXP_BOTTLE":
            return Material.EXPERIENCE_BOTTLE;
         case "TRIDENT":
            return Material.TRIDENT;
         case "FIREWORK_ROCKET":
         case "FIREWORK":
            return Material.FIREWORK_ROCKET;
         case "EGG":
            return Material.EGG;
         case "SNOWBALL":
            return Material.SNOWBALL;
         case "WIND_CHARGE":
            return Material.valueOf("WIND_CHARGE");
         default:
            return BukkitAdapter.ADAPTER.getFrameType(type);
      }
   }

   public static String getEntityName(int id) {
      String entityName = "";
      if (ConfigHandler.entitiesReversed.get(id) != null) {
         entityName = ConfigHandler.entitiesReversed.get(id);
      }

      return entityName;
   }

   public static EntityType getEntityType(int id) {
      EntityType entitytype = EntityType.UNKNOWN;
      if (ConfigHandler.entitiesReversed.get(id) != null) {
         String name = ConfigHandler.entitiesReversed.get(id);
         if (name.contains("minecraft:")) {
            name = name.split(":")[1];
         }

         entitytype = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
      }

      return entitytype;
   }

   public static EntityType getEntityType(String name) {
      EntityType type = null;
      name = name.toLowerCase(Locale.ROOT).trim();
      if (name.contains("minecraft:")) {
         name = name.split(":")[1];
      }

      if (ConfigHandler.entities.get(name) != null) {
         type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
      }

      return type;
   }

   public static int getSpawnerType(EntityType type) {
      int result = getEntityId(type);
      if (result == -1) {
         result = 0;
      }

      return result;
   }

   public static EntityType getSpawnerType(int type) {
      EntityType result = getEntityType(type);
      if (result == null) {
         result = EntityType.PIG;
      }

      return result;
   }
}
