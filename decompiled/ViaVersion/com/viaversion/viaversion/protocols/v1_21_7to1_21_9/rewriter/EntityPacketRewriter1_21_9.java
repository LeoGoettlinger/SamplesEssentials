package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandler;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_21_9 extends EntityRewriter<ClientboundPacket1_21_6, Protocol1_21_7To1_21_9> {
   public EntityPacketRewriter1_21_9(Protocol1_21_7To1_21_9 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets1_21_6.ADD_ENTITY, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.UUID);
         int entityTypeId = wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.LOW_PRECISION_VECTOR, Vector3d.ZERO);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         int data = wrapper.passthrough(Types.VAR_INT);
         TrackedEntity entity = this.trackAndRewrite(wrapper, entityTypeId, entityId);
         if (this.protocol.getMappingData() != null && entity.entityType() == EntityTypes1_21_9.FALLING_BLOCK) {
            int mappedBlockStateId = this.protocol.getMappingData().getNewBlockStateId(data);
            wrapper.set(Types.VAR_INT, 2, mappedBlockStateId);
         }

         wrapper.set(Types.LOW_PRECISION_VECTOR, 0, this.readRelativeMovement(wrapper));
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_6.SET_ENTITY_MOTION, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.LOW_PRECISION_VECTOR, this.readRelativeMovement(wrapper));
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_6.PLAYER_ROTATION, wrapper -> {
         wrapper.passthrough(Types.FLOAT);
         wrapper.write(Types.BOOLEAN, false);
         wrapper.passthrough(Types.FLOAT);
         wrapper.write(Types.BOOLEAN, false);
      });
      this.protocol.registerClientbound(ClientboundPackets1_21_6.SET_DEFAULT_SPAWN_POSITION, wrapper -> {
         BlockPosition pos = wrapper.read(Types.BLOCK_POSITION1_14);
         String dimension = this.<EntityTracker>tracker(wrapper.user()).currentWorld();
         wrapper.write(Types.GLOBAL_POSITION, new GlobalBlockPosition(dimension, pos.x(), pos.y(), pos.z()));
         wrapper.passthrough(Types.FLOAT);
         wrapper.write(Types.FLOAT, 0.0F);
      });
      this.protocol.getRegistryDataRewriter().addHandler("dimension_type", (key, dimension) -> {
         if (Key.equals(key, "minecraft:the_end")) {
            dimension.putFloat("ambient_light", 0.25F);
         }
      });
   }

   private Vector3d readRelativeMovement(PacketWrapper wrapper) {
      double movementX = wrapper.read(Types.SHORT).shortValue() / 8000.0;
      double movementY = wrapper.read(Types.SHORT).shortValue() / 8000.0;
      double movementZ = wrapper.read(Types.SHORT).shortValue() / 8000.0;
      return new Vector3d(movementX, movementY, movementZ);
   }

   @Override
   protected void registerRewrites() {
      EntityDataTypes1_21_5 unmappedEntityDataTypes = this.protocol.types().entityDataTypes();
      EntityDataTypes1_21_9 entityDataTypes = this.protocol.mappedTypes().entityDataTypes();
      this.dataTypeMapper()
         .added(entityDataTypes.copperGolemState)
         .added(entityDataTypes.weatheringCopperState)
         .removed(unmappedEntityDataTypes.compoundTagType)
         .register();
      this.registerEntityDataTypeHandler(
         entityDataTypes.itemType,
         entityDataTypes.blockStateType,
         entityDataTypes.optionalBlockStateType,
         entityDataTypes.particleType,
         entityDataTypes.particlesType,
         entityDataTypes.componentType,
         entityDataTypes.optionalComponentType
      );
      EntityDataHandler shoulderDataHandler = (event, data) -> {
         CompoundTag value = data.value();
         if (value == null) {
            data.setTypeAndValue(this.protocol.mappedTypes().entityDataTypes.optionalVarIntType, null);
         } else {
            int variant = value.getInt("Variant", -1);
            if (variant != -1) {
               data.setTypeAndValue(this.protocol.mappedTypes().entityDataTypes.optionalVarIntType, variant);
            } else {
               data.setTypeAndValue(this.protocol.mappedTypes().entityDataTypes.optionalVarIntType, null);
            }
         }
      };
      this.filter().type(EntityTypes1_21_9.PLAYER).index(19).handler(shoulderDataHandler);
      this.filter().type(EntityTypes1_21_9.PLAYER).index(20).handler(shoulderDataHandler);
      this.filter().type(EntityTypes1_21_9.PLAYER).handler((event, data) -> {
         if (event.index() == 17) {
            event.setIndex(16);
         } else if (event.index() == 18) {
            event.setIndex(15);
         } else if (event.index() == 15 || event.index() == 16) {
            event.setIndex(event.index() + 2);
         }
      });
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_9.getTypeFromId(type);
   }
}
