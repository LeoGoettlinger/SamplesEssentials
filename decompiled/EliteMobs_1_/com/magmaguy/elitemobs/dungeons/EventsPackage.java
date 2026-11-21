package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EventsPackage extends EMPackage {
   private final List<CustomEventsConfigFields> customEvents = new ArrayList<>();
   private boolean allFilesDownloaded = true;

   public EventsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);

      for (String customEventName : contentPackagesConfigFields.getCustomEventFilenames()) {
         if (CustomEventsConfig.getCustomEvent(customEventName) != null) {
            this.customEvents.add(CustomEventsConfig.getCustomEvent(customEventName));
         } else {
            this.allFilesDownloaded = false;
         }
      }
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      boolean allInstalled = true;
      boolean someInstalled = false;
      if (this.customEvents.isEmpty()) {
         return ContentPackage.ContentState.NOT_DOWNLOADED;
      } else {
         for (CustomEventsConfigFields customEventsConfigFields : this.customEvents) {
            if (!customEventsConfigFields.isEnabled()) {
               allInstalled = false;
            }

            if (customEventsConfigFields.isEnabled()) {
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
            ? DungeonsConfig.getEventsInstallingMessage().replace("$count", String.valueOf(this.customEvents.size()))
            : DungeonsConfig.getEventsUninstallingMessage().replace("$count", String.valueOf(this.customEvents.size()));
         notify(player, actionMessage);
         notify(player, DungeonsConfig.getEventsSavingMessage().replace("$count", String.valueOf(this.customEvents.size())));
         submitBulkMemberSaves(player, this.customEvents, enable, DungeonsConfig.getEventsReloadingMessage());
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
