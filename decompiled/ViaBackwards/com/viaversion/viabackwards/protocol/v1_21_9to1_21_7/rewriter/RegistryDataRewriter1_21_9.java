package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.DimensionScaleStorage;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.ProtocolStorables1_21_9;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;

public final class RegistryDataRewriter1_21_9 extends BackwardsRegistryRewriter {
   private final BackwardsProtocol<?, ?, ?, ?> backwardsProtocol;

   public RegistryDataRewriter1_21_9(BackwardsProtocol<?, ?, ?, ?> protocol) {
      super(protocol);
      this.backwardsProtocol = protocol;
   }

   public void trackDimensionAndBiomes(UserConnection connection, String registryKey, RegistryEntry[] entries) {
      super.trackDimensionAndBiomes(connection, registryKey, entries);
      if (registryKey.equals("dimension_type")) {
         ProtocolStorables1_21_9 storables = (ProtocolStorables1_21_9)connection.storables(this.backwardsProtocol);
         DimensionScaleStorage dimensionScaleStorage = storables.dimensionScaleStorage();

         for (int i = 0; i < entries.length; i++) {
            RegistryEntry entry = entries[i];
            CompoundTag dimension = (CompoundTag)entry.tag();
            if (dimension != null) {
               double coordinateScale = dimension.getDouble("coordinate_scale", 1.0);
               dimensionScaleStorage.setScale(i, coordinateScale);
            }
         }
      }
   }
}
