package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter;

import com.viaversion.viabackwards.api.entities.EntityScaleHelper;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.Protocol1_21_6To1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;

public final class EntityPacketRewriter1_21_6 extends EntityRewriter<ClientboundPacket1_21_6, Protocol1_21_6To1_21_5> {
   public EntityPacketRewriter1_21_6(Protocol1_21_6To1_21_5 protocol) {
      super(
         protocol,
         ((EntityDataTypes1_21_5)VersionedTypes.V1_21_6.entityDataTypes).optionalComponentType,
         ((EntityDataTypes1_21_5)VersionedTypes.V1_21_6.entityDataTypes).booleanType
      );
   }

   public void registerPackets() {
      ((Protocol1_21_6To1_21_5)this.protocol).replaceClientbound(ClientboundPackets1_21_6.ADD_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.UUID);
         int entityType = (Integer)wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         int data = (Integer)wrapper.passthrough(Types.VAR_INT);
         short velocityX = (Short)wrapper.passthrough(Types.SHORT);
         short velocityY = (Short)wrapper.passthrough(Types.SHORT);
         short velocityZ = (Short)wrapper.passthrough(Types.SHORT);
         this.trackSpawnWithData1_19(wrapper, data);
         if ((velocityX != 0 || velocityY != 0 || velocityZ != 0) && !this.typeFromId(entityType).isOrHasParent(EntityTypes1_21_6.LIVING_ENTITY)) {
            PacketWrapper motionPacket = wrapper.create(ClientboundPackets1_21_5.SET_ENTITY_MOTION);
            motionPacket.write(Types.VAR_INT, entityId);
            motionPacket.write(Types.SHORT, velocityX);
            motionPacket.write(Types.SHORT, velocityY);
            motionPacket.write(Types.SHORT, velocityZ);
            wrapper.send(Protocol1_21_6To1_21_5.class);
            motionPacket.send(Protocol1_21_6To1_21_5.class);
            wrapper.cancel();
         }
      });
      ((Protocol1_21_6To1_21_5)this.protocol).registerServerbound(ServerboundPackets1_21_5.PLAYER_COMMAND, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int action = (Integer)wrapper.read(Types.VAR_INT);
         if (action < 2) {
            wrapper.cancel();
         }

         wrapper.write(Types.VAR_INT, action - 2);
      });
   }

   protected void registerRewrites() {
      EntityDataTypes1_21_5 entityDataTypes = (EntityDataTypes1_21_5)VersionedTypes.V1_21_5.entityDataTypes;
      this.dataTypeMapper().register();
      this.registerEntityDataTypeHandler1_20_3(
         entityDataTypes.itemType,
         entityDataTypes.blockStateType,
         entityDataTypes.optionalBlockStateType,
         entityDataTypes.particleType,
         entityDataTypes.particlesType,
         entityDataTypes.componentType,
         entityDataTypes.optionalComponentType
      );
      this.filter().type(EntityTypes1_21_6.HANGING_ENTITY).removeIndex(8);
      this.filter().type(EntityTypes1_21_6.HAPPY_GHAST).cancel(17);
      this.filter().type(EntityTypes1_21_6.HAPPY_GHAST).cancel(18);
      EntityScaleHelper scaleHelper = new EntityScaleHelper(this, ClientboundPackets1_21_5.UPDATE_ATTRIBUTES);
      scaleHelper.addBabyScale(EntityTypes1_21_6.HAPPY_GHAST, 0.2375F, 16);
      this.filter().type(EntityTypes1_21_6.HAPPY_GHAST).cancel(16);
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes1_21_6.HAPPY_GHAST, EntityTypes1_21_6.GHAST).tagName();
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_21_6.getTypeFromId(type);
   }
}
