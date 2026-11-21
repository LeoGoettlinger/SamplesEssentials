package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import lombok.Generated;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;

public class FakeItemSettings {
   private ItemStack itemStack;
   private Billboard billboard = Billboard.FIXED;
   private float scale = 1.0F;
   private float viewRange = 1.0F;
   private boolean glowing = false;
   private String customName = null;
   private boolean customNameVisible = false;

   public FakeItemSettings() {
   }

   public FakeItemSettings(FakeItemSettings other) {
      this.itemStack = other.itemStack != null ? other.itemStack.clone() : null;
      this.billboard = other.billboard;
      this.scale = other.scale;
      this.viewRange = other.viewRange;
      this.glowing = other.glowing;
      this.customName = other.customName;
      this.customNameVisible = other.customNameVisible;
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   @Generated
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @Generated
   public Billboard getBillboard() {
      return this.billboard;
   }

   @Generated
   public float getScale() {
      return this.scale;
   }

   @Generated
   public float getViewRange() {
      return this.viewRange;
   }

   @Generated
   public boolean isGlowing() {
      return this.glowing;
   }

   @Generated
   public String getCustomName() {
      return this.customName;
   }

   @Generated
   public boolean isCustomNameVisible() {
      return this.customNameVisible;
   }

   @Generated
   public void setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   @Generated
   public void setBillboard(Billboard billboard) {
      this.billboard = billboard;
   }

   @Generated
   public void setScale(float scale) {
      this.scale = scale;
   }

   @Generated
   public void setViewRange(float viewRange) {
      this.viewRange = viewRange;
   }

   @Generated
   public void setGlowing(boolean glowing) {
      this.glowing = glowing;
   }

   @Generated
   public void setCustomName(String customName) {
      this.customName = customName;
   }

   @Generated
   public void setCustomNameVisible(boolean customNameVisible) {
      this.customNameVisible = customNameVisible;
   }
}
