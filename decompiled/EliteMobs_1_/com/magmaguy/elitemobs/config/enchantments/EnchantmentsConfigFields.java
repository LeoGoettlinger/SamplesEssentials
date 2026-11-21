package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import java.util.Locale;
import lombok.Generated;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentsConfigFields extends CustomConfigFields {
   private String name = "name";
   private int maxLevel = 5;
   private double value = 1.0;
   private Enchantment enchantment = null;
   private boolean isEnabledForProcedurallyGeneratedItems = false;
   private int maxEnchantmentLevel;

   public EnchantmentsConfigFields(
      String filename, boolean isEnabled, String name, int maxLevel, double value, boolean isEnabledForProcedurallyGeneratedItems, int maxEnchantmentLevel
   ) {
      super(filename, isEnabled);
      this.isEnabled = isEnabled;
      this.name = name;
      this.maxLevel = maxLevel;
      this.value = value;
      this.isEnabledForProcedurallyGeneratedItems = isEnabledForProcedurallyGeneratedItems;
      this.maxEnchantmentLevel = maxEnchantmentLevel;
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.name = this.translatable(this.filename, "name", this.processString("name", this.name, "name", true));
      this.maxLevel = this.processInt("maxLevel", this.maxLevel, 1, true);
      String cleanName = this.filename.replace(".yml", "").toUpperCase(Locale.ROOT);

      try {
         this.enchantment = Enchantment.getByName(cleanName);
      } catch (Exception var3) {
         this.enchantment = null;
      }

      this.value = this.processDouble("value", this.value, 1.0, true);
      this.isEnabledForProcedurallyGeneratedItems = this.processBoolean(
         "isEnabledForProcedurallyGeneratedItems", this.isEnabledForProcedurallyGeneratedItems, false, true
      );
      this.maxEnchantmentLevel = this.processInt("maxEnchantmentLevel", this.maxEnchantmentLevel, 1, false);
      this.processAdditionalFields();
   }

   public void processAdditionalFields() {
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getMaxLevel() {
      return this.maxLevel;
   }

   @Generated
   public double getValue() {
      return this.value;
   }

   @Generated
   public Enchantment getEnchantment() {
      return this.enchantment;
   }

   @Generated
   public boolean isEnabledForProcedurallyGeneratedItems() {
      return this.isEnabledForProcedurallyGeneratedItems;
   }

   @Generated
   public int getMaxEnchantmentLevel() {
      return this.maxEnchantmentLevel;
   }
}
