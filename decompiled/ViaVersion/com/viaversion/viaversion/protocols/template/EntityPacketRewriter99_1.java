package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;

final class EntityPacketRewriter99_1 extends EntityRewriter<ClientboundPacket26_1, Protocol98_1To99_1> {
   public EntityPacketRewriter99_1(Protocol98_1To99_1 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
   }

   @Override
   protected void registerRewrites() {
      EntityDataTypes26_1 entityDataTypes = this.protocol.mappedTypes().entityDataTypes();
      this.dataTypeMapper().register();
      this.registerEntityDataTypeHandler(
         entityDataTypes.itemType,
         entityDataTypes.blockStateType,
         entityDataTypes.optionalBlockStateType,
         entityDataTypes.particleType,
         entityDataTypes.particlesType,
         entityDataTypes.componentType,
         entityDataTypes.optionalComponentType
      );
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_11.getTypeFromId(type);
   }
}
