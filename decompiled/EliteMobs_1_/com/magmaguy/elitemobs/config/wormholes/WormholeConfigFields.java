package com.magmaguy.elitemobs.config.wormholes;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import lombok.Generated;
import org.bukkit.Location;

public class WormholeConfigFields extends CustomConfigFields {
   private String location1;
   private String location2;
   private String location1Text;
   private String location2Text;
   private String permission;
   private Wormhole.WormholeStyle style = Wormhole.WormholeStyle.CUBE;
   private int particleColor;
   private double coinCost;
   private boolean blindPlayer;
   private double sizeMultiplier = 1.0;
   private String customModel1 = null;
   private String customModel2 = null;

   public WormholeConfigFields(String filename, boolean isEnabled, String location1, String location2, Wormhole.WormholeStyle style) {
      super(filename, isEnabled);
      this.location1 = location1;
      this.location2 = location2;
      this.style = style;
   }

   public WormholeConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.location1 = this.processString("location1", this.location1, null, true);
      this.location1Text = this.translatable(this.filename, "location1Text", this.processString("location1Text", this.location1Text, null, false));
      this.location2 = this.processString("location2", this.location2, null, true);
      this.location2Text = this.translatable(this.filename, "location2Text", this.processString("location2Text", this.location2Text, null, false));
      this.permission = this.processString("permission", this.permission, null, false);
      this.coinCost = this.processDouble("coinCost", this.coinCost, 0.0, false);
      this.style = this.processEnum("style", this.style, Wormhole.WormholeStyle.CUBE, Wormhole.WormholeStyle.class, false);
      this.particleColor = this.processInt("particleColor", this.particleColor, 8388736, false);
      this.blindPlayer = this.processBoolean("blindPlayer", this.blindPlayer, false, false);
      this.sizeMultiplier = this.processDouble("sizeMultiplier", this.sizeMultiplier, 1.0, false);
      this.customModel1 = this.processString("customModel1", this.customModel1, null, false);
      this.customModel2 = this.processString("customModel2", this.customModel2, null, false);
   }

   public void setWormholeEntryLocation(Location location, int wormholeNumber) {
      if (wormholeNumber == 1) {
         this.location1 = ConfigurationLocation.deserialize(location);
         this.fileConfiguration.set("location1", this.location1);
         ConfigurationEngine.fileSaverCustomValues(this.fileConfiguration, this.file);
      } else {
         this.location2 = ConfigurationLocation.deserialize(location);
         this.fileConfiguration.set("location2", this.location2);
         ConfigurationEngine.fileSaverCustomValues(this.fileConfiguration, this.file);
      }
   }

   @Generated
   public String getLocation1() {
      return this.location1;
   }

   @Generated
   public void setLocation1(String location1) {
      this.location1 = location1;
   }

   @Generated
   public String getLocation2() {
      return this.location2;
   }

   @Generated
   public void setLocation2(String location2) {
      this.location2 = location2;
   }

   @Generated
   public String getLocation1Text() {
      return this.location1Text;
   }

   @Generated
   public void setLocation1Text(String location1Text) {
      this.location1Text = location1Text;
   }

   @Generated
   public String getLocation2Text() {
      return this.location2Text;
   }

   @Generated
   public void setLocation2Text(String location2Text) {
      this.location2Text = location2Text;
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
   public Wormhole.WormholeStyle getStyle() {
      return this.style;
   }

   @Generated
   public void setStyle(Wormhole.WormholeStyle style) {
      this.style = style;
   }

   @Generated
   public int getParticleColor() {
      return this.particleColor;
   }

   @Generated
   public void setParticleColor(int particleColor) {
      this.particleColor = particleColor;
   }

   @Generated
   public double getCoinCost() {
      return this.coinCost;
   }

   @Generated
   public void setCoinCost(double coinCost) {
      this.coinCost = coinCost;
   }

   @Generated
   public boolean isBlindPlayer() {
      return this.blindPlayer;
   }

   @Generated
   public void setBlindPlayer(boolean blindPlayer) {
      this.blindPlayer = blindPlayer;
   }

   @Generated
   public double getSizeMultiplier() {
      return this.sizeMultiplier;
   }

   @Generated
   public void setSizeMultiplier(double sizeMultiplier) {
      this.sizeMultiplier = sizeMultiplier;
   }

   @Generated
   public String getCustomModel1() {
      return this.customModel1;
   }

   @Generated
   public void setCustomModel1(String customModel1) {
      this.customModel1 = customModel1;
   }

   @Generated
   public String getCustomModel2() {
      return this.customModel2;
   }

   @Generated
   public void setCustomModel2(String customModel2) {
      this.customModel2 = customModel2;
   }
}
