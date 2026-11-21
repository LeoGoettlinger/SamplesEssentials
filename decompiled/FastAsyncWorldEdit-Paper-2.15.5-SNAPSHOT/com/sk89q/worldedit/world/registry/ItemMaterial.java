package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;

public interface ItemMaterial {
   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   default int getMaxStackSize() {
      return this.maxStackSize();
   }

   @NonAbstractForCompatibility(
      delegateName = "getMaxStackSize",
      delegateParams = {}
   )
   default int maxStackSize() {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.getMaxStackSize();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   default int getMaxDamage() {
      return this.maxDamage();
   }

   @NonAbstractForCompatibility(
      delegateName = "getMaxDamage",
      delegateParams = {}
   )
   default int maxDamage() {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.getMaxDamage();
   }
}
