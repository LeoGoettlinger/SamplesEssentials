package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.items.ClassLootFamily;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class CustomItemsConfigFields extends CustomConfigFields {
   private Material material = Material.WOODEN_SWORD;
   private String name = "Default name";
   private List<String> lore = new ArrayList<>();
   private List<String> enchantments = new ArrayList<>();
   private List<String> potionEffects = new ArrayList<>();
   private String dropWeight = "dynamic";
   private CustomItem.Scalability scalability = CustomItem.Scalability.SCALABLE;
   private CustomItem.ItemType itemType = CustomItem.ItemType.CUSTOM;
   private ClassLootFamily classLootFamily;
   private String customModelID = null;
   private String equipmentModelID = null;
   private String scriptedItem = null;
   private SkillType weaponType = null;
   private String fmmItemModel = null;
   private boolean proceduralEnchantments = false;
   private String permission = "";
   private int level = 0;
   private boolean soulbound = true;
   private boolean showSource = true;
   private ItemConsumables.Definition consumable;

   public CustomItemsConfigFields(String fileName, boolean isEnabled, Material material, String name, List<String> lore) {
      super(fileName, isEnabled);
      this.material = material;
      this.name = name;
      this.lore = lore;
   }

   public CustomItemsConfigFields(String fileName, boolean isEnabled) {
      super(fileName, isEnabled);
   }

   @Override
   public void processConfigFields() {
      Object authoredMaterial = this.fileConfiguration.get("material");
      Object authoredName = this.fileConfiguration.get("name");
      Object authoredLore = this.fileConfiguration.get("lore");
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      if (this.configHas("material")) {
         String rawMaterial = this.fileConfiguration.getString("material");
         if (rawMaterial != null && rawMaterial.toUpperCase(Locale.ROOT).endsWith("_SPEAR") && VersionChecker.serverVersionOlderThan(21, 11)) {
            this.material = Material.WOODEN_SWORD;
            this.isEnabled = false;
         } else {
            this.material = this.processEnum("material", this.material, Material.WOODEN_SWORD, Material.class, true);
         }
      } else {
         this.material = this.processEnum("material", this.material, Material.WOODEN_SWORD, Material.class, true);
      }

      this.name = this.translatable(this.filename, "name", this.processString("name", this.name, "Default name", true));
      this.lore = this.translatable(this.filename, "lore", this.processStringList("lore", this.lore, new ArrayList<>(), true));
      this.enchantments = this.processStringList("enchantments", this.enchantments, null, false);
      this.potionEffects = this.processStringList("potionEffects", this.potionEffects, null, false);
      this.dropWeight = this.processString("dropWeight", this.dropWeight, "dynamic", false);
      this.scalability = this.processEnum("scalability", this.scalability, CustomItem.Scalability.SCALABLE, CustomItem.Scalability.class, false);
      this.itemType = this.processEnum("itemType", this.itemType, CustomItem.ItemType.CUSTOM, CustomItem.ItemType.class, false);
      this.customModelID = this.processString("customModelV2", this.customModelID, null, false);
      this.equipmentModelID = this.processString("equipmentModelID", this.equipmentModelID, null, false);
      this.scriptedItem = this.processString("scriptedItem", this.scriptedItem, null, false);
      this.weaponType = this.processEnum("weaponType", this.weaponType, null, SkillType.class, false);
      if (this.weaponType != null && this.weaponType != SkillType.STAVES && this.weaponType != SkillType.WANDS) {
         Logger.warn("Item " + this.filename + ": weaponType accepts STAVES or WANDS; other weapons use their material.");
         this.weaponType = null;
      }

      this.fmmItemModel = this.processString("fmmItemModel", this.fmmItemModel, null, false);
      this.proceduralEnchantments = this.processBoolean("proceduralEnchantments", this.proceduralEnchantments, false, false);
      this.permission = this.processString("permission", this.permission, "", false);
      this.level = this.processInt("level", this.level, 0, false);
      this.soulbound = this.processBoolean("soulbound", this.soulbound, true, false);
      this.showSource = this.processBoolean("showSource", this.showSource, this.showSource, false);
      this.classLootFamily = null;
      if (this.itemType == CustomItem.ItemType.CLASS_LOOT) {
         try {
            if (!(authoredMaterial instanceof String && authoredName instanceof String text)
               || !(!text.isBlank() && authoredLore instanceof List<?> lines)
               || !lines.stream().allMatch(String.class::isInstance)) {
               throw new IllegalArgumentException("material, name and lore must be valid item fields (lore: [] is allowed)");
            }

            if (Material.getMaterial(this.fileConfiguration.getString("material").toUpperCase(Locale.ROOT)) == null) {
               throw new IllegalArgumentException("material is unavailable on this server");
            }

            this.classLootFamily = ClassLootFamily.resolve(this.material, this.weaponType, this.fileConfiguration.getString("classLootFamily"));
         } catch (IllegalArgumentException var6) {
            this.isEnabled = false;
            Logger.warn("Invalid CLASS_LOOT item " + this.filename + ": " + var6.getMessage() + "; item disabled.");
         }
      }

      this.processConsumable();
      this.updatePostProcessor();
   }

   private void processConsumable() {
      try {
         if (this.configHas("consumable") && !this.fileConfiguration.isConfigurationSection("consumable")) {
            throw new IllegalArgumentException("consumable must contain type and optional repair tier");
         }

         ConfigurationSection section = this.fileConfiguration.getConfigurationSection("consumable");
         if (section != null && !Set.of("type", "tier").containsAll(section.getKeys(false))) {
            throw new IllegalArgumentException("Unknown consumable field");
         }

         String type = this.processString(
            "consumable.type", this.consumable == null ? null : this.consumable.type().name().toLowerCase(Locale.ROOT), null, false
         );
         if (type == null) {
            if (section != null) {
               throw new IllegalArgumentException("consumable.type is required");
            }

            this.consumable = null;
            return;
         }

         if (this.material == null || this.material.isAir() || !this.material.isItem()) {
            throw new IllegalArgumentException("Consumables require a valid item material");
         }

         ItemConsumables.Type parsed = ItemConsumables.Type.valueOf(type.toUpperCase(Locale.ROOT));
         if (this.configHas("consumable.tier") && !(this.fileConfiguration.get("consumable.tier") instanceof Integer)) {
            throw new IllegalArgumentException("consumable.tier must be an integer");
         }

         if (parsed != ItemConsumables.Type.REPAIR_SCRAP && this.configHas("consumable.tier")) {
            throw new IllegalArgumentException("Only repair_scrap accepts consumable.tier");
         }

         int tier = parsed == ItemConsumables.Type.REPAIR_SCRAP
            ? this.processInt("consumable.tier", this.consumable == null ? 0 : this.consumable.tier(), 0, false)
            : 0;
         this.consumable = new ItemConsumables.Definition(parsed, tier);
      } catch (IllegalArgumentException var5) {
         this.consumable = null;
         this.isEnabled = false;
         Logger.warn("Item " + this.filename + " disabled: " + var5.getMessage());
      }
   }

   private void updatePostProcessor() {
      List<String> newEnchantments = new ArrayList<>();

      for (String enchantment : this.enchantments) {
         if (!enchantment.contains(",")) {
            if (enchantment.contains(":") && !enchantment.startsWith("minecraft:")) {
               this.isEnabled = false;
            }

            Logger.warn(
               "Invalid format for enchantment in file "
                  + this.filename
                  + " for enchantment "
                  + enchantment
                  + " : missing ',' for valid level after the enchantment name"
            );
         } else {
            String[] split = enchantment.split(",");
            String result = LegacyValueConverter.parseEnchantment(split[0]);
            if (result.equals(split[0])) {
               newEnchantments.add(enchantment);
            } else {
               newEnchantments.add(result + "," + split[1]);
            }
         }
      }

      this.enchantments = newEnchantments;
      List<String> newPotionEffects = new ArrayList<>();

      for (String potionEffect : this.potionEffects) {
         if (!potionEffect.contains(",")) {
            Logger.warn(
               "Invalid format for potion effect in file "
                  + this.filename
                  + " for potion effect "
                  + potionEffect
                  + " : missing ',' for valid level after the potion effect name"
            );
         } else {
            String[] split = potionEffect.split(",");
            String result = LegacyValueConverter.parsePotionEffect(split[0]);
            if (result.equals(split[0])) {
               newPotionEffects.add(potionEffect);
            } else {
               StringBuilder newString = new StringBuilder();
               newString.append(result + ",");

               for (int i = 1; i < split.length; i++) {
                  newString.append(split[i]);
                  if (i != split.length - 1) {
                     newString.append(",");
                  }
               }

               newPotionEffects.add(newString.toString());
            }
         }
      }

      this.potionEffects = newPotionEffects;
      this.potionEffects
         .forEach(
            potionEffectx -> {
               if (potionEffectx.contains("INSTANT_DAMAGE")) {
                  Logger.warn(
                     "Item "
                        + this.filename
                        + " contains HARM/INSTANT_DAMAGE potion effect, which heals undead mobs (Minecraft vanilla mechanic) and often times confuses players and admins. It is recommended you switch this potion effect with something else, like STRENGTH if you want more damage."
                  );
               }
            }
         );
   }

   @Generated
   public Material getMaterial() {
      return this.material;
   }

   @Generated
   public void setMaterial(Material material) {
      this.material = material;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public List<String> getLore() {
      return this.lore;
   }

   @Generated
   public void setLore(List<String> lore) {
      this.lore = lore;
   }

   @Generated
   public List<String> getEnchantments() {
      return this.enchantments;
   }

   @Generated
   public void setEnchantments(List<String> enchantments) {
      this.enchantments = enchantments;
   }

   @Generated
   public List<String> getPotionEffects() {
      return this.potionEffects;
   }

   @Generated
   public void setPotionEffects(List<String> potionEffects) {
      this.potionEffects = potionEffects;
   }

   @Generated
   public String getDropWeight() {
      return this.dropWeight;
   }

   @Generated
   public void setDropWeight(String dropWeight) {
      this.dropWeight = dropWeight;
   }

   @Generated
   public CustomItem.Scalability getScalability() {
      return this.scalability;
   }

   @Generated
   public void setScalability(CustomItem.Scalability scalability) {
      this.scalability = scalability;
   }

   @Generated
   public CustomItem.ItemType getItemType() {
      return this.itemType;
   }

   @Generated
   public void setItemType(CustomItem.ItemType itemType) {
      this.itemType = itemType;
   }

   @Generated
   public ClassLootFamily getClassLootFamily() {
      return this.classLootFamily;
   }

   @Generated
   public String getCustomModelID() {
      return this.customModelID;
   }

   @Generated
   public void setCustomModelID(String customModelID) {
      this.customModelID = customModelID;
   }

   @Generated
   public String getEquipmentModelID() {
      return this.equipmentModelID;
   }

   @Generated
   public void setEquipmentModelID(String equipmentModelID) {
      this.equipmentModelID = equipmentModelID;
   }

   @Generated
   public String getScriptedItem() {
      return this.scriptedItem;
   }

   @Generated
   public void setScriptedItem(String scriptedItem) {
      this.scriptedItem = scriptedItem;
   }

   @Generated
   public SkillType getWeaponType() {
      return this.weaponType;
   }

   @Generated
   public void setWeaponType(SkillType weaponType) {
      this.weaponType = weaponType;
   }

   @Generated
   public String getFmmItemModel() {
      return this.fmmItemModel;
   }

   @Generated
   public void setFmmItemModel(String fmmItemModel) {
      this.fmmItemModel = fmmItemModel;
   }

   @Generated
   public boolean isProceduralEnchantments() {
      return this.proceduralEnchantments;
   }

   @Generated
   public void setProceduralEnchantments(boolean proceduralEnchantments) {
      this.proceduralEnchantments = proceduralEnchantments;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public void setPermission(String permission) {
      this.permission = permission;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }

   @Generated
   public void setLevel(int level) {
      this.level = level;
   }

   @Generated
   public boolean isSoulbound() {
      return this.soulbound;
   }

   @Generated
   public void setSoulbound(boolean soulbound) {
      this.soulbound = soulbound;
   }

   @Generated
   public boolean isShowSource() {
      return this.showSource;
   }

   @Generated
   public void setShowSource(boolean showSource) {
      this.showSource = showSource;
   }

   @Generated
   public ItemConsumables.Definition getConsumable() {
      return this.consumable;
   }

   @Generated
   public void setConsumable(ItemConsumables.Definition consumable) {
      this.consumable = consumable;
   }
}
