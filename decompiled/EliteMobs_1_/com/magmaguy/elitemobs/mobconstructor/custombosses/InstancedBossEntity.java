package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class InstancedBossEntity extends RegionalBossEntity implements PersistentObject, PersistentMovingEntity {
   private static final ArrayListMultimap<String, InstancedBossEntity.InstancedBossContainer> instancedBossEntities = ArrayListMultimap.create();
   private DungeonInstance dungeonInstance = null;
   private String externalDifficultyID = "0";
   private DifficultyResolver externalDifficultyResolver;
   private Set<Player> lockoutPlayers = new HashSet<>();

   public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, DungeonInstance dungeonInstance) {
      super(customBossesConfigFields, location, false, true);
      this.dungeonInstance = dungeonInstance;
      super.setElitePowers(ElitePowerParser.parsePowers(customBossesConfigFields, this));
      if (this.level == -1) {
         if (dungeonInstance instanceof DynamicDungeonInstance dynamicDungeonInstance) {
            this.level = dynamicDungeonInstance.getSelectedLevel();
         } else if (dungeonInstance.getPlayers().isEmpty()) {
            Logger.warn("Failed to get players for new instance when assigning dynamic level! The bosses will default to level 1.");
         } else {
            this.level = ElitePlayerInventory.getPlayer(dungeonInstance.getPlayers().stream().findFirst().get()).getNaturalMobSpawnLevel(true);
         }
      }
   }

   public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, int level) {
      this(customBossesConfigFields, location, level, "0");
   }

   public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, int level, String difficultyID) {
      super(customBossesConfigFields, location, false, true);
      if (!DifficultyResolver.isCanonical(difficultyID)) {
         throw new IllegalArgumentException("External instance difficulty must be 0, 1 or 2");
      } else {
         this.externalDifficultyID = difficultyID;
         this.externalDifficultyResolver = new DifficultyResolver(customBossesConfigFields.getFilename(), List.of());
         super.level = level;
         super.setElitePowers(ElitePowerParser.parsePowers(customBossesConfigFields, this));
      }
   }

   public static CustomBossEntity createInstancedBossEntity(String filename, Location location, int level) {
      return createInstancedBossEntity(filename, location, level, "0");
   }

   public static CustomBossEntity createInstancedBossEntity(String filename, Location location, int level, String difficultyID) {
      CustomBossesConfigFields configFields = CustomBossesConfig.getCustomBoss(filename);
      if (configFields == null) {
         Logger.warn("Failed to spawn instanced boss entity " + filename + " via API!");
         return null;
      } else {
         return new InstancedBossEntity(configFields, location, level, difficultyID);
      }
   }

   public String getDifficultyID() {
      return this.dungeonInstance == null ? this.externalDifficultyID : this.dungeonInstance.getDifficultyID();
   }

   public String getResolvedDifficultyID() {
      return this.dungeonInstance == null ? this.externalDifficultyID : this.dungeonInstance.getResolvedDifficultyID();
   }

   public boolean matchesDifficulty(List<String> filter, String source) {
      return this.dungeonInstance != null
         ? this.dungeonInstance.matchesDifficulty(filter, source)
         : this.externalDifficultyResolver == null || this.externalDifficultyResolver.matches(filter, this.externalDifficultyID, source);
   }

   public static void shutdown() {
      instancedBossEntities.clear();
   }

   public static void add(String stringLocation, CustomBossesConfigFields customBossesConfigFields) {
      String blueprintWorldName = stringLocation.split(",")[0];
      if (blueprintWorldName != null && !blueprintWorldName.isEmpty()) {
         instancedBossEntities.put(
            blueprintWorldName, new InstancedBossEntity.InstancedBossContainer(ConfigurationLocation.serialize(stringLocation, true), customBossesConfigFields)
         );
      } else {
         Logger.warn("Failed to get blueprint world location for custom boss " + customBossesConfigFields.getFilename() + " !");
      }
   }

   public static List<InstancedBossEntity> initializeInstancedBosses(
      String blueprintWorldName, World newWorld, int playerCount, DungeonInstance dungeonInstance
   ) {
      List<InstancedBossEntity> newDungeonList = new ArrayList<>();

      for (InstancedBossEntity.InstancedBossContainer containers : instancedBossEntities.get(blueprintWorldName)) {
         Location newLocation = containers.getLocation().clone();
         newLocation.setWorld(newWorld);
         InstancedBossEntity newEntity = new InstancedBossEntity(containers.getCustomBossesConfigFields(), newLocation, dungeonInstance);
         newEntity.spawn(false);
         newDungeonList.add(newEntity);
      }

      return newDungeonList;
   }

   public void setNormalizedMaxHealth(int playerCount) {
      super.setNormalizedMaxHealth();
      if (this.dungeonInstance != null && playerCount >= 2) {
         this.maxHealth = Math.min(this.maxHealth * 0.75 * playerCount, LevelScaling.getMinecraftMaxHealth());
      }

      if (this.livingEntity != null) {
         AttributeManager.setAttribute(this.livingEntity, "generic_max_health", this.maxHealth);
         this.livingEntity.setHealth(this.maxHealth);
      }

      this.health = this.maxHealth;
   }

   @Override
   public void setNormalizedMaxHealth() {
      this.setNormalizedMaxHealth(this.dungeonInstance == null ? 1 : this.dungeonInstance.getPlayers().size());
   }

   @Override
   public void setMaxHealth() {
      Double previousHealth = this.health;
      this.setNormalizedMaxHealth();
      if (previousHealth != null) {
         this.health = Math.min(previousHealth, this.maxHealth);
         if (this.livingEntity != null) {
            this.livingEntity.setHealth(this.health);
         }
      }
   }

   public void setEntityLevel(int level) {
      this.level = level;
      this.setMaxHealth();
   }

   @Override
   public void remove(RemovalReason removalReason) {
      this.beginRemovalCall();

      try {
         super.remove(removalReason);
         if (removalReason.equals(RemovalReason.WORLD_UNLOAD) && this.persistentObjectHandler != null) {
            this.persistentObjectHandler.remove();
            this.persistentObjectHandler = null;
         }

         if (removalReason.equals(RemovalReason.WORLD_UNLOAD)
            || removalReason.equals(RemovalReason.SHUTDOWN)
            || removalReason.equals(RemovalReason.ARENA_RESET)) {
            this.dungeonInstance = null;
            this.lockoutPlayers.clear();
         }
      } finally {
         this.finishRemovalCall();
      }
   }

   @Generated
   public DungeonInstance getDungeonInstance() {
      return this.dungeonInstance;
   }

   @Generated
   public Set<Player> getLockoutPlayers() {
      return this.lockoutPlayers;
   }

   @Generated
   public void setLockoutPlayers(Set<Player> lockoutPlayers) {
      this.lockoutPlayers = lockoutPlayers;
   }

   private static class InstancedBossContainer {
      private final Location location;
      private final CustomBossesConfigFields customBossesConfigFields;

      public InstancedBossContainer(Location location, CustomBossesConfigFields customBossesConfigFields) {
         this.location = location;
         this.customBossesConfigFields = customBossesConfigFields;
      }

      @Generated
      public Location getLocation() {
         return this.location;
      }

      @Generated
      public CustomBossesConfigFields getCustomBossesConfigFields() {
         return this.customBossesConfigFields;
      }
   }
}
