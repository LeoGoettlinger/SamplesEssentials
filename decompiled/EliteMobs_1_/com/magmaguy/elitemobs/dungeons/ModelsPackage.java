package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import org.bukkit.entity.Player;

public class ModelsPackage extends EMPackage {
   public ModelsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);
   }

   @Override
   public void doInstall(Player player) {
      Logger.sendMessage(player, DungeonsConfig.getModelsAutoInstallMessage());
   }

   @Override
   public void doUninstall(Player player) {
      Logger.sendMessage(player, DungeonsConfig.getModelsCannotUninstallMessage());
   }

   @Override
   public void baseInitialization() {
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      boolean allInstalled = true;
      boolean someInstalled = false;

      for (String customModel : this.contentPackagesConfigFields.getModelNames()) {
         boolean exists = CustomModel.modelExists(customModel);
         if (exists) {
            someInstalled = true;
         }

         if (!exists) {
            allInstalled = false;
         }
      }

      if (!someInstalled) {
         this.isInstalled = false;
         this.isDownloaded = false;
         return ContentPackage.ContentState.NOT_DOWNLOADED;
      } else if (allInstalled) {
         this.isInstalled = true;
         this.isDownloaded = true;
         return ContentPackage.ContentState.INSTALLED;
      } else {
         this.isDownloaded = true;
         return ContentPackage.ContentState.PARTIALLY_INSTALLED;
      }
   }

   @Override
   public void initializeContent() {
   }
}
