package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractNightbreakContentPackage extends ContentPackage implements NightbreakManagedContent {
   private boolean outOfDate = false;
   private NightbreakAccount.AccessInfo cachedAccessInfo = null;

   protected abstract JavaPlugin getOwnerPlugin();

   protected abstract String getPluginDisplayName();

   protected abstract String getContentPageUrl();

   protected abstract List<String> getPackageDescription();

   protected abstract String getManualImportsFolderName();

   protected abstract String getManualReloadCommand();

   protected abstract void onDownloadStateSaved(Player var1);

   @Override
   protected ItemStack getInstalledItemStack() {
      return NightbreakSetupMenuHelper.createInstalledItem(this.getDisplayName(), this.getPackageDescription());
   }

   @Override
   protected ItemStack getPartiallyInstalledItemStack() {
      return NightbreakSetupMenuHelper.createPartiallyInstalledItem(this.getDisplayName(), this.getPackageDescription());
   }

   @Override
   protected ItemStack getNotInstalledItemStack() {
      return NightbreakSetupMenuHelper.createNotInstalledItem(this.getDisplayName(), this.getPackageDescription());
   }

   @Override
   protected ItemStack getNotDownloadedItemStack() {
      return NightbreakSetupMenuHelper.createNotDownloadedItem(
         this.getDisplayName(), this.getPackageDescription(), this.getNightbreakSlug(), this.getCachedAccessInfo()
      );
   }

   @Override
   protected ItemStack getNeedsAccessItemStack() {
      return NightbreakSetupMenuHelper.createNeedsAccessItem(this.getDisplayName(), this.getPackageDescription(), this.getCachedAccessInfo());
   }

   @Override
   protected ItemStack getOutOfDateUpdatableItemStack() {
      return NightbreakSetupMenuHelper.createOutOfDateUpdatableItem(this.getDisplayName(), this.getPackageDescription(), this.getNightbreakSlug());
   }

   @Override
   protected ItemStack getOutOfDateNoAccessItemStack() {
      return NightbreakSetupMenuHelper.createOutOfDateNoAccessItem(this.getDisplayName(), this.getPackageDescription());
   }

   @Override
   protected void doDownload(Player player) {
      player.closeInventory();
      String slug = this.getNightbreakSlug();
      if (slug == null || slug.isEmpty()) {
         NightbreakSetupMenuHelper.sendManualDownloadMessage(player, this.getDownloadLink(), this.getManualImportsFolderName(), this.getManualReloadCommand());
      } else if (!NightbreakAccount.hasToken()) {
         NightbreakSetupMenuHelper.sendNoTokenPrompt(player, this.getPluginDisplayName(), this.getContentPageUrl());
      } else {
         JavaPlugin plugin = this.getOwnerPlugin();
         NightbreakContentManager.OperationGate lifecycleGate = NightbreakBulkDownloader.captureOperationGate(plugin);
         Logger.sendSimpleMessage(player, "&aChecking Nightbreak access for &2" + this.getDisplayName() + "&a...");
         NightbreakContentManager.checkAccessAsync(
            plugin,
            slug,
            lifecycleGate,
            accessInfo -> {
               if (lifecycleGate.isCurrent()) {
                  this.setCachedAccessInfo(accessInfo);
                  if (player.isOnline()) {
                     if (accessInfo == null) {
                        if (NightbreakAccount.hasAuthFailure()) {
                           NightbreakSetupMenuHelper.sendTokenUpdatePrompt(player, this.getPluginDisplayName());
                        } else {
                           Logger.sendSimpleMessage(player, "&cFailed to contact Nightbreak for access information.");
                        }
                     } else if (!accessInfo.hasAccess) {
                        this.doShowAccessInfo(player);
                     } else {
                        File importsFolder = new File(plugin.getDataFolder(), "imports");
                        if (!importsFolder.exists()) {
                           importsFolder.mkdirs();
                        }

                        NightbreakContentManager.downloadAsync(
                           plugin,
                           slug,
                           importsFolder,
                           player,
                           lifecycleGate,
                           success -> {
                              if (lifecycleGate.isCurrent() && player.isOnline() && success) {
                                 this.enableAfterDownload()
                                    .whenComplete(
                                       (ignored, throwable) -> {
                                          if (lifecycleGate.isCurrent()) {
                                             try {
                                                Bukkit.getScheduler()
                                                   .runTask(
                                                      plugin,
                                                      () -> {
                                                         if (lifecycleGate.isCurrent()) {
                                                            if (throwable != null) {
                                                               throwable.printStackTrace();
                                                               Logger.sendSimpleMessage(
                                                                  player,
                                                                  "&cFailed to update " + this.getPluginDisplayName() + " package state. Check the console."
                                                               );
                                                            } else {
                                                               this.onDownloadStateSaved(player);
                                                            }
                                                         }
                                                      }
                                                   );
                                             } catch (RuntimeException var7) {
                                             }
                                          }
                                       }
                                    );
                              }
                           }
                        );
                     }
                  }
               }
            }
         );
      }
   }

   @Override
   protected void doShowAccessInfo(Player player) {
      NightbreakSetupMenuHelper.sendAccessInfo(player, this.getDisplayName(), this.getCachedAccessInfo(), this.getContentPageUrl());
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      boolean downloaded = this.isDownloaded();
      boolean installed = this.isInstalled();
      boolean hasNightbreakSlug = this.getNightbreakSlug() != null && !this.getNightbreakSlug().isEmpty();
      if (installed && this.isOutOfDate()) {
         return hasNightbreakSlug && NightbreakAccount.hasToken() && this.getCachedAccessInfo() != null && !this.getCachedAccessInfo().hasAccess
            ? ContentPackage.ContentState.OUT_OF_DATE_NO_ACCESS
            : ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
      } else if (installed) {
         return ContentPackage.ContentState.INSTALLED;
      } else if (downloaded) {
         return ContentPackage.ContentState.NOT_INSTALLED;
      } else {
         return hasNightbreakSlug && NightbreakAccount.hasToken() && this.getCachedAccessInfo() != null && !this.getCachedAccessInfo().hasAccess
            ? ContentPackage.ContentState.NEEDS_ACCESS
            : ContentPackage.ContentState.NOT_DOWNLOADED;
      }
   }

   protected void handleStateSave(Player player, CompletableFuture<Void> future, Runnable onSuccess, String failureMessage) {
      Bukkit.getScheduler()
         .runTaskAsynchronously(
            this.getOwnerPlugin(), () -> future.whenComplete((ignored, throwable) -> Bukkit.getScheduler().runTask(this.getOwnerPlugin(), () -> {
               if (throwable != null) {
                  throwable.printStackTrace();
                  if (player.isOnline()) {
                     Logger.sendSimpleMessage(player, failureMessage);
                  }
               } else {
                  onSuccess.run();
               }
            }))
         );
   }

   @Generated
   @Override
   public boolean isOutOfDate() {
      return this.outOfDate;
   }

   @Generated
   @Override
   public void setOutOfDate(boolean outOfDate) {
      this.outOfDate = outOfDate;
   }

   @Generated
   @Override
   public NightbreakAccount.AccessInfo getCachedAccessInfo() {
      return this.cachedAccessInfo;
   }

   @Generated
   @Override
   public void setCachedAccessInfo(NightbreakAccount.AccessInfo cachedAccessInfo) {
      this.cachedAccessInfo = cachedAccessInfo;
   }
}
