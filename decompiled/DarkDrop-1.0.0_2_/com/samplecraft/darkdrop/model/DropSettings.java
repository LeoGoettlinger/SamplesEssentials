package com.samplecraft.darkdrop.model;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class DropSettings {
   private double goodChance = 50.0;
   private double badChance = 50.0;
   private int goodAmount = 1;
   private int badAmount = 1;
   private BadLootType badLootType = BadLootType.LIGHTNING;
   private List<ItemStack> goodLootItems = new ArrayList<>();
   private List<String> goodLootCommands = new ArrayList<>();
   private boolean useCommandsAsGoodLoot = false;

   public DropSettings copy() {
      DropSettings copy = new DropSettings();
      copy.goodChance = this.goodChance;
      copy.badChance = this.badChance;
      copy.goodAmount = this.goodAmount;
      copy.badAmount = this.badAmount;
      copy.badLootType = this.badLootType;
      copy.goodLootItems = new ArrayList<>();

      for (ItemStack item : this.goodLootItems) {
         if (item != null && !item.getType().isAir()) {
            copy.goodLootItems.add(item.clone());
         }
      }

      copy.goodLootCommands = new ArrayList<>(this.goodLootCommands);
      copy.useCommandsAsGoodLoot = this.useCommandsAsGoodLoot;
      return copy;
   }

   public double normalizedGoodRatio() {
      double sum = this.goodChance + this.badChance;
      return sum <= 0.0 ? 0.5 : this.goodChance / sum;
   }

   public double normalizedBadRatio() {
      return 1.0 - this.normalizedGoodRatio();
   }

   public double normalizedGoodPercent() {
      return this.normalizedGoodRatio() * 100.0;
   }

   public double normalizedBadPercent() {
      return this.normalizedBadRatio() * 100.0;
   }

   public double getGoodChance() {
      return this.goodChance;
   }

   public void setGoodChance(double goodChance) {
      this.goodChance = Math.max(0.0, goodChance);
   }

   public double getBadChance() {
      return this.badChance;
   }

   public void setBadChance(double badChance) {
      this.badChance = Math.max(0.0, badChance);
   }

   public int getGoodAmount() {
      return this.goodAmount;
   }

   public void setGoodAmount(int goodAmount) {
      this.goodAmount = Math.max(1, Math.min(64, goodAmount));
   }

   public int getBadAmount() {
      return this.badAmount;
   }

   public void setBadAmount(int badAmount) {
      this.badAmount = Math.max(1, Math.min(64, badAmount));
   }

   public BadLootType getBadLootType() {
      return this.badLootType;
   }

   public void setBadLootType(BadLootType badLootType) {
      this.badLootType = badLootType;
   }

   public List<ItemStack> getGoodLootItems() {
      return this.goodLootItems;
   }

   public void setGoodLootItems(List<ItemStack> goodLootItems) {
      this.goodLootItems = (List<ItemStack>)(goodLootItems != null ? goodLootItems : new ArrayList<>());
   }

   public List<String> getGoodLootCommands() {
      return this.goodLootCommands;
   }

   public void setGoodLootCommands(List<String> goodLootCommands) {
      this.goodLootCommands = (List<String>)(goodLootCommands != null ? goodLootCommands : new ArrayList<>());
   }

   public boolean isUseCommandsAsGoodLoot() {
      return this.useCommandsAsGoodLoot;
   }

   public void setUseCommandsAsGoodLoot(boolean useCommandsAsGoodLoot) {
      this.useCommandsAsGoodLoot = useCommandsAsGoodLoot;
   }
}
