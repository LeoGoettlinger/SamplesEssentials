package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.LootItemPolicy;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.items.upgradesystem.EliteEnchantmentItems;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.freeminecraftmodels.api.ModelItemAPI;
import com.magmaguy.freeminecraftmodels.api.ScriptedItemAPI;
import com.magmaguy.freeminecraftmodels.api.ItemConstructionSnapshot.ItemData;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemConstructor {
   public static ItemStack constructItem(
      int level,
      String rawName,
      Material material,
      HashMap<Enchantment, Integer> enchantments,
      HashMap<String, Integer> customEnchantments,
      List<String> potionEffects,
      List<String> lore,
      EliteEntity eliteEntity,
      Player player,
      boolean showItemWorth,
      String customModelID,
      String equipmentModelID,
      boolean soulbound,
      String filename,
      String scriptedItem,
      SkillType weaponType,
      String fmmItemModel
   ) {
      return constructItem(
         level,
         rawName,
         material,
         enchantments,
         customEnchantments,
         potionEffects,
         lore,
         eliteEntity,
         player,
         showItemWorth,
         customModelID,
         equipmentModelID,
         soulbound,
         filename,
         scriptedItem,
         weaponType,
         fmmItemModel,
         null
      );
   }

   public static ItemStack constructItem(
      int level,
      String rawName,
      Material material,
      HashMap<Enchantment, Integer> enchantments,
      HashMap<String, Integer> customEnchantments,
      List<String> potionEffects,
      List<String> lore,
      EliteEntity eliteEntity,
      Player player,
      boolean showItemWorth,
      String customModelID,
      String equipmentModelID,
      boolean soulbound,
      String filename,
      String scriptedItem,
      SkillType weaponType,
      String fmmItemModel,
      ItemConstructionContext construction
   ) {
      boolean modelsEnabled = construction == null ? Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels") : construction.modelsEnabled();
      if (modelsEnabled
         || weaponType != SkillType.STAVES
            && weaponType != SkillType.WANDS
            && !customEnchantments.keySet().stream().anyMatch(id -> id.startsWith("freeminecraftmodels:"))) {
         if (weaponType == SkillType.STAVES || weaponType == SkillType.WANDS) {
            enchantments = new HashMap<>(enchantments);
            enchantments.remove(Enchantment.PUNCH);
         }

         ItemStack itemStack = ItemStackGenerator.generateItemStack(material);
         if (level != -1) {
            EliteItemManager.initializeEliteLevel(itemStack, level);
         }

         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(ChatColorConverter.convert(rawName));
         if (!enchantments.isEmpty()) {
            EnchantmentGenerator.generateEnchantments(itemMeta, enchantments);
         }

         itemStack.setItemMeta(itemMeta);
         if (lore != null && !lore.isEmpty()) {
            ItemTagger.registerCustomLore(itemMeta, lore);
         }

         new ElitePotionEffectContainer(itemMeta, potionEffects);
         itemStack.setItemMeta(itemMeta);
         if ((scriptedItem == null || scriptedItem.isEmpty()) && (fmmItemModel == null || fmmItemModel.isEmpty())) {
            if (customModelID != null && !customModelID.isEmpty() || equipmentModelID != null && !equipmentModelID.isEmpty()) {
               String effectiveCustomModelID = customModelID != null && !customModelID.isEmpty()
                  ? customModelID
                  : EliteItemSkins.getItemModelId(material, level);
               String effectiveEquipmentModelID = equipmentModelID != null && !equipmentModelID.isEmpty()
                  ? equipmentModelID
                  : EliteItemSkins.getEquipmentModelId(material, level);
               CustomModelAdder.addCustomModel(itemStack, effectiveCustomModelID);
               CustomModelAdder.addEquippableModel(itemStack, effectiveEquipmentModelID);
            } else {
               EliteItemSkins.applyLevelBasedSkin(itemStack, level);
            }
         }

         itemMeta = itemStack.getItemMeta();
         Objects.requireNonNull(itemMeta)
            .getPersistentDataContainer()
            .set(new NamespacedKey(MetadataHandler.PLUGIN, filename), PersistentDataType.STRING, filename);
         ItemTagger.registerCustomItemId(itemMeta, filename);
         itemStack.setItemMeta(itemMeta);
         if (fmmItemModel != null && !fmmItemModel.isEmpty() && modelsEnabled) {
            if (construction == null) {
               applyFmmItemData(itemStack, fmmItemModel, filename);
            } else {
               ItemData model = construction.model(fmmItemModel);
               if (model != null && (model.kind() != null || model.model() != null)) {
                  if (model.kind() != null) {
                     model.apply(itemStack);
                  } else {
                     new ItemData(null, model.model(), null).apply(itemStack);
                  }
               } else {
                  Logger.warn(
                     "FMM presentation model '" + fmmItemModel + "' was not found for custom item " + filename + "; its vanilla material will be used."
                  );
               }
            }
         }

         if (weaponType != null && (construction == null ? WeaponIdentityResolver.progressionSkill(itemStack) : construction.skill(itemStack)) != weaponType) {
            Logger.warn("Custom item " + filename + " requires an available FMM authored weapon matching " + weaponType + ".");
            return null;
         } else {
            if (scriptedItem != null && !scriptedItem.isEmpty() && modelsEnabled) {
               SkillType skill = construction == null ? WeaponIdentityResolver.progressionSkill(itemStack) : construction.skill(itemStack);
               if (skill == SkillType.WANDS || skill == SkillType.STAVES) {
                  Logger.warn("Custom item " + filename + " cannot combine an authored FMM weapon with the old scriptedItem path.");
                  return null;
               }

               try {
                  boolean applied;
                  if (construction == null) {
                     applied = ScriptedItemAPI.applyScriptedItemData(itemStack, scriptedItem);
                  } else {
                     ItemData data = construction.scriptedItem(scriptedItem);
                     applied = data != null && data.authoredId() != null;
                     if (applied) {
                        data.apply(itemStack);
                     }
                  }

                  if (!applied) {
                     Logger.warn("Scripted item '" + scriptedItem + "' not found in FreeMinecraftModels! Referenced by custom item: " + filename);
                  }
               } catch (NoClassDefFoundError var24) {
                  Logger.warn("Failed to apply scripted item data for '" + scriptedItem + "': " + var24.getMessage());
               }
            }

            SkillType skill = construction == null ? WeaponIdentityResolver.progressionSkill(itemStack) : construction.skill(itemStack);
            ItemDurability.prepareMagicWeapon(itemStack, skill == SkillType.WANDS || skill == SkillType.STAVES);
            return commonFeatures(itemStack, eliteEntity, player, enchantments, customEnchantments, showItemWorth, soulbound, construction);
         }
      } else {
         return null;
      }
   }

   private static void applyFmmItemData(ItemStack itemStack, String fmmItemModel, String filename) {
      if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         boolean magicWeapon = false;
         boolean applied = false;

         try {
            magicWeapon = MagicWeaponAPI.isWeapon(fmmItemModel);
            applied = magicWeapon ? MagicWeaponAPI.applyWeaponData(itemStack, fmmItemModel) : ModelItemAPI.applyDisplayModel(itemStack, fmmItemModel);
         } catch (LinkageError var6) {
         }

         if (!applied && !magicWeapon) {
            Logger.warn("FMM presentation model '" + fmmItemModel + "' was not found for custom item " + filename + "; its vanilla material will be used.");
         }
      }
   }

   public static ItemStack constructItemWithMaterial(Material material, int level, Player player, boolean showItemWorth) {
      return material == null ? null : constructProceduralItem(ProceduralItemType.vanilla(material), level, null, player, showItemWorth);
   }

   public static ItemStack constructItem(double itemTier, EliteEntity killedMob, Player player, boolean showItemWorth) {
      return constructProceduralItem(MaterialGenerator.generateItemType(itemTier), itemTier, killedMob, player, showItemWorth);
   }

   public static ItemStack constructProceduralItem(ProceduralItemType type, double itemTier, EliteEntity killedMob, Player player, boolean showItemWorth) {
      if (type != null && type.isAvailable()) {
         int level = (int)Math.round(itemTier);
         ItemStack itemStack = ItemStackGenerator.generateItemStack(type.material());
         EliteItemManager.initializeEliteLevel(itemStack, level);
         ItemMeta itemMeta = itemStack.getItemMeta();
         HashMap<Enchantment, Integer> enchantmentMap = EnchantmentGenerator.generateEnchantments(itemTier, type.material(), type.magicSkill(), itemMeta);
         HashMap<String, Integer> customEnchantmentMap = type.magicSkill() == null
            ? EnchantmentGenerator.generateCustomEnchantments(itemTier, type.material())
            : MagicEnchantmentGeneration.generate(itemTier, type.magicSkill());
         itemMeta.setDisplayName(NameGenerator.generateName(type));
         itemStack.setItemMeta(itemMeta);
         if (type.magicSkill() == null) {
            EliteItemSkins.applyLevelBasedSkin(itemStack, level);
         } else if (!type.applyMagicData(itemStack)) {
            return null;
         }

         ItemQualityColorizer.dropQualityColorizer(itemStack);
         return commonFeatures(itemStack, killedMob, player, enchantmentMap, customEnchantmentMap, showItemWorth, true);
      } else {
         return null;
      }
   }

   private static ItemStack commonFeatures(
      ItemStack itemStack,
      EliteEntity eliteEntity,
      Player player,
      HashMap<Enchantment, Integer> enchantments,
      HashMap<String, Integer> customEnchantments,
      boolean showItemWorth,
      boolean soulbound
   ) {
      return commonFeatures(itemStack, eliteEntity, player, enchantments, customEnchantments, showItemWorth, soulbound, null);
   }

   private static ItemStack commonFeatures(
      ItemStack itemStack,
      EliteEntity eliteEntity,
      Player player,
      HashMap<Enchantment, Integer> enchantments,
      HashMap<String, Integer> customEnchantments,
      boolean showItemWorth,
      boolean soulbound,
      ItemConstructionContext construction
   ) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (ItemSettingsConfig.isHideEnchants()) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      ItemTagger.registerEliteItem(itemMeta);
      if (LootItemPolicy.keepsMobProvenance(itemStack, construction)) {
         ItemTagger.registerItemSource(eliteEntity, itemMeta);
      }

      ItemTagger.registerEnchantments(itemMeta, enchantments);
      itemStack.setItemMeta(itemMeta);
      if (!customEnchantments.isEmpty()) {
         itemStack = construction == null
            ? EliteEnchantmentItems.ITEMS.previewAuthoredCustom(itemStack, customEnchantments).apply(itemStack)
            : construction.enchantments().applyCustom(itemStack, customEnchantments);
      }

      if (soulbound) {
         SoulbindEnchantment.addEnchantment(itemStack, player);
      }

      new EliteItemLore(itemStack, showItemWorth, false, construction);
      return itemStack;
   }
}
