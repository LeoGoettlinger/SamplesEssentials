package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.Nullable;

public class EntityUtils {
   private static final boolean HAS_PIGLINS = Skript.classExists("org.bukkit.entity.Piglin");
   private static final BiMap<EntityData<?>, EntityType> SPAWNER_TYPES = HashBiMap.create();
   private static final Map<Class<? extends Entity>, EntityType> CLASS_ENTITY_TYPE_MAP = new HashMap<>();

   public static boolean isAgeable(Entity entity) {
      return !(entity instanceof Ageable) && !(entity instanceof Zombie) ? HAS_PIGLINS && (entity instanceof Piglin || entity instanceof Zoglin) : true;
   }

   public static int getAge(Entity entity) {
      if (entity instanceof Ageable) {
         return ((Ageable)entity).getAge();
      } else if (entity instanceof Zombie) {
         return ((Zombie)entity).isBaby() ? -1 : 0;
      } else {
         if (HAS_PIGLINS) {
            if (entity instanceof Piglin) {
               return ((Piglin)entity).isBaby() ? -1 : 0;
            }

            if (entity instanceof Zoglin) {
               return ((Zoglin)entity).isBaby() ? -1 : 0;
            }
         }

         return 0;
      }
   }

   public static void setAge(Entity entity, int age) {
      if (entity instanceof Ageable) {
         ((Ageable)entity).setAge(age);
      } else if (entity instanceof Zombie) {
         ((Zombie)entity).setBaby(age < 0);
      } else if (HAS_PIGLINS) {
         if (entity instanceof Piglin) {
            ((Piglin)entity).setBaby(age < 0);
         } else if (entity instanceof Zoglin) {
            ((Zoglin)entity).setBaby(age < 0);
         }
      }
   }

   public static void setBaby(Entity entity) {
      setAge(entity, -24000);
   }

   public static void setAdult(Entity entity) {
      setAge(entity, 0);
   }

   public static boolean isAdult(Entity entity) {
      return getAge(entity) >= 0;
   }

   private static void loadSpawnerTypes() {
      for (EntityType e : EntityType.values()) {
         Class<? extends Entity> c = e.getEntityClass();
         if (c != null) {
            SPAWNER_TYPES.put(EntityData.fromClass(c), e);
         }
      }
   }

   public static EntityType toBukkitEntityType(EntityData<?> e) {
      if (SPAWNER_TYPES.isEmpty()) {
         loadSpawnerTypes();
      }

      EntityData<?> entityData = EntityData.fromClass(e.getType());
      return SPAWNER_TYPES.containsKey(entityData) ? (EntityType)SPAWNER_TYPES.get(entityData) : toBukkitEntityType((Class<? extends Entity>)e.getType());
   }

   @Nullable
   public static EntityType toBukkitEntityType(Class<? extends Entity> entityClass) {
      if (CLASS_ENTITY_TYPE_MAP.containsKey(entityClass)) {
         return CLASS_ENTITY_TYPE_MAP.get(entityClass);
      } else {
         EntityType closestEntityType = null;
         Class<? extends Entity> closestClass = null;

         for (EntityType entityType : EntityType.values()) {
            Class<? extends Entity> typeClass = entityType.getEntityClass();
            if (typeClass != null && typeClass.isAssignableFrom(entityClass) && (closestEntityType == null || closestClass.isAssignableFrom(typeClass))) {
               closestEntityType = entityType;
               closestClass = typeClass;
               if (typeClass.equals(entityClass)) {
                  break;
               }
            }
         }

         CLASS_ENTITY_TYPE_MAP.put(entityClass, closestEntityType);
         return closestEntityType;
      }
   }

   public static EntityData<?> toSkriptEntityData(EntityType e) {
      if (SPAWNER_TYPES.isEmpty()) {
         loadSpawnerTypes();
      }

      return (EntityData<?>)SPAWNER_TYPES.inverse().get(e);
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public static void teleport(Entity entity, Location location) {
      if (location.getWorld() == null) {
         location = location.clone();
         location.setWorld(entity.getWorld());
      }

      entity.teleport(location);
   }

   static {
      for (EntityType entityType : EntityType.values()) {
         Class<? extends Entity> entityClass = entityType.getEntityClass();
         if (entityClass != null) {
            CLASS_ENTITY_TYPE_MAP.put(entityClass, entityType);
         }
      }
   }
}
