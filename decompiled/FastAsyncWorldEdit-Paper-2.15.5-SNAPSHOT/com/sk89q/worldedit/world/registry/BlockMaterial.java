package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import javax.annotation.Nullable;

public interface BlockMaterial {
   boolean isAir();

   boolean isFullCube();

   boolean isOpaque();

   boolean isPowerSource();

   boolean isLiquid();

   boolean isSolid();

   float getHardness();

   float getResistance();

   float getSlipperiness();

   int getLightValue();

   boolean isFragileWhenPushed();

   boolean isUnpushable();

   boolean isTicksRandomly();

   boolean isMovementBlocker();

   boolean isBurnable();

   boolean isToolRequired();

   boolean isReplacedDuringPlacement();

   boolean isTranslucent();

   boolean hasContainer();

   int getLightOpacity();

   boolean isTile();

   @Nullable
   default CompoundTag getDefaultTile() {
      FaweCompoundTag faweCompoundTag = this.defaultTile();
      return faweCompoundTag == null ? null : new CompoundTag(faweCompoundTag.linTag());
   }

   @Nullable
   FaweCompoundTag defaultTile();

   int getMapColor();
}
