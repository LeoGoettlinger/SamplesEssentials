package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import com.magmaguy.elitemobs.magmacore.menus.NightbreakSetupIcons;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DownloadAllContentPackage<T extends NightbreakManagedContent> extends ContentPackage {
   private final Supplier<Collection<T>> packagesSupplier;
   private final String pluginName;
   private final String contentUrl;
   private final String downloadAllCommand;

   public DownloadAllContentPackage(Supplier<Collection<T>> packagesSupplier, String pluginName, String contentUrl, String downloadAllCommand) {
      this.packagesSupplier = packagesSupplier;
      this.pluginName = pluginName;
      this.contentUrl = contentUrl;
      this.downloadAllCommand = downloadAllCommand == null ? "" : downloadAllCommand.trim();
   }

   @Override
   public ItemStack getItemstack() {
      String iconModel;
      Material baseMaterial;
      String displayName;
      List<String> lore;
      if (!NightbreakAccount.hasToken()) {
         iconModel = "nightbreak:ui/redcross";
         baseMaterial = Material.RED_STAINED_GLASS_PANE;
         displayName = this.includesPluginUpdate() ? "&cDownload Everything" : "&cDownload All Content";
         lore = List.of("&7No account token linked.", "&7Click for setup instructions.");
      } else if (NightbreakAccount.hasAuthFailure()) {
         iconModel = "nightbreak:ui/redcross";
         baseMaterial = Material.RED_STAINED_GLASS_PANE;
         displayName = "&eUpdate Account Token";
         lore = List.of("&7Your saved account token needs", "&7to be updated before downloads work.");
      } else if (this.includesPluginUpdate()) {
         iconModel = "nightbreak:ui/yellowcrown";
         baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
         displayName = "&eDownload / Update Everything";
         lore = List.of("&7Checks for a plugin update,", "&7then downloads and updates content.", "&7Restart the server to use plugin updates.");
      } else {
         Collection<T> allPackages = this.packagesSupplier.get();
         long notDownloadedCount = this.countNotDownloaded(allPackages);
         long outdatedCount = this.countOutdated(allPackages);
         if (notDownloadedCount > 0L && outdatedCount > 0L) {
            iconModel = "nightbreak:ui/yellowcrown";
            baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
            displayName = "&eDownload & Update All";
            lore = List.of(
               "&7Click to download &e" + notDownloadedCount + " &7new package" + (notDownloadedCount != 1L ? "s" : ""),
               "&7and update &e" + outdatedCount + " &7outdated package" + (outdatedCount != 1L ? "s" : "") + "."
            );
         } else if (notDownloadedCount > 0L) {
            iconModel = "nightbreak:ui/yellowcrown";
            baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
            displayName = "&eDownload All";
            lore = List.of("&7Click to download &e" + notDownloadedCount + " &7available package" + (notDownloadedCount != 1L ? "s" : "") + ".");
         } else if (outdatedCount > 0L) {
            iconModel = "nightbreak:ui/yellowcrown";
            baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
            displayName = "&eUpdate All";
            lore = List.of("&7Click to update &e" + outdatedCount + " &7outdated package" + (outdatedCount != 1L ? "s" : "") + ".");
         } else {
            iconModel = "nightbreak:ui/checkmark";
            baseMaterial = Material.LIME_STAINED_GLASS_PANE;
            displayName = "&aAll Content Up To Date";
            lore = List.of("&7All available content is downloaded", "&7and up to date!");
         }
      }

      ItemStack itemStack = ItemStackGenerator.generateItemStack(baseMaterial, displayName, lore);
      NightbreakSetupIcons.applyItemModel(itemStack, iconModel);
      return itemStack;
   }

   private long countNotDownloaded(Collection<T> allPackages) {
      Set<String> seenSlugs = new HashSet<>();
      long count = 0L;

      for (T contentPackage : allPackages) {
         String slug = contentPackage.getNightbreakSlug();
         if (slug != null
            && !slug.isEmpty()
            && seenSlugs.add(slug)
            && !contentPackage.isDownloaded()
            && (contentPackage.getCachedAccessInfo() == null || contentPackage.getCachedAccessInfo().hasAccess)) {
            count++;
         }
      }

      return count;
   }

   private long countOutdated(Collection<T> allPackages) {
      Set<String> seenSlugs = new HashSet<>();
      long count = 0L;

      for (T contentPackage : allPackages) {
         if (contentPackage.isOutOfDate()) {
            String slug = contentPackage.getNightbreakSlug();
            if (slug != null
               && !slug.isEmpty()
               && (contentPackage.getCachedAccessInfo() == null || contentPackage.getCachedAccessInfo().hasAccess)
               && seenSlugs.add(slug)) {
               count++;
            }
         }
      }

      return count;
   }

   @Override
   public void onClick(Player player) {
      player.closeInventory();
      if (!NightbreakAccount.hasToken()) {
         NightbreakSetupMenuHelper.sendNoTokenPrompt(player, this.pluginName, this.contentUrl);
      } else if (NightbreakAccount.hasAuthFailure()) {
         NightbreakSetupMenuHelper.sendTokenUpdatePrompt(player, this.pluginName);
      } else if (this.includesPluginUpdate()) {
         Bukkit.dispatchCommand(player, this.downloadAllCommand);
      } else {
         boolean hasNotDownloaded = false;
         boolean hasOutdated = false;

         for (T contentPackage : this.packagesSupplier.get()) {
            String slug = contentPackage.getNightbreakSlug();
            if (slug != null && !slug.isEmpty()) {
               if (!contentPackage.isDownloaded() && (contentPackage.getCachedAccessInfo() == null || contentPackage.getCachedAccessInfo().hasAccess)) {
                  hasNotDownloaded = true;
               }

               if (contentPackage.isOutOfDate() && (contentPackage.getCachedAccessInfo() == null || contentPackage.getCachedAccessInfo().hasAccess)) {
                  hasOutdated = true;
               }

               if (hasNotDownloaded && hasOutdated) {
                  break;
               }
            }
         }

         if (!hasNotDownloaded && !hasOutdated) {
            Logger.sendSimpleMessage(player, "&aAll available " + this.pluginName + " content is already downloaded and up to date.");
         } else {
            Bukkit.dispatchCommand(player, this.downloadAllCommand);
         }
      }
   }

   private boolean includesPluginUpdate() {
      return this.downloadAllCommand.endsWith(" downloadall");
   }

   @Override
   protected ItemStack getInstalledItemStack() {
      return this.getItemstack();
   }

   @Override
   protected ItemStack getPartiallyInstalledItemStack() {
      return this.getItemstack();
   }

   @Override
   protected ItemStack getNotInstalledItemStack() {
      return this.getItemstack();
   }

   @Override
   protected ItemStack getNotDownloadedItemStack() {
      return this.getItemstack();
   }

   @Override
   protected ItemStack getNeedsAccessItemStack() {
      return this.getItemstack();
   }

   @Override
   protected ItemStack getOutOfDateUpdatableItemStack() {
      return this.getItemstack();
   }

   @Override
   protected ItemStack getOutOfDateNoAccessItemStack() {
      return this.getItemstack();
   }

   @Override
   protected void doInstall(Player player) {
   }

   @Override
   protected void doUninstall(Player player) {
   }

   @Override
   protected void doDownload(Player player) {
   }

   @Override
   protected void doShowAccessInfo(Player player) {
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      return ContentPackage.ContentState.INSTALLED;
   }
}
