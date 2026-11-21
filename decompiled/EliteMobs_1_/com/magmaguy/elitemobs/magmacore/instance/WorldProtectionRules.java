package com.magmaguy.elitemobs.magmacore.instance;

import lombok.Generated;

public class WorldProtectionRules {
   private boolean allowExplosions = false;
   private boolean allowLiquidFlow = false;
   private boolean allowElytra = false;
   private boolean preventFlyToggle = false;
   private boolean preventFriendlyFire = false;
   private boolean preventVanillaMobSpawning = true;
   private boolean preventRedstoneInteraction = false;
   private double fireDamageMultiplier = 1.0;
   private double poisonDamageMultiplier = 1.0;
   private double witherDamageMultiplier = 1.0;

   public static WorldProtectionRules strict() {
      return new WorldProtectionRules();
   }

   @Generated
   public boolean isAllowExplosions() {
      return this.allowExplosions;
   }

   @Generated
   public boolean isAllowLiquidFlow() {
      return this.allowLiquidFlow;
   }

   @Generated
   public boolean isAllowElytra() {
      return this.allowElytra;
   }

   @Generated
   public boolean isPreventFlyToggle() {
      return this.preventFlyToggle;
   }

   @Generated
   public boolean isPreventFriendlyFire() {
      return this.preventFriendlyFire;
   }

   @Generated
   public boolean isPreventVanillaMobSpawning() {
      return this.preventVanillaMobSpawning;
   }

   @Generated
   public boolean isPreventRedstoneInteraction() {
      return this.preventRedstoneInteraction;
   }

   @Generated
   public double getFireDamageMultiplier() {
      return this.fireDamageMultiplier;
   }

   @Generated
   public double getPoisonDamageMultiplier() {
      return this.poisonDamageMultiplier;
   }

   @Generated
   public double getWitherDamageMultiplier() {
      return this.witherDamageMultiplier;
   }

   @Generated
   public WorldProtectionRules setAllowExplosions(boolean allowExplosions) {
      this.allowExplosions = allowExplosions;
      return this;
   }

   @Generated
   public WorldProtectionRules setAllowLiquidFlow(boolean allowLiquidFlow) {
      this.allowLiquidFlow = allowLiquidFlow;
      return this;
   }

   @Generated
   public WorldProtectionRules setAllowElytra(boolean allowElytra) {
      this.allowElytra = allowElytra;
      return this;
   }

   @Generated
   public WorldProtectionRules setPreventFlyToggle(boolean preventFlyToggle) {
      this.preventFlyToggle = preventFlyToggle;
      return this;
   }

   @Generated
   public WorldProtectionRules setPreventFriendlyFire(boolean preventFriendlyFire) {
      this.preventFriendlyFire = preventFriendlyFire;
      return this;
   }

   @Generated
   public WorldProtectionRules setPreventVanillaMobSpawning(boolean preventVanillaMobSpawning) {
      this.preventVanillaMobSpawning = preventVanillaMobSpawning;
      return this;
   }

   @Generated
   public WorldProtectionRules setPreventRedstoneInteraction(boolean preventRedstoneInteraction) {
      this.preventRedstoneInteraction = preventRedstoneInteraction;
      return this;
   }

   @Generated
   public WorldProtectionRules setFireDamageMultiplier(double fireDamageMultiplier) {
      this.fireDamageMultiplier = fireDamageMultiplier;
      return this;
   }

   @Generated
   public WorldProtectionRules setPoisonDamageMultiplier(double poisonDamageMultiplier) {
      this.poisonDamageMultiplier = poisonDamageMultiplier;
      return this;
   }

   @Generated
   public WorldProtectionRules setWitherDamageMultiplier(double witherDamageMultiplier) {
      this.witherDamageMultiplier = witherDamageMultiplier;
      return this;
   }
}
