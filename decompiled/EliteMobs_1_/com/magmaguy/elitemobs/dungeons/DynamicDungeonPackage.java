package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonInstallEvent;
import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import java.io.File;
import org.bukkit.entity.Player;

public class DynamicDungeonPackage extends EMPackage implements CombatContent {
   private static final int MIN_LEVEL = 5;
   private static final int MAX_LEVEL = 200;

   public DynamicDungeonPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);
   }

   @Override
   public void doInstall(Player player) {
      DungeonInstallEvent event = new DungeonInstallEvent(this.contentPackagesConfigFields);
      new EventCaller(event);
      this.contentPackagesConfigFields.simpleInstall();
      notify(player, DungeonsConfig.getDynamicDungeonInstalledMessage().replace("$name", this.contentPackagesConfigFields.getFilename()));
      notify(player, DungeonsConfig.getDynamicDungeonAccessMessage1());
      notify(player, DungeonsConfig.getDynamicDungeonAccessMessage2());
      notify(player, DungeonsConfig.getDynamicDungeonAccessMessage3());
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
      return 5;
   }

   @Override
   public int getHighestLevel() {
      return 200;
   }
}
