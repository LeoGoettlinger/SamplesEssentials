package com.magmaguy.elitemobs.tagger;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PersistentTagger {
   private static final String ELITE_ENTITY = "EliteEntity";
   private static final String NPC_ENTITY = "NPCEntity";
   private static final String VISUAL_EFFECT = "VisualEffect";
   private static final String ELITE_PROJECTILE = "EliteProjectile";

   private PersistentTagger() {
   }

   public static void tag(@NotNull Entity entity, String key, String value) {
      if (entity != null) {
         entity.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING, value);
      }
   }

   public static void tagElite(Entity entity, @NotNull UUID eliteUUID) {
      tag(entity, "EliteEntity", eliteUUID.toString());
   }

   public static boolean isEliteEntity(Entity entity) {
      return entity == null
         ? false
         : entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "EliteEntity"), PersistentDataType.STRING);
   }

   public static EliteEntity getEliteEntity(Entity entity) {
      UUID uuid = getUUID(entity, "EliteEntity");
      return uuid == null ? null : EntityTracker.getEliteMobEntities().get(uuid);
   }

   public static void tagNPC(Entity entity, @NotNull UUID npcUUID) {
      tag(entity, "NPCEntity", npcUUID.toString());
   }

   public static boolean isNPC(Entity entity) {
      return entity == null
         ? false
         : entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "NPCEntity"), PersistentDataType.STRING);
   }

   public static NPCEntity getNPC(Entity entity) {
      UUID uuid = getUUID(entity, "NPCEntity");
      return uuid == null ? null : EntityTracker.getNpcEntities().get(uuid);
   }

   public static void tagVisualEffect(Entity entity) {
      tag(entity, "VisualEffect", entity.getType().toString());
   }

   public static boolean isVisualEffect(Entity entity) {
      return entity == null
         ? false
         : entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "VisualEffect"), PersistentDataType.STRING);
   }

   public static void tagEliteProjectile(Projectile projectile) {
      tag(projectile, "EliteProjectile", "");
   }

   public static boolean isEliteProjectile(Entity entity) {
      return entity == null
         ? false
         : entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "EliteProjectile"), PersistentDataType.STRING);
   }

   @Nullable
   public static UUID getUUID(Entity entity, String key) {
      if (entity == null) {
         return null;
      } else {
         String uuidString = (String)entity.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING);
         return uuidString == null ? null : UUID.fromString(uuidString);
      }
   }

   @Generated
   public static String getELITE_ENTITY() {
      return "EliteEntity";
   }

   @Generated
   public static String getNPC_ENTITY() {
      return "NPCEntity";
   }

   @Generated
   public static String getVISUAL_EFFECT() {
      return "VisualEffect";
   }

   @Generated
   public static String getELITE_PROJECTILE() {
      return "EliteProjectile";
   }
}
