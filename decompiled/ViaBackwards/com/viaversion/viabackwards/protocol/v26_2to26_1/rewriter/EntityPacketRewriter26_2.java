package com.viaversion.viabackwards.protocol.v26_2to26_1.rewriter;

import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v26_2to26_1.Protocol26_2To26_1;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;

public final class EntityPacketRewriter26_2 extends EntityRewriter<ClientboundPacket26_1, Protocol26_2To26_1> {
   private static final EntityDataTypes26_1 MAPPED_DATA_TYPES = (EntityDataTypes26_1)VersionedTypes.V26_1.entityDataTypes;

   public EntityPacketRewriter26_2(Protocol26_2To26_1 protocol) {
      super(protocol, MAPPED_DATA_TYPES.optionalComponentType, MAPPED_DATA_TYPES.booleanType);
   }

   protected void registerPackets() {
      super.registerPackets();
      ((Protocol26_2To26_1)this.protocol)
         .registerServerbound(ServerboundPackets26_1.SPECTATE_ENTITY, wrapper -> wrapper.passthroughAndMap(Types.VAR_INT, Types.OPTIONAL_VAR_INT));
      ((Protocol26_2To26_1)this.protocol).appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.read(Types.BOOLEAN);
      });
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
      this.filter().type(EntityTypes26_2.SULFUR_CUBE).cancel(19);
      this.filter().type(EntityTypes26_2.SULFUR_CUBE).cancel(20);
      this.filter().type(EntityTypes26_2.ABSTRACT_CUBE_MOB).removeIndex(17);
      this.filter().type(EntityTypes26_2.ABSTRACT_CUBE_MOB).removeIndex(16);
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes26_2.SULFUR_CUBE, EntityTypes26_2.SLIME).tagName();
   }

   public EntityType typeFromId(int type) {
      return EntityTypes26_2.getTypeFromId(type);
   }
}
