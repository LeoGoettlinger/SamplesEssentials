package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldPackage extends EMPackage {
   protected World world;

   public WorldPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);
   }

   @Override
   public void doInstall(Player player) {
      DungeonUtils.loadWorld(this);
      this.contentPackagesConfigFields.installWorld();
      if (player != null) {
         player.teleport(this.contentPackagesConfigFields.getTeleportLocation());
      }

      this.world = this.contentPackagesConfigFields.getTeleportLocation().getWorld();
      if (this.contentPackagesConfigFields.getSong() != null) {
         new CustomMusic(this.contentPackagesConfigFields.getSong(), this.contentPackagesConfigFields, this.world);
      }

      for (Wormhole wormhole : Wormhole.getWormholes()) {
         wormhole.onDungeonInstall(this.contentPackagesConfigFields.getFilename());
      }

      this.isInstalled = true;
      notify(player, DungeonsConfig.getContentInstalledMessage().replace("$name", this.contentPackagesConfigFields.getName()));
   }

   @Override
   public void doUninstall(Player player) {
      DungeonUninstallEvent event = new DungeonUninstallEvent(this.contentPackagesConfigFields);
      new EventCaller(event);
      this.isInstalled = false;
      if (!DungeonUtils.unloadWorld(this)) {
         this.isInstalled = true;
         notify(player, DungeonsConfig.getContentUninstallFailedMessage().replace("$name", this.contentPackagesConfigFields.getName()));
      } else {
         for (Wormhole wormhole : Wormhole.getWormholes()) {
            wormhole.onDungeonUninstall(this.contentPackagesConfigFields.getFilename());
         }

         this.contentPackagesConfigFields.uninstallWorld();
         this.world = null;
         notify(player, DungeonsConfig.getContentUninstalledMessage().replace("$name", this.contentPackagesConfigFields.getName()));
      }
   }

   @Override
   public void baseInitialization() {
      if (this.contentPackagesConfigFields.getWorldName() != null && !this.contentPackagesConfigFields.getWorldName().isEmpty()) {
         content.put(this.contentPackagesConfigFields.getWorldName(), this);
         if (this.contentPackagesConfigFields.getWormholeWorldName() != null && !this.contentPackagesConfigFields.getWormholeWorldName().isEmpty()) {
            content.put(this.contentPackagesConfigFields.getWormholeWorldName(), this);
         }

         if (Bukkit.getWorld(this.contentPackagesConfigFields.getWorldName()) != null) {
            this.isDownloaded = this.isInstalled = true;
            this.world = Bukkit.getWorld(this.contentPackagesConfigFields.getWorldName());
            EliteMobsWorld.create(this.world.getUID(), this.contentPackagesConfigFields);
            this.contentPackagesConfigFields.initializeWorld();
         } else {
            this.isInstalled = this.contentPackagesConfigFields.isEnabled();
            this.isDownloaded = WorldFolderResolver.folderExists(this.contentPackagesConfigFields.getWorldName());
            if (this.isDownloaded && this.isInstalled) {
               this.world = DungeonUtils.loadWorld(this);
               if (this.contentPackagesConfigFields.getSong() != null) {
                  new CustomMusic(this.contentPackagesConfigFields.getSong(), this.contentPackagesConfigFields, this.world);
               }

               this.contentPackagesConfigFields.initializeWorld();
            } else {
               this.isInstalled = false;
            }
         }
      } else {
         this.isDownloaded = this.isInstalled = false;
         Logger.warn("Packaged content " + this.contentPackagesConfigFields.getFilename() + " does not have a valid world name in the dungeon packager!");
      }
   }

   @Override
   public void initializeContent() {
   }

   @Generated
   public World getWorld() {
      return this.world;
   }
}
