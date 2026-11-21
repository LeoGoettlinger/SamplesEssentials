package com.viaversion.viaversion.protocols.v1_18_2to1_19.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DimensionRegistryStorage implements StorableObject {
   private Map<CompoundTag, String> dimensions;

   @Nullable
   public String dimensionKey(CompoundTag dimensionData) {
      return this.dimensions.get(dimensionData);
   }

   public void setDimensions(Map<CompoundTag, String> dimensions) {
      this.dimensions = dimensions;
   }

   public Map<CompoundTag, String> dimensions() {
      return this.dimensions;
   }
}
