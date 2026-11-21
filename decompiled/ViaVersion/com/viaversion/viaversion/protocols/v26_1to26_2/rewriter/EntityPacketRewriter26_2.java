package com.viaversion.viaversion.protocols.v26_1to26_2.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.protocols.v26_1to26_2.storage.ProtocolStorables26_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandler;
import java.util.concurrent.ThreadLocalRandom;

public final class EntityPacketRewriter26_2 extends EntityRewriter<ClientboundPacket26_1, Protocol26_1To26_2> {
   public EntityPacketRewriter26_2(Protocol26_1To26_2 protocol) {
      super(protocol);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.protocol.registerServerbound(ServerboundPackets26_1.SPECTATE_ENTITY, wrapper -> {
         Integer entityId = wrapper.read(Types.OPTIONAL_VAR_INT);
         if (entityId != null) {
            wrapper.write(Types.VAR_INT, this.toOriginalEntityId(wrapper, entityId));
         } else {
            wrapper.setPacketType(ServerboundPackets26_1.SWING);
            wrapper.write(Types.VAR_INT, 0);
         }
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
         ProtocolStorables26_2 storables = wrapper.user().storables(this.protocol);
         storables.setFakeEntityId(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, -1));
         int entityId = wrapper.get(Types.INT, 0);
         wrapper.set(Types.INT, 0, this.toFakeEntityId(wrapper, entityId));
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.BOOLEAN, storables.encrypted());
      });
      PacketHandler setFakeEntityId = wrapper -> wrapper.set(Types.VAR_INT, 0, this.toFakeEntityId(wrapper, wrapper.get(Types.VAR_INT, 0)));
      this.protocol.appendClientbound(ClientboundPackets26_1.ADD_ENTITY, wrapper -> {
         setFakeEntityId.handle(wrapper);
         EntityType entityType = this.typeFromId(wrapper.get(Types.VAR_INT, 1));
         if (entityType != null && entityType.isOrHasParent(EntityTypes26_2.PROJECTILE)) {
            wrapper.set(Types.VAR_INT, 2, this.toFakeEntityId(wrapper, wrapper.get(Types.VAR_INT, 2)));
         }
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.SET_ENTITY_DATA, setFakeEntityId);
      this.protocol.appendClientbound(ClientboundPackets26_1.SET_EQUIPMENT, setFakeEntityId);
      this.protocol.appendClientbound(ClientboundPackets26_1.PLAYER_COMBAT_KILL, setFakeEntityId);
      this.protocol.appendClientbound(ClientboundPackets26_1.UPDATE_ATTRIBUTES, setFakeEntityId);
      this.protocol.appendClientbound(ClientboundPackets26_1.REMOVE_ENTITIES, wrapper -> {
         int[] entities = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);

         for (int i = 0; i < entities.length; i++) {
            entities[i] = this.toFakeEntityId(wrapper, entities[i]);
         }
      });
      PacketHandler toFakeEntityId = wrapper -> wrapper.write(Types.VAR_INT, this.toFakeEntityId(wrapper, wrapper.read(Types.VAR_INT)));
      PacketHandler toOriginalEntityId = wrapper -> wrapper.write(Types.VAR_INT, this.toOriginalEntityId(wrapper, wrapper.read(Types.VAR_INT)));
      this.protocol.registerClientbound(ClientboundPackets26_1.ANIMATE, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.DEBUG_ENTITY_VALUE, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.ENTITY_POSITION_SYNC, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.HURT_ANIMATION, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_ROT, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_ENTITY_POS_ROT, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.MOVE_MINECART_ALONG_TRACK, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.REMOVE_MOB_EFFECT, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.ROTATE_HEAD, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.SET_CAMERA, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.SET_ENTITY_MOTION, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.UPDATE_MOB_EFFECT, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.TELEPORT_ENTITY, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.PROJECTILE_POWER, toFakeEntityId);
      this.protocol.registerClientbound(ClientboundPackets26_1.BLOCK_DESTRUCTION, toFakeEntityId);
      this.protocol
         .registerClientbound(ClientboundPackets26_1.ENTITY_EVENT, wrapper -> wrapper.write(Types.INT, this.toFakeEntityId(wrapper, wrapper.read(Types.INT))));
      this.protocol.registerClientbound(ClientboundPackets26_1.DAMAGE_EVENT, wrapper -> {
         toFakeEntityId.handle(wrapper);
         wrapper.passthrough(Types.VAR_INT);
         Integer sourceCauseId = wrapper.passthrough(Types.OPTIONAL_VAR_INT);
         if (sourceCauseId != null) {
            wrapper.set(Types.OPTIONAL_VAR_INT, 0, this.toFakeEntityId(wrapper, sourceCauseId));
         }

         Integer sourceDirectId = wrapper.passthrough(Types.OPTIONAL_VAR_INT);
         if (sourceDirectId != null) {
            wrapper.set(Types.OPTIONAL_VAR_INT, 1, this.toFakeEntityId(wrapper, sourceDirectId));
         }
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.SOUND_ENTITY, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         toFakeEntityId.handle(wrapper);
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.PLAYER_LOOK_AT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         boolean atEntity = wrapper.passthrough(Types.BOOLEAN);
         if (atEntity) {
            toFakeEntityId.handle(wrapper);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.SET_PASSENGERS, wrapper -> {
         toFakeEntityId.handle(wrapper);
         int[] passengers = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);

         for (int i = 0; i < passengers.length; i++) {
            passengers[i] = this.toFakeEntityId(wrapper, passengers[i]);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.MOUNT_SCREEN_OPEN, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.INT, this.toFakeEntityId(wrapper, wrapper.read(Types.INT)));
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets26_1.SET_ENTITY_LINK, wrapper -> wrapper.write(Types.INT, this.toFakeEntityId(wrapper, wrapper.read(Types.INT)))
         );
      this.protocol.registerClientbound(ClientboundPackets26_1.TAKE_ITEM_ENTITY, wrapper -> {
         toFakeEntityId.handle(wrapper);
         toFakeEntityId.handle(wrapper);
      });
      this.protocol.registerServerbound(ServerboundPackets26_1.ATTACK, toOriginalEntityId);
      this.protocol.registerServerbound(ServerboundPackets26_1.INTERACT, toOriginalEntityId);
      this.protocol.registerServerbound(ServerboundPackets26_1.PICK_ITEM_FROM_ENTITY, toOriginalEntityId);
      this.protocol.registerServerbound(ServerboundPackets26_1.PLAYER_COMMAND, toOriginalEntityId);
      this.protocol.registerServerbound(ServerboundPackets26_1.SET_COMMAND_MINECART, toOriginalEntityId);
      this.protocol.registerServerbound(ServerboundPackets26_1.ENTITY_TAG_QUERY, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         toOriginalEntityId.handle(wrapper);
      });
   }

   private int toFakeEntityId(PacketWrapper wrapper, int entityId) {
      ProtocolStorables26_2 storables = wrapper.user().storables(this.protocol);
      return entityId == 0 ? storables.fakeEntityId() : entityId;
   }

   private int toOriginalEntityId(PacketWrapper wrapper, int entityId) {
      ProtocolStorables26_2 storables = wrapper.user().storables(this.protocol);
      return entityId < -1 && entityId == storables.fakeEntityId() ? 0 : entityId;
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
      this.filter().type(EntityTypes26_2.ABSTRACT_CUBE_MOB).addIndex(16);
      this.filter().type(EntityTypes26_2.ABSTRACT_CUBE_MOB).addIndex(17);
      EntityDataHandler toFakeEntityId = (event, data) -> {
         Integer target = data.value();
         if (target != null) {
            ProtocolStorables26_2 storables = event.user().storables(this.protocol);
            data.setValue(target == 0 ? storables.fakeEntityId() : target);
         }
      };
      this.filter().type(EntityTypes26_2.FROG).index(19).handler(toFakeEntityId);
      this.filter().type(EntityTypes26_2.FIREWORK_ROCKET).index(9).handler(toFakeEntityId);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes26_2.getTypeFromId(type);
   }
}
