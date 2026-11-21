package com.magmaguy.elitemobs.advancedcombat.weapons;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructionContext;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AdvancedMagicWeaponItems {
   public static final String STAFF_ITEM_ID = "advanced_arcane_staff";
   public static final String WAND_ITEM_ID = "advanced_arcane_wand";
   public static final String STAFF_FMM_ITEM_ID = "fmm_default_arcane_staff";
   public static final String WAND_FMM_ITEM_ID = "fmm_default_arcane_wand";
   private static final List<String> DEBUG_LOADOUT_IDS = List.of("advanced_arcane_staff", "advanced_arcane_wand");

   private AdvancedMagicWeaponItems() {
   }

   public static void register() {
      register(null);
   }

   public static void register(ItemConstructionContext construction) {
      if (construction == null ? Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels") : construction.modelsEnabled()) {
         if (CustomItem.getCustomItem("advanced_arcane_staff") == null) {
            new CustomItem(staff(), construction);
         }

         if (CustomItem.getCustomItem("advanced_arcane_wand") == null) {
            new CustomItem(wand(), construction);
         }
      }
   }

   public static List<ItemStack> generateDebugLoadout(int itemLevel, Player player) {
      if (!Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         return List.of();
      } else {
         List<ItemStack> items = new ArrayList<>(DEBUG_LOADOUT_IDS.size());

         for (String itemId : DEBUG_LOADOUT_IDS) {
            CustomItem customItem = CustomItem.getCustomItem(itemId);
            if (customItem == null) {
               Logger.warn("[Alpha] Advanced Combat System debug item '" + itemId + "' is not registered.");
            } else {
               ItemStack itemStack = customItem.generateItemStackExact(itemLevel, player, null);
               if (itemStack != null) {
                  SkillType expectedSkill = "advanced_arcane_staff".equals(itemId) ? SkillType.STAVES : SkillType.WANDS;
                  boolean validIdentity = WeaponIdentityResolver.progressionSkill(itemStack) == expectedSkill;
                  if (!validIdentity) {
                     Logger.warn("[Alpha] Advanced Combat System debug item '" + itemId + "' lost its registered magic-weapon identity and was not granted.");
                  } else {
                     items.add(itemStack);
                  }
               }
            }
         }

         return List.copyOf(items);
      }
   }

   static List<String> debugLoadoutIds() {
      return DEBUG_LOADOUT_IDS;
   }

   private static Material staffMaterial() {
      try {
         return Material.WOODEN_SPEAR;
      } catch (NoSuchFieldError var1) {
         return Material.STICK;
      }
   }

   private static CustomItemsConfigFields staff() {
      CustomItemsConfigFields fields = new CustomItemsConfigFields(
         "advanced_arcane_staff.yml",
         true,
         staffMaterial(),
         "<gradient:#76EFC4:#00C96E:#0A7A44>Advanced Staff of Wonders</gradient>",
         List.of("&7Left-click for a weak close-range strike.", "&7Right-click to launch a slow area fireball.", "&8Scales with Staves and item level.")
      );
      fields.setWeaponType(SkillType.STAVES);
      fields.setProceduralEnchantments(true);
      fields.setFmmItemModel("fmm_default_arcane_staff");
      fields.setScalability(CustomItem.Scalability.SCALABLE);
      fields.setItemType(CustomItem.ItemType.CUSTOM);
      fields.setLevel(1);
      fields.setShowSource(false);
      return fields;
   }

   private static CustomItemsConfigFields wand() {
      CustomItemsConfigFields fields = new CustomItemsConfigFields(
         "advanced_arcane_wand.yml",
         true,
         Material.BLAZE_ROD,
         "<gradient:#D9B8FF:#8A5CFF>Advanced Arcane Wand</gradient>",
         List.of(
            "&7Left-click to fire an aim-assisted magic missile.",
            "&7Missiles arc upward and sideways toward their target.",
            "&8Scales with Wands and item level."
         )
      );
      fields.setWeaponType(SkillType.WANDS);
      fields.setProceduralEnchantments(true);
      fields.setFmmItemModel("fmm_default_arcane_wand");
      fields.setScalability(CustomItem.Scalability.SCALABLE);
      fields.setItemType(CustomItem.ItemType.CUSTOM);
      fields.setLevel(1);
      fields.setShowSource(false);
      return fields;
   }
}
