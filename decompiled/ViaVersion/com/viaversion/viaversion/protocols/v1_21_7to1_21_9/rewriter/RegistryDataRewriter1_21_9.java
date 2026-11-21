package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage.DimensionScaleStorage;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage.ProtocolStorables1_21_9;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class RegistryDataRewriter1_21_9 extends RegistryDataRewriter {
   public RegistryDataRewriter1_21_9(Protocol<?, ?, ?, ?> protocol) {
      super(protocol);
   }

   @Override
   protected void handleParticleData(CompoundTag particleData) {
      String type = particleData.getString("type");
      if (type != null && Key.stripMinecraftNamespace(type).equals("flash")) {
         particleData.putInt("color", -1);
      }

      super.handleParticleData(particleData);
   }

   @Override
   public void trackDimensionAndBiomes(UserConnection connection, String registryKey, RegistryEntry[] entries) {
      super.trackDimensionAndBiomes(connection, registryKey, entries);
      if (registryKey.equals("dimension_type")) {
         ProtocolStorables1_21_9 storables = connection.storables(this.protocol);
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
