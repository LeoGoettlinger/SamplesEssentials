package com.magmaguy.elitemobs.magmacore.menus;

import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakManagedContent;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakSetupMenuHelper;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NightbreakBulkActionPackage<T extends NightbreakManagedContent> extends ContentPackage {
   private final String pluginDisplayName;
   private final String contentPageUrl;
   private final String commandToRun;
   private final List<T> allPackages;
   private final ItemStack displayIcon;

   public NightbreakBulkActionPackage(String pluginDisplayName, String contentPageUrl, String commandToRun, List<T> allPackages) {
      this.pluginDisplayName = pluginDisplayName;
      this.contentPageUrl = contentPageUrl;
      this.commandToRun = commandToRun;
      this.allPackages = allPackages;
      this.displayIcon = this.buildIcon();
   }

   private ItemStack buildIcon() {
      String iconModel;
      Material baseMaterial;
      String displayName;
      List<String> lore;
      if (!NightbreakAccount.hasToken()) {
         iconModel = "nightbreak:ui/redcross";
         baseMaterial = Material.RED_STAINED_GLASS_PANE;
         displayName = "&cDownload All";
         lore = List.of("&7No account token linked.", "&7Click for setup instructions.");
      } else if (NightbreakAccount.hasAuthFailure()) {
         iconModel = "nightbreak:ui/redcross";
         baseMaterial = Material.RED_STAINED_GLASS_PANE;
         displayName = "&eUpdate Account Token";
         lore = List.of("&7Your saved account token needs", "&7to be updated before downloads work.");
      } else {
         long notDownloadedCount = this.countNotDownloaded();
         long outdatedCount = this.countOutdated();
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

      return NightbreakSetupVisuals.bulkActionItem(displayName, lore, baseMaterial, iconModel);
   }

   private long countNotDownloaded() {
      Set<String> seenSlugs = new HashSet<>();
      long count = 0L;

      for (T pkg : this.allPackages) {
         String slug = pkg.getNightbreakSlug();
         if (slug != null
            && !slug.isEmpty()
            && !seenSlugs.contains(slug)
            && !pkg.isDownloaded()
            && pkg.getCachedAccessInfo() != null
            && pkg.getCachedAccessInfo().hasAccess) {
            count++;
            seenSlugs.add(slug);
         }
      }

      return count;
   }

   private long countOutdated() {
      Set<String> seenSlugs = new HashSet<>();
      long count = 0L;

      for (T pkg : this.allPackages) {
         if (pkg.isOutOfDate()) {
            String slug = pkg.getNightbreakSlug();
            if (slug != null && !slug.isEmpty() && (pkg.getCachedAccessInfo() == null || pkg.getCachedAccessInfo().hasAccess) && seenSlugs.add(slug)) {
               count++;
            }
         }
      }

      return count;
   }

   @Override
   public ItemStack getItemstack() {
      return this.displayIcon;
   }

   @Override
   public void onClick(Player player) {
      player.closeInventory();
      if (!NightbreakAccount.hasToken()) {
         player.sendMessage(ChatColorConverter.convert("&8&m----------------------------------------------------"));
         player.sendMessage(ChatColorConverter.convert("&eConnect this server first to install " + this.pluginDisplayName + " content in-game."));
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage("&6Account page: "),
                  SpigotMessage.hoverLinkMessage(
                     "&9&nhttps://nightbreak.io/account/", "&7Click to open the account token page", "https://nightbreak.io/account/"
                  )
               }
            );
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage("&6Content page: "),
                  SpigotMessage.hoverLinkMessage("&9&n" + this.contentPageUrl, "&7Click to browse content", this.contentPageUrl)
               }
            );
         player.sendMessage(ChatColorConverter.convert("&aUse &2/nightbreaklogin <token> &ato link your account, then click this button again."));
         player.sendMessage(ChatColorConverter.convert("&8&m----------------------------------------------------"));
      } else if (NightbreakAccount.hasAuthFailure()) {
         NightbreakSetupMenuHelper.sendTokenUpdatePrompt(player, this.pluginDisplayName);
      } else {
         boolean hasNotDownloaded = false;
         boolean hasOutdated = false;

         for (T pkg : this.allPackages) {
            String slug = pkg.getNightbreakSlug();
            if (slug != null && !slug.isEmpty()) {
               if (!pkg.isDownloaded() && pkg.getCachedAccessInfo() != null && pkg.getCachedAccessInfo().hasAccess) {
                  hasNotDownloaded = true;
               }

               if (pkg.isOutOfDate()) {
                  hasOutdated = true;
               }

               if (hasNotDownloaded && hasOutdated) {
                  break;
               }
            }
         }

         if (!hasNotDownloaded && !hasOutdated) {
            player.sendMessage(
               ChatColorConverter.convert("&a[" + this.pluginDisplayName + "] No new content to download! All available packages are already downloaded.")
            );
         } else {
            Bukkit.dispatchCommand(player, this.commandToRun);
         }
      }
   }

   @Override
   protected ItemStack getInstalledItemStack() {
      return this.displayIcon;
   }

   @Override
   protected ItemStack getPartiallyInstalledItemStack() {
      return this.displayIcon;
   }

   @Override
   protected ItemStack getNotInstalledItemStack() {
      return this.displayIcon;
   }

   @Override
   protected ItemStack getNotDownloadedItemStack() {
      return this.displayIcon;
   }

   @Override
   protected ItemStack getNeedsAccessItemStack() {
      return this.displayIcon;
   }

   @Override
   protected ItemStack getOutOfDateUpdatableItemStack() {
      return this.displayIcon;
   }

   @Override
   protected ItemStack getOutOfDateNoAccessItemStack() {
      return this.displayIcon;
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
