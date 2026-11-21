package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemsPackage extends EMPackage {
   private final List<CustomItemsConfigFields> customItems = new ArrayList<>();
   private boolean allFilesDownloaded = true;

   public ItemsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);

      for (String customItemFilename : contentPackagesConfigFields.getCustomItemFilenames()) {
         if (CustomItemsConfig.getCustomItems().containsKey(customItemFilename)) {
            this.customItems.add(CustomItemsConfig.getCustomItems().get(customItemFilename));
         } else {
            this.allFilesDownloaded = false;
         }
      }
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      boolean allInstalled = true;
      boolean someInstalled = false;
      if (this.customItems.isEmpty()) {
         return ContentPackage.ContentState.NOT_DOWNLOADED;
      } else {
         for (CustomItemsConfigFields customItem : this.customItems) {
            if (!customItem.isEnabled()) {
               allInstalled = false;
            }

            if (customItem.isEnabled()) {
               someInstalled = true;
            }
         }

         if (allInstalled) {
            this.isInstalled = true;
            return ContentPackage.ContentState.INSTALLED;
         } else if (someInstalled) {
            this.isInstalled = true;
            return ContentPackage.ContentState.PARTIALLY_INSTALLED;
         } else if (this.allFilesDownloaded) {
            this.isDownloaded = true;
            return ContentPackage.ContentState.NOT_INSTALLED;
         } else {
            return ContentPackage.ContentState.NOT_DOWNLOADED;
         }
      }
   }

   private void handleInstallation(Player player, boolean enable) {
      if (bulkMemberTogglesLocked()) {
         notify(player, DungeonsConfig.getContentToggleInProgressMessage());
      } else {
         String actionMessage = enable
            ? DungeonsConfig.getItemsInstallingMessage().replace("$count", String.valueOf(this.customItems.size()))
            : DungeonsConfig.getItemsUninstallingMessage().replace("$count", String.valueOf(this.customItems.size()));
         notify(player, actionMessage);
         notify(player, DungeonsConfig.getItemsSavingMessage().replace("$count", String.valueOf(this.customItems.size())));
         submitBulkMemberSaves(player, this.customItems, enable, DungeonsConfig.getItemsReloadingMessage());
      }
   }

   @Override
   protected ItemStack getPartiallyInstalledItemStack() {
      return this.generateItemStackWithIcon(
         List.of(DungeonsConfig.getBundledContentPartialLine1(), DungeonsConfig.getBundledContentPartialLine2(), DungeonsConfig.getBundledContentPartialLine3()),
         Material.ORANGE_STAINED_GLASS_PANE,
         "nightbreak:ui/gray_x"
      );
   }

   @Override
   public void doInstall(Player player) {
      this.handleInstallation(player, true);
   }

   @Override
   public void doUninstall(Player player) {
      this.handleInstallation(player, false);
   }

   @Override
   public void doDownload(Player player) {
      this.handleInstallation(player, true);
   }

   @Override
   public void baseInitialization() {
   }

   @Override
   public void initializeContent() {
   }
}
