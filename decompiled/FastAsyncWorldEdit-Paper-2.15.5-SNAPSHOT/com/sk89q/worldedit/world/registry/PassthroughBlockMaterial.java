package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.util.GuavaUtil;
import javax.annotation.Nullable;

public class PassthroughBlockMaterial implements BlockMaterial {
   private static final SimpleBlockMaterial DEFAULT_MATERIAL = new SimpleBlockMaterial();
   private final BlockMaterial blockMaterial;

   public PassthroughBlockMaterial(@Nullable BlockMaterial material) {
      this.blockMaterial = GuavaUtil.firstNonNull(material, DEFAULT_MATERIAL);
   }

   @Override
   public boolean isAir() {
      return this.blockMaterial.isAir();
   }

   @Override
   public int getMapColor() {
      return this.blockMaterial == null ? 0 : this.blockMaterial.getMapColor();
   }

   @Override
   public boolean isFullCube() {
      return this.blockMaterial.isFullCube();
   }

   @Override
   public boolean isOpaque() {
      return this.blockMaterial.isOpaque();
   }

   @Override
   public boolean isPowerSource() {
      return this.blockMaterial.isPowerSource();
   }

   @Override
   public boolean isLiquid() {
      return this.blockMaterial.isLiquid();
   }

   @Override
   public boolean isSolid() {
      return this.blockMaterial.isSolid();
   }

   @Override
   public float getHardness() {
      return this.blockMaterial.getHardness();
   }

   @Override
   public float getResistance() {
      return this.blockMaterial.getResistance();
   }

   @Override
   public float getSlipperiness() {
      return this.blockMaterial.getSlipperiness();
   }

   @Override
   public int getLightValue() {
      return this.blockMaterial.getLightValue();
   }

   @Override
   public int getLightOpacity() {
      return this.blockMaterial.getLightOpacity();
   }

   @Override
   public boolean isFragileWhenPushed() {
      return this.blockMaterial.isFragileWhenPushed();
   }

   @Override
   public boolean isUnpushable() {
      return this.blockMaterial.isUnpushable();
   }

   @Override
   public boolean isTicksRandomly() {
      return this.blockMaterial.isTicksRandomly();
   }

   @Override
   public boolean isMovementBlocker() {
      return this.blockMaterial.isMovementBlocker();
   }

   @Override
   public boolean isBurnable() {
      return this.blockMaterial.isBurnable();
   }

   @Override
   public boolean isToolRequired() {
      return this.blockMaterial.isToolRequired();
   }

   @Override
   public boolean isReplacedDuringPlacement() {
      return this.blockMaterial.isReplacedDuringPlacement();
   }

   @Override
   public boolean isTranslucent() {
      return this.blockMaterial.isTranslucent();
   }

   @Override
   public boolean hasContainer() {
      return this.blockMaterial.hasContainer();
   }

   @Override
   public boolean isTile() {
      return this.blockMaterial.isTile();
   }

   @Override
   public CompoundTag getDefaultTile() {
      return this.blockMaterial.getDefaultTile();
   }

   @Nullable
   @Override
   public FaweCompoundTag defaultTile() {
      return this.blockMaterial.defaultTile();
   }

   static {
      DEFAULT_MATERIAL.setFullCube(true);
      DEFAULT_MATERIAL.setOpaque(true);
      DEFAULT_MATERIAL.setSolid(true);
      DEFAULT_MATERIAL.setTicksRandomly(true);
      DEFAULT_MATERIAL.setMovementBlocker(true);
      DEFAULT_MATERIAL.setBurnable(true);
      DEFAULT_MATERIAL.setToolRequired(true);
   }
}
