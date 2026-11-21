package de.theredend2000.advancedhunt.menu.common;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import de.theredend2000.advancedhunt.util.HeadHelper;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import de.theredend2000.advancedhunt.util.MaterialUtils;
import de.theredend2000.advancedhunt.util.XMaterialHelper;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.ItemStack;

public final class TreasureMenuItemSupport {
   private TreasureMenuItemSupport() {
   }

   public static CompletableFuture<SkullInfo> loadSkullInfo(Main plugin, UUID treasureId) {
      return plugin.getTreasureManager().getFullTreasureAsync(treasureId).thenApply(fullTreasure -> {
         if (fullTreasure == null) {
            return null;
         } else {
            HeadHelper.SkullProfileData profileData = HeadHelper.getSkullProfileData(fullTreasure.getNbtData());
            return profileData != null && profileData.hasRenderableData() ? new SkullInfo(profileData.texture(), profileData.ownerName()) : null;
         }
      });
   }

   public static ItemStack resolveDisplayItem(TreasureCore treasureCore, SkullInfo skullInfo) {
      ItemStack item = null;
      if ("ITEMS_ADDER".equalsIgnoreCase(treasureCore.getMaterial())) {
         item = ItemsAdderAdapter.getCustomItem(treasureCore.getBlockState());
      }

      if (item != null && MaterialUtils.isAir(item.getType())) {
         item = null;
      }

      if (item == null) {
         item = XMaterialHelper.getItemStack(treasureCore.getMaterial(), treasureCore.getBlockState());
         if (item != null && MaterialUtils.isAir(item.getType())) {
            item = null;
         }
      }

      if (shouldRenderAsPlayerHead(treasureCore, skullInfo)) {
         item = ensurePlayerHeadDisplayItem(item);
      }

      return item != null ? item : new ItemStack(XMaterial.CHEST.get());
   }

   public static void applySkullInfo(ItemBuilder builder, SkullInfo skullInfo) {
      if (builder != null && skullInfo != null) {
         if (skullInfo.texture() != null) {
            builder.setSkullTexture(skullInfo.texture());
         } else if (skullInfo.ownerName() != null) {
            builder.setSkullOwner(skullInfo.ownerName());
         }
      }
   }

   private static boolean shouldRenderAsPlayerHead(TreasureCore treasureCore, SkullInfo skullInfo) {
      return skullInfo != null || HeadHelper.isPlayerHeadMaterialName(treasureCore.getMaterial(), treasureCore.getBlockState());
   }

   private static ItemStack ensurePlayerHeadDisplayItem(ItemStack item) {
      ItemStack normalized = item != null ? PlatformAccess.get().ensurePlayerHeadItem(item) : null;
      if (normalized != null && HeadHelper.isPlayerHead(normalized)) {
         return normalized;
      } else {
         ItemStack fallback = XMaterial.PLAYER_HEAD.parseItem();
         if (fallback == null) {
            return normalized;
         } else {
            ItemStack fallbackHead = PlatformAccess.get().ensurePlayerHeadItem(fallback);
            return fallbackHead != null ? fallbackHead : normalized;
         }
      }
   }
}
