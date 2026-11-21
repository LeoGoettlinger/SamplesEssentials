package com.viaversion.viabackwards.protocol.template;

import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;

final class EntityPacketRewriter99_1 extends EntityRewriter<ClientboundPacket26_1, Protocol99_1To98_1> {
   private static final EntityDataTypes26_1 MAPPED_DATA_TYPES = (EntityDataTypes26_1)VersionedTypes.V26_1.entityDataTypes;

   public EntityPacketRewriter99_1(Protocol99_1To98_1 protocol) {
      super(protocol, MAPPED_DATA_TYPES.optionalComponentType, MAPPED_DATA_TYPES.booleanType);
   }

   public void registerPackets() {
   }

   protected void registerRewrites() {
      this.dataTypeMapper().register();
      this.registerEntityDataTypeHandler1_20_3(
         MAPPED_DATA_TYPES.itemType,
         MAPPED_DATA_TYPES.blockStateType,
         MAPPED_DATA_TYPES.optionalBlockStateType,
         MAPPED_DATA_TYPES.particleType,
         MAPPED_DATA_TYPES.particlesType,
         MAPPED_DATA_TYPES.componentType,
         MAPPED_DATA_TYPES.optionalComponentType
      );
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_21_11.getTypeFromId(type);
   }
}
