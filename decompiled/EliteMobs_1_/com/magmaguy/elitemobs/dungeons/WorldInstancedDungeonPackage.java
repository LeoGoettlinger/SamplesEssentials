package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonInstallEvent;
import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import java.io.File;
import org.bukkit.entity.Player;

public class WorldInstancedDungeonPackage extends EMPackage implements CombatContent {
   private int level;

   public WorldInstancedDungeonPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);
   }

   @Override
   public void doInstall(Player player) {
      DungeonInstallEvent event = new DungeonInstallEvent(this.contentPackagesConfigFields);
      new EventCaller(event);
      this.contentPackagesConfigFields.simpleInstall();
      notify(player, DungeonsConfig.getInstancedDungeonInstalledMessage().replace("$name", this.contentPackagesConfigFields.getFilename()));
      if (!this.contentPackagesConfigFields.isEnchantmentChallenge()) {
         notify(player, DungeonsConfig.getInstancedDungeonAccessMessage());
         notify(player, DungeonsConfig.getInstancedDungeonInstallNote());
      } else {
         notify(player, DungeonsConfig.getEnchantmentDungeonInstalledMessage());
         ContentPackagesConfig.getEnchantedChallengeDungeonPackages().put(this.contentPackagesConfigFields.getFilename(), this.contentPackagesConfigFields);
      }

      this.isInstalled = true;
   }

   @Override
   public void doUninstall(Player player) {
      DungeonUninstallEvent event = new DungeonUninstallEvent(this.contentPackagesConfigFields);
      new EventCaller(event);
      this.contentPackagesConfigFields.simpleUninstall();
      this.isInstalled = false;
      notify(player, DungeonsConfig.getContentUninstalledMessage().replace("$name", this.contentPackagesConfigFields.getName()));
   }

   @Override
   public void baseInitialization() {
      this.level = this.contentPackagesConfigFields.getContentLevel();
      File file = new File(
         MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath()
            + File.separatorChar
            + "world_blueprints"
            + File.separatorChar
            + this.contentPackagesConfigFields.getDungeonConfigFolderName()
      );
      if (file.exists()
         && WorldInstantiator.validateBlueprint(
            this.contentPackagesConfigFields.getWorldName(),
            this.contentPackagesConfigFields.getDungeonConfigFolderName(),
            this.contentPackagesConfigFields.getEnvironment()
         )) {
         this.isDownloaded = true;
         this.isInstalled = this.contentPackagesConfigFields.isEnabled();
      } else {
         this.isDownloaded = false;
         this.isInstalled = false;
      }
   }

   @Override
   public void initializeContent() {
   }

   @Override
   public int getLowestLevel() {
      return this.level;
   }

   @Override
   public int getHighestLevel() {
      return this.level;
   }
}
