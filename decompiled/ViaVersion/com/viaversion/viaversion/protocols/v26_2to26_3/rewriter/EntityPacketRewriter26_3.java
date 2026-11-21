package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_3;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_3;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.minecraft.item.data.SwingAnimation;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPackets26_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_3 extends EntityRewriter<ClientboundPacket26_1, Protocol26_2To26_3> {
   private static final int SINGLE_STEP = 2;
   private static final int INTERPOLATION_STEP_TICKS = 3;
   private static final int MAIN_HAND = 0;
   private static final int OFF_HAND = 1;
   private static final int CHANGE_DESTROY_DIRECTION_ACTION = 1;
   private static final SwingAnimation DEFAULT_SWING_ANIMATION = new SwingAnimation(EnumTypes.SWING_ANIMATION1_21_11.idFromName("whack"), 6);

   public EntityPacketRewriter26_3(Protocol26_2To26_3 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         short xa = wrapper.read(Types.SHORT);
         short ya = wrapper.read(Types.SHORT);
         short za = wrapper.read(Types.SHORT);
         boolean onGround = wrapper.read(Types.BOOLEAN);
         wrapper.write(Types.VAR_INT, (onGround ? 1 : 0) | 2);
         wrapper.write(Types.VAR_INT, 3);
         wrapper.write(Types.SHORT, xa);
         wrapper.write(Types.SHORT, ya);
         wrapper.write(Types.SHORT, za);
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         short xa = wrapper.read(Types.SHORT);
         short ya = wrapper.read(Types.SHORT);
         short za = wrapper.read(Types.SHORT);
         byte yRot = wrapper.read(Types.BYTE);
         byte xRot = wrapper.read(Types.BYTE);
         boolean onGround = wrapper.read(Types.BOOLEAN);
         wrapper.write(Types.VAR_INT, (onGround ? 1 : 0) | 2);
         wrapper.write(Types.VAR_INT, 3);
         wrapper.write(Types.SHORT, xa);
         wrapper.write(Types.SHORT, ya);
         wrapper.write(Types.SHORT, za);
         wrapper.write(Types.BYTE, yRot);
         wrapper.write(Types.BYTE, xRot);
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_ROT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         byte yRot = wrapper.read(Types.BYTE);
         byte xRot = wrapper.read(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.write(Types.BYTE, yRot);
         wrapper.write(Types.BYTE, xRot);
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.ENTITY_POSITION_SYNC, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, 1);
         wrapper.write(Types.VAR_INT, 1);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.VAR_INT, 3);
         wrapper.read(Types.DOUBLE);
         wrapper.read(Types.DOUBLE);
         wrapper.read(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
         wrapper.rewindReader(1);
         this.handleGamemodes(wrapper);
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.RESPAWN, wrapper -> {
         wrapper.rewindReader(1);
         this.handleGamemodes(wrapper);
      });
      this.protocol.registerServerbound(ServerboundPackets26_3.ACCEPT_TELEPORTATION, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         double x = wrapper.read(Types.DOUBLE);
         double y = wrapper.read(Types.DOUBLE);
         double z = wrapper.read(Types.DOUBLE);
         float yRot = wrapper.read(Types.FLOAT);
         float xRot = wrapper.read(Types.FLOAT);
         wrapper.sendToServer(Protocol26_2To26_3.class);
         wrapper.cancel();
         PacketWrapper movePlayer = wrapper.create(ServerboundPackets26_1.MOVE_PLAYER_POS_ROT);
         movePlayer.write(Types.DOUBLE, x);
         movePlayer.write(Types.DOUBLE, y);
         movePlayer.write(Types.DOUBLE, z);
         movePlayer.write(Types.FLOAT, yRot);
         movePlayer.write(Types.FLOAT, xRot);
         movePlayer.write(Types.UNSIGNED_BYTE, (short)0);
         movePlayer.sendToServer(Protocol26_2To26_3.class);
      });
      this.protocol.registerServerbound(ServerboundPackets26_3.PUNCH, ServerboundPackets26_1.SWING, wrapper -> wrapper.write(Types.VAR_INT, 0));
      this.protocol.registerServerbound(ServerboundPackets26_3.PLAYER_ACTION, wrapper -> {
         int action = wrapper.read(Types.VAR_INT);
         if (action == 1) {
            wrapper.cancel();
         } else {
            wrapper.write(Types.VAR_INT, action > 1 ? action - 1 : action);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.ANIMATE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         short action = wrapper.read(Types.UNSIGNED_BYTE);
         switch (action) {
            case 0:
            case 3:
               wrapper.setPacketType(ClientboundPackets26_3.SWING_ANIMATION);
               wrapper.write(Types.VAR_INT, action == 0 ? 0 : 1);
               wrapper.write(SwingAnimation.TYPE, DEFAULT_SWING_ANIMATION);
               break;
            case 1:
            default:
               wrapper.cancel();
               break;
            case 2:
               wrapper.write(Types.UNSIGNED_BYTE, (short)0);
               break;
            case 4:
               wrapper.write(Types.UNSIGNED_BYTE, (short)1);
               break;
            case 5:
               wrapper.write(Types.UNSIGNED_BYTE, (short)2);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets26_3.SPECTATOR_ACTION, ServerboundPackets26_1.SPECTATE_ENTITY);
   }

   private void handleGamemodes(PacketWrapper wrapper) {
      byte gamemode = wrapper.read(Types.BYTE);
      wrapper.write(Types.VAR_INT, Integer.valueOf(gamemode));
      byte previousGamemode = wrapper.read(Types.BYTE);
      wrapper.write(Types.OPTIONAL_VAR_INT, previousGamemode == -1 ? null : Integer.valueOf(previousGamemode));
   }

   @Override
   protected void registerRewrites() {
      EntityDataTypes26_3 entityDataTypes = this.protocol.mappedTypes().entityDataTypes();
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
      return EntityTypes26_3.getTypeFromId(type);
   }
}
