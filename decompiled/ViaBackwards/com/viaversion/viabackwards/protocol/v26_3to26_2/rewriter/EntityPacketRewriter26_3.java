package com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter;

import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v26_3to26_2.Protocol26_3To26_2;
import com.viaversion.viabackwards.protocol.v26_3to26_2.storage.ProtocolStorables26_3;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_3;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_3;
import com.viaversion.viaversion.api.minecraft.item.data.SwingAnimation;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPacket26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPackets26_3;
import java.util.ArrayList;
import java.util.List;

public final class EntityPacketRewriter26_3 extends EntityRewriter<ClientboundPacket26_3, Protocol26_3To26_2> {
   private static final EntityDataTypes26_1 MAPPED_DATA_TYPES = (EntityDataTypes26_1)VersionedTypes.V26_2.entityDataTypes;
   private static final int NO_SWING_ANIMATION = 0;
   private static final int CHANGE_DESTROY_DIRECTION_ACTION = 1;
   private static final int WHITE_CARPET_BLOCK_STATE = 12896;

   public EntityPacketRewriter26_3(Protocol26_3To26_2 protocol) {
      super(protocol, MAPPED_DATA_TYPES.optionalComponentType, MAPPED_DATA_TYPES.booleanType);
   }

   public void registerPackets() {
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.MOVE_ENTITY_POS, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int properties = (Integer)wrapper.read(Types.VAR_INT);
         boolean onGround = (properties & 1) != 0;
         this.handleMovePos(wrapper, properties);
         wrapper.write(Types.BOOLEAN, onGround);
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.MOVE_ENTITY_POS_ROT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int properties = (Integer)wrapper.read(Types.VAR_INT);
         boolean onGround = (properties & 1) != 0;
         this.handleMovePos(wrapper, properties);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         wrapper.write(Types.BOOLEAN, onGround);
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.MOVE_ENTITY_ROT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         boolean onGround = (Boolean)wrapper.read(Types.BOOLEAN);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         wrapper.write(Types.BOOLEAN, onGround);
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.ENTITY_POSITION_SYNC, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int pathType = (Integer)wrapper.read(Types.VAR_INT);
         if (pathType == 0) {
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.write(Types.DOUBLE, 0.0);
            wrapper.write(Types.DOUBLE, 0.0);
            wrapper.write(Types.DOUBLE, 0.0);
         } else {
            int steps = (Integer)wrapper.read(Types.VAR_INT);
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            double previousX = 0.0;
            double previousY = 0.0;
            double previousZ = 0.0;
            int tickOffset = 0;

            for (int i = 0; i < steps; i++) {
               previousX = x;
               previousY = y;
               previousZ = z;
               x = (Double)wrapper.read(Types.DOUBLE);
               y = (Double)wrapper.read(Types.DOUBLE);
               z = (Double)wrapper.read(Types.DOUBLE);
               tickOffset = (Integer)wrapper.read(Types.VAR_INT);
            }

            wrapper.write(Types.DOUBLE, x);
            wrapper.write(Types.DOUBLE, y);
            wrapper.write(Types.DOUBLE, z);
            if (steps > 1 && tickOffset > 0) {
               wrapper.write(Types.DOUBLE, (x - previousX) / tickOffset);
               wrapper.write(Types.DOUBLE, (y - previousY) / tickOffset);
               wrapper.write(Types.DOUBLE, (z - previousZ) / tickOffset);
            } else {
               wrapper.write(Types.DOUBLE, 0.0);
               wrapper.write(Types.DOUBLE, 0.0);
               wrapper.write(Types.DOUBLE, 0.0);
            }
         }
      });
      ((Protocol26_3To26_2)this.protocol).appendClientbound(ClientboundPackets26_3.LOGIN, wrapper -> {
         wrapper.rewindReader(1);
         this.handleGamemodes(wrapper);
      });
      ((Protocol26_3To26_2)this.protocol).appendClientbound(ClientboundPackets26_3.RESPAWN, wrapper -> {
         wrapper.rewindReader(1);
         this.handleGamemodes(wrapper);
      });
      ((Protocol26_3To26_2)this.protocol).registerServerbound(ServerboundPackets26_1.SWING, ServerboundPackets26_3.PUNCH, wrapper -> {
         int hand = (Integer)wrapper.read(Types.VAR_INT);
         if (hand == 1) {
            wrapper.cancel();
         }
      });
      ((Protocol26_3To26_2)this.protocol).registerServerbound(ServerboundPackets26_1.PLAYER_ACTION, wrapper -> {
         int action = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, action >= 1 ? action + 1 : action);
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.SWING_ANIMATION, ClientboundPackets26_1.ANIMATE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int hand = (Integer)wrapper.read(Types.VAR_INT);
         SwingAnimation animation = (SwingAnimation)wrapper.read(SwingAnimation.TYPE);
         if (animation.type() == 0) {
            wrapper.cancel();
         } else {
            wrapper.write(Types.UNSIGNED_BYTE, (short)(hand == 0 ? 0 : 3));
         }
      });
      ((Protocol26_3To26_2)this.protocol).registerServerbound(ServerboundPackets26_1.ACCEPT_TELEPORTATION, wrapper -> {
         int id = (Integer)wrapper.passthrough(Types.VAR_INT);
         ProtocolStorables26_3 storables = (ProtocolStorables26_3)wrapper.user().storables(this.protocol);
         storables.setCurrentTeleportId(id);
         wrapper.cancel();
      });
      ((Protocol26_3To26_2)this.protocol).registerServerbound(ServerboundPackets26_1.MOVE_PLAYER_POS_ROT, wrapper -> {
         ProtocolStorables26_3 storables = (ProtocolStorables26_3)wrapper.user().storables(this.protocol);
         if (storables.currentTeleportId() != null) {
            wrapper.setPacketType(ServerboundPackets26_3.ACCEPT_TELEPORTATION);
            wrapper.write(Types.VAR_INT, storables.currentTeleportId());
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.FLOAT);
            wrapper.read(Types.UNSIGNED_BYTE);
            storables.setCurrentTeleportId(null);
         }
      });
      ((Protocol26_3To26_2)this.protocol).registerClientbound(ClientboundPackets26_3.ANIMATE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         short action = (Short)wrapper.read(Types.UNSIGNED_BYTE);

         wrapper.write(Types.UNSIGNED_BYTE, switch (action) {
            case 0 -> (short)2;
            case 1 -> (short)4;
            case 2 -> (short)5;
            default -> action;
         });
      });
      ((Protocol26_3To26_2)this.protocol).registerServerbound(ServerboundPackets26_1.SPECTATE_ENTITY, ServerboundPackets26_3.SPECTATOR_ACTION);
      ((Protocol26_3To26_2)this.protocol).appendClientbound(ClientboundPackets26_3.ADD_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
         TrackedEntity entity = this.tracker(wrapper.user()).entity(entityId);
         if (entity != null && entity.entityType() == EntityTypes26_3.CUSHION) {
            wrapper.set(Types.VAR_INT, 2, 12896);
            wrapper.send(Protocol26_3To26_2.class);
            wrapper.cancel();
            List<EntityData> entityDataList = new ArrayList<>();
            entityDataList.add(new EntityData(5, MAPPED_DATA_TYPES.booleanType, true));
            PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets26_1.SET_ENTITY_DATA);
            entityDataPacket.write(Types.VAR_INT, entityId);
            entityDataPacket.write(VersionedTypes.V26_2.entityDataList, entityDataList);
            entityDataPacket.send(Protocol26_3To26_2.class);
         }
      });
   }

   private void handleGamemodes(PacketWrapper wrapper) {
      int gamemode = (Integer)wrapper.read(Types.VAR_INT);
      wrapper.write(Types.BYTE, (byte)gamemode);
      Integer previousGamemode = (Integer)wrapper.read(Types.OPTIONAL_VAR_INT);
      wrapper.write(Types.BYTE, previousGamemode == null ? -1 : previousGamemode.byteValue());
   }

   private void handleMovePos(PacketWrapper wrapper, int properties) {
      int steps = properties >>> 1;
      if (steps == 0) {
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.SHORT);
      } else {
         int deltaX = 0;
         int deltaY = 0;
         int deltaZ = 0;

         for (int i = 0; i < steps; i++) {
            wrapper.read(Types.VAR_INT);
            deltaX += wrapper.read(Types.SHORT);
            deltaY += wrapper.read(Types.SHORT);
            deltaZ += wrapper.read(Types.SHORT);
         }

         wrapper.write(Types.SHORT, (short)deltaX);
         wrapper.write(Types.SHORT, (short)deltaY);
         wrapper.write(Types.SHORT, (short)deltaZ);
      }
   }

   protected void registerRewrites() {
      this.dataTypeMapper().removed(((EntityDataTypes26_3)VersionedTypes.V26_3.entityDataTypes).dyeColorType).register();
      this.registerEntityDataTypeHandler1_20_3(
         MAPPED_DATA_TYPES.itemType,
         MAPPED_DATA_TYPES.blockStateType,
         MAPPED_DATA_TYPES.optionalBlockStateType,
         MAPPED_DATA_TYPES.particleType,
         MAPPED_DATA_TYPES.particlesType,
         MAPPED_DATA_TYPES.componentType,
         MAPPED_DATA_TYPES.optionalComponentType
      );
      this.filter().type(EntityTypes26_3.CUSHION).handler((event, data) -> {
         if (event.index() > 7) {
            event.cancel();
         }
      });
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes26_3.CUSHION, EntityTypes26_3.FALLING_BLOCK).tagName();
   }

   public EntityType typeFromId(int type) {
      return EntityTypes26_3.getTypeFromId(type);
   }
}
