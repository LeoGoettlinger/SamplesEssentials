package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import javax.annotation.Nullable;

class SimpleBlockMaterial implements BlockMaterial {
   private boolean isAir;
   private boolean fullCube;
   private boolean opaque;
   private boolean powerSource;
   private boolean liquid;
   private boolean solid;
   private float hardness;
   private float resistance;
   private float slipperiness;
   private int lightValue;
   private boolean fragileWhenPushed;
   private boolean unpushable;
   private boolean ticksRandomly;
   private boolean movementBlocker;
   private boolean burnable;
   private boolean toolRequired;
   private boolean replacedDuringPlacement;
   private boolean isTranslucent;
   private boolean hasContainer;
   private int lightOpacity;
   private int mapColor;
   private boolean isTile;
   private CompoundTag tile = null;

   @Override
   public boolean isAir() {
      return this.isAir;
   }

   @Override
   public int getMapColor() {
      return this.mapColor;
   }

   public void setMapColor(int mapColor) {
      this.mapColor = mapColor;
   }

   @Override
   public int getLightOpacity() {
      return this.lightOpacity;
   }

   public void setLightOpacity(int lightOpacity) {
      this.lightOpacity = lightOpacity;
   }

   public void setIsAir(boolean isAir) {
      this.isAir = isAir;
   }

   @Override
   public boolean isFullCube() {
      return this.fullCube;
   }

   public void setFullCube(boolean fullCube) {
      this.fullCube = fullCube;
   }

   @Override
   public boolean isOpaque() {
      return this.opaque;
   }

   public void setOpaque(boolean opaque) {
      this.opaque = opaque;
   }

   @Override
   public boolean isPowerSource() {
      return this.powerSource;
   }

   public void setPowerSource(boolean powerSource) {
      this.powerSource = powerSource;
   }

   @Override
   public boolean isLiquid() {
      return this.liquid;
   }

   public void setLiquid(boolean liquid) {
      this.liquid = liquid;
   }

   @Override
   public boolean isSolid() {
      return this.solid;
   }

   public void setSolid(boolean solid) {
      this.solid = solid;
   }

   @Override
   public float getHardness() {
      return this.hardness;
   }

   public void setHardness(float hardness) {
      this.hardness = hardness;
   }

   @Override
   public float getResistance() {
      return this.resistance;
   }

   public void setResistance(float resistance) {
      this.resistance = resistance;
   }

   @Override
   public float getSlipperiness() {
      return this.slipperiness;
   }

   public void setSlipperiness(float slipperiness) {
      this.slipperiness = slipperiness;
   }

   @Override
   public int getLightValue() {
      return this.lightValue;
   }

   public void setLightValue(int lightValue) {
      this.lightValue = lightValue;
   }

   @Override
   public boolean isFragileWhenPushed() {
      return this.fragileWhenPushed;
   }

   public void setFragileWhenPushed(boolean fragileWhenPushed) {
      this.fragileWhenPushed = fragileWhenPushed;
   }

   @Override
   public boolean isUnpushable() {
      return this.unpushable;
   }

   public void setUnpushable(boolean unpushable) {
      this.unpushable = unpushable;
   }

   @Override
   public boolean isTicksRandomly() {
      return this.ticksRandomly;
   }

   public void setTicksRandomly(boolean ticksRandomly) {
      this.ticksRandomly = ticksRandomly;
   }

   @Override
   public boolean isMovementBlocker() {
      return this.movementBlocker;
   }

   public void setMovementBlocker(boolean movementBlocker) {
      this.movementBlocker = movementBlocker;
   }

   @Override
   public boolean isBurnable() {
      return this.burnable;
   }

   public void setBurnable(boolean burnable) {
      this.burnable = burnable;
   }

   @Override
   public boolean isToolRequired() {
      return this.toolRequired;
   }

   public void setToolRequired(boolean toolRequired) {
      this.toolRequired = toolRequired;
   }

   @Override
   public boolean isReplacedDuringPlacement() {
      return this.replacedDuringPlacement;
   }

   public void setTranslucent(boolean isTranslucent) {
      this.isTranslucent = isTranslucent;
   }

   @Override
   public boolean isTranslucent() {
      return this.isTranslucent;
   }

   public void setReplacedDuringPlacement(boolean replacedDuringPlacement) {
      this.replacedDuringPlacement = replacedDuringPlacement;
   }

   @Override
   public boolean hasContainer() {
      return this.hasContainer;
   }

   public void setIsTile(boolean isTile) {
      this.isTile = isTile;
   }

   @Override
   public boolean isTile() {
      return this.isTile;
   }

   public void setDefaultTile(CompoundTag tile) {
      this.tile = tile;
   }

   @Nullable
   @Override
   public FaweCompoundTag defaultTile() {
      return this.tile != null ? FaweCompoundTag.of(this.tile.toLinTag()) : null;
   }

   public void setHasContainer(boolean hasContainer) {
      this.hasContainer = hasContainer;
   }
}
