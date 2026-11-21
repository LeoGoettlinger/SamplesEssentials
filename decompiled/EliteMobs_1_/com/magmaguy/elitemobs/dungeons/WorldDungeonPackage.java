package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldDungeonPackage extends WorldPackage implements Dungeon {
   private int lowestLevel;
   private int highestLevel;
   private World wormholeWorld = null;

   public WorldDungeonPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);
   }

   @Override
   public void baseInitialization() {
      super.baseInitialization();
      this.initializeWormholeWorld();
   }

   private void initializeWormholeWorld() {
      if (this.contentPackagesConfigFields.getWormholeWorldName() != null && !this.contentPackagesConfigFields.getWormholeWorldName().isEmpty()) {
         String wormholeWorldName = this.contentPackagesConfigFields.getWormholeWorldName();
         boolean wormholeWorldExists = Bukkit.getWorld(wormholeWorldName) != null || WorldFolderResolver.folderExists(wormholeWorldName);
         if (wormholeWorldExists) {
            this.wormholeWorld = DungeonUtils.loadWorld(
               this.getContentPackagesConfigFields().getWormholeWorldName(),
               this.getContentPackagesConfigFields().getEnvironment(),
               this.contentPackagesConfigFields
            );
         }
      }
   }

   @Override
   public void initializeContent() {
      if (this.isInstalled) {
         this.getEntities();
         this.qualifyEntities();
         this.getChests();
         this.getNPCs();
      }
   }

   @Override
   public void doUninstall(Player player) {
      super.doUninstall(player);
      if (!this.isInstalled) {
         this.customBossEntityList.clear();
         this.treasureChestList.clear();
         this.npcEntities.clear();
         this.wormholeWorld = null;
      }
   }

   private void getEntities() {
      for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet()) {
         if (regionalBossEntity != null && this.matchesPackageWorld(regionalBossEntity.getWorldName())) {
            this.customBossEntityList.add(regionalBossEntity);
         }
      }
   }

   private void qualifyEntities() {
      DungeonUtils.Pair lowestAndHighestValues = DungeonUtils.getLowestAndHighestLevels(this.customBossEntityList);
      this.lowestLevel = lowestAndHighestValues.getLowestValue();
      this.highestLevel = lowestAndHighestValues.getHighestValue();
   }

   private void getChests() {
      for (TreasureChest treasureChest : TreasureChest.getTreasureChestHashMap().values()) {
         if (treasureChest != null && this.matchesPackageWorld(treasureChest.getWorldName())) {
            this.treasureChestList.add(treasureChest);
         }
      }
   }

   private void getNPCs() {
      for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values()) {
         if (npcEntity != null && this.matchesPackageWorld(npcEntity.getWorldName())) {
            this.npcEntities.add(npcEntity);
         }
      }
   }

   private boolean matchesPackageWorld(String worldName) {
      return worldName == null
         ? false
         : this.world != null && worldName.equals(this.world.getName()) || this.wormholeWorld != null && worldName.equals(this.wormholeWorld.getName());
   }

   @Override
   public int getLowestLevel() {
      return this.lowestLevel;
   }

   @Override
   public int getHighestLevel() {
      return this.highestLevel;
   }

   @Generated
   public World getWormholeWorld() {
      return this.wormholeWorld;
   }
}
