package com.magmaguy.elitemobs.magmacore.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ContentPackage extends MenuButton {
   public ItemStack getItemstack() {
      return switch (this.getContentState()) {
         case INSTALLED -> this.getInstalledItemStack();
         case PARTIALLY_INSTALLED -> this.getPartiallyInstalledItemStack();
         case NOT_INSTALLED -> this.getNotInstalledItemStack();
         case NOT_DOWNLOADED -> this.getNotDownloadedItemStack();
         case NEEDS_ACCESS -> this.getNeedsAccessItemStack();
         case OUT_OF_DATE_UPDATABLE -> this.getOutOfDateUpdatableItemStack();
         case OUT_OF_DATE_NO_ACCESS -> this.getOutOfDateNoAccessItemStack();
      };
   }

   protected abstract ItemStack getInstalledItemStack();

   protected abstract ItemStack getPartiallyInstalledItemStack();

   protected abstract ItemStack getNotInstalledItemStack();

   protected abstract ItemStack getNotDownloadedItemStack();

   protected abstract void doInstall(Player var1);

   protected abstract void doUninstall(Player var1);

   protected abstract void doDownload(Player var1);

   protected abstract void doShowAccessInfo(Player var1);

   protected abstract ItemStack getNeedsAccessItemStack();

   protected abstract ItemStack getOutOfDateUpdatableItemStack();

   protected abstract ItemStack getOutOfDateNoAccessItemStack();

   protected abstract ContentPackage.ContentState getContentState();

   @Override
   public void onClick(Player player) {
      player.closeInventory();
      switch (this.getContentState()) {
         case INSTALLED:
            this.doUninstall(player);
            break;
         case PARTIALLY_INSTALLED:
            this.doDownload(player);
            break;
         case NOT_INSTALLED:
            this.doInstall(player);
            break;
         case NOT_DOWNLOADED:
            this.doDownload(player);
            break;
         case NEEDS_ACCESS:
            this.doShowAccessInfo(player);
            break;
         case OUT_OF_DATE_UPDATABLE:
            this.doDownload(player);
            break;
         case OUT_OF_DATE_NO_ACCESS:
            this.doShowAccessInfo(player);
      }
   }

   public static enum ContentState {
      INSTALLED,
      PARTIALLY_INSTALLED,
      NOT_INSTALLED,
      NOT_DOWNLOADED,
      NEEDS_ACCESS,
      OUT_OF_DATE_UPDATABLE,
      OUT_OF_DATE_NO_ACCESS;
   }
}
