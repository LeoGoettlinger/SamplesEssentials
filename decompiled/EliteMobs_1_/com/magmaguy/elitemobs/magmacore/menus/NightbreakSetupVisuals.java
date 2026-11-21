package com.magmaguy.elitemobs.magmacore.menus;

import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class NightbreakSetupVisuals {
   private NightbreakSetupVisuals() {
   }

   public static ItemStack installedItem(String name, List<String> description) {
      return stateItem(
         name,
         description,
         List.of("&aContent is installed and up to date.", "&7Click to uninstall."),
         Material.GREEN_STAINED_GLASS_PANE,
         "nightbreak:ui/checkmark"
      );
   }

   public static ItemStack partiallyInstalledItem(String name, List<String> description) {
      return stateItem(
         name,
         description,
         List.of(
            "Content is partially installed!",
            "Some files are missing or disabled.",
            "Click to download the latest content again.",
            "This will restore the package to a clean state."
         ),
         Material.ORANGE_STAINED_GLASS_PANE,
         "nightbreak:ui/gray_x"
      );
   }

   public static ItemStack notInstalledItem(String name, List<String> description) {
      return stateItem(
         name, description, List.of("Content is downloaded but disabled.", "Click to install!"), Material.YELLOW_STAINED_GLASS_PANE, "nightbreak:ui/gray_x"
      );
   }

   public static ItemStack notDownloadedItem(String name, List<String> description, String nightbreakSlug, NightbreakAccount.AccessInfo cachedAccessInfo) {
      String modelId;
      if (nightbreakSlug == null || nightbreakSlug.isEmpty()) {
         modelId = "nightbreak:ui/unlocked";
      } else if (!NightbreakAccount.hasToken()) {
         modelId = "nightbreak:ui/lockedunlinked";
      } else if (cachedAccessInfo != null && cachedAccessInfo.hasAccess) {
         modelId = "nightbreak:ui/unlocked";
      } else {
         modelId = "nightbreak:ui/lockedunpaid";
      }

      return stateItem(name, description, List.of("Content is not downloaded yet.", "Click to download it!"), Material.YELLOW_STAINED_GLASS_PANE, modelId);
   }

   public static ItemStack needsAccessItem(String name, List<String> description, NightbreakAccount.AccessInfo cachedAccessInfo) {
      List<String> tooltip = new ArrayList<>();
      tooltip.add("You need Nightbreak access for this content.");
      tooltip.add("Click to see access links.");
      if (cachedAccessInfo != null) {
         if (cachedAccessInfo.patreonLink != null && !cachedAccessInfo.patreonLink.isEmpty()) {
            tooltip.add("Available on Patreon.");
         }

         if (cachedAccessInfo.itchLink != null && !cachedAccessInfo.itchLink.isEmpty()) {
            tooltip.add("Available on itch.io.");
         }
      }

      return stateItem(name, description, tooltip, Material.PURPLE_STAINED_GLASS_PANE, "nightbreak:ui/lockedunpaid");
   }

   public static ItemStack outOfDateUpdatableItem(String name, List<String> description, String nightbreakSlug) {
      String modelId;
      if (nightbreakSlug == null || nightbreakSlug.isEmpty()) {
         modelId = "nightbreak:ui/update";
      } else if (!NightbreakAccount.hasToken()) {
         modelId = "nightbreak:ui/updateunlinked";
      } else {
         modelId = "nightbreak:ui/update";
      }

      return stateItem(name, description, List.of("An update is available!", "Click to download the update."), Material.YELLOW_STAINED_GLASS_PANE, modelId);
   }

   public static ItemStack outOfDateNoAccessItem(String name, List<String> description) {
      return stateItem(
         name,
         description,
         List.of("An update is available!", "You need Nightbreak access before you can update it.", "Click to see access links."),
         Material.ORANGE_STAINED_GLASS_PANE,
         "nightbreak:ui/updateunpaid"
      );
   }

   public static ItemStack bulkActionItem(String name, List<String> lore, Material material, String modelId) {
      return stateItem(name, List.of(), lore, material, modelId);
   }

   public static ItemStack stateItem(String name, List<String> description, List<String> specificTooltip, Material material, String modelId) {
      List<String> tooltip = new ArrayList<>(specificTooltip);
      tooltip.addAll(description);
      ItemStack itemStack = ItemStackGenerator.generateItemStack(material, name, tooltip);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
         itemStack.setItemMeta(itemMeta);
      }

      NightbreakSetupIcons.applyItemModel(itemStack, modelId);
      return itemStack;
   }
}
