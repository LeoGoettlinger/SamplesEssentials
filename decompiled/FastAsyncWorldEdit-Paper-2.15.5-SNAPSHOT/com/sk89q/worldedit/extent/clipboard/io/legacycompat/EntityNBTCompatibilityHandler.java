package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.world.entity.EntityType;
import org.enginehub.linbus.tree.LinCompoundTag;

public interface EntityNBTCompatibilityHandler {
   @Deprecated
   default boolean isAffectedEntity(EntityType type, CompoundTag entityTag) {
      LinCompoundTag original = entityTag.toLinTag();
      LinCompoundTag updated = this.updateNbt(type, original);
      return !original.equals(updated);
   }

   @Deprecated
   default CompoundTag updateNBT(EntityType type, CompoundTag entityTag) {
      return new CompoundTag(this.updateNbt(type, entityTag.toLinTag()));
   }

   @NonAbstractForCompatibility(
      delegateName = "updateNBT",
      delegateParams = {EntityType.class, CompoundTag.class}
   )
   default LinCompoundTag updateNbt(EntityType type, LinCompoundTag entityTag) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.updateNBT(type, new CompoundTag(entityTag)).toLinTag();
   }
}
