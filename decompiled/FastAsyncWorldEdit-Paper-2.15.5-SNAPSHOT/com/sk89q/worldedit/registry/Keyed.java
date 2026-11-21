package com.sk89q.worldedit.registry;

import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;

public interface Keyed {
   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   default String getId() {
      return this.id();
   }

   @NonAbstractForCompatibility(
      delegateName = "getId",
      delegateParams = {}
   )
   default String id() {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.getId();
   }
}
