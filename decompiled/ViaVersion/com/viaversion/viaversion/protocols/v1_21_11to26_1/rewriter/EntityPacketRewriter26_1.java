package com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter;

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.storage.ProtocolStorables26_1;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter26_1 extends EntityRewriter<ClientboundPacket1_21_11, Protocol1_21_11To26_1> {
   public EntityPacketRewriter26_1(Protocol1_21_11To26_1 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.appendClientbound(ClientboundPackets1_21_11.RESPAWN, wrapper -> {
         ProtocolStorables26_1 storables = wrapper.user().storables(this.protocol);
         storables.setSneaking(false);
      });
      this.protocol.registerServerbound(ServerboundPackets26_1.PLAYER_INPUT, wrapper -> {
         byte flags = wrapper.passthrough(Types.BYTE);
         boolean pressingShift = (flags & 32) != 0;
         ProtocolStorables26_1 storables = wrapper.user().storables(this.protocol);
         storables.setSneaking(pressingShift);
      });
      this.protocol.registerServerbound(ServerboundPackets26_1.INTERACT, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, 0);
         int hand = wrapper.passthrough(Types.VAR_INT);
         Vector3d location = wrapper.read(Types.LOW_PRECISION_VECTOR);
         boolean secondaryAction = wrapper.passthrough(Types.BOOLEAN);
         ProtocolStorables26_1 storables = wrapper.user().storables(this.protocol);
         storables.setSneaking(secondaryAction);
         PacketWrapper interactAtPacket = wrapper.create(ServerboundPackets1_21_6.INTERACT);
         interactAtPacket.write(Types.VAR_INT, entityId);
         interactAtPacket.write(Types.VAR_INT, 2);
         interactAtPacket.write(Types.FLOAT, (float)location.x());
         interactAtPacket.write(Types.FLOAT, (float)location.y());
         interactAtPacket.write(Types.FLOAT, (float)location.z());
         interactAtPacket.write(Types.VAR_INT, hand);
         interactAtPacket.write(Types.BOOLEAN, secondaryAction);
         interactAtPacket.sendToServer(Protocol1_21_11To26_1.class);
      });
      this.protocol.registerServerbound(ServerboundPackets26_1.ATTACK, ServerboundPackets1_21_6.INTERACT, this::writeInteract);
      this.protocol.registerServerbound(ServerboundPackets26_1.SPECTATE_ENTITY, ServerboundPackets1_21_6.INTERACT, this::writeInteract);
   }

   private void writeInteract(PacketWrapper wrapper) {
      wrapper.passthrough(Types.VAR_INT);
      wrapper.write(Types.VAR_INT, 1);
      ProtocolStorables26_1 storables = wrapper.user().storables(this.protocol);
      wrapper.write(Types.BOOLEAN, storables.sneaking());
   }

   @Override
   protected void registerRewrites() {
      EntityDataTypes26_1 entityDataTypes = this.protocol.mappedTypes().entityDataTypes();
      this.dataTypeMapper()
         .added(entityDataTypes.catSoundVariant)
         .added(entityDataTypes.cowSoundVariant)
         .added(entityDataTypes.pigSoundVariant)
         .added(entityDataTypes.chickenSoundVariant)
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
      this.filter().type(EntityTypes1_21_11.VILLAGER).addIndex(19);
      this.filter().type(EntityTypes1_21_11.ZOMBIE_VILLAGER).addIndex(21);
      this.filter().type(EntityTypes1_21_11.ABSTRACT_AGEABLE).addIndex(17);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_11.getTypeFromId(type);
   }
}
