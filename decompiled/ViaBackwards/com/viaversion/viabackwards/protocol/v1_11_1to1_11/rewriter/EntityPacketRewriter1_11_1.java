package com.viaversion.viabackwards.protocol.v1_11_1to1_11.rewriter;

import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viabackwards.protocol.v1_11_1to1_11.Protocol1_11_1To1_11;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.ObjectType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;

public class EntityPacketRewriter1_11_1 extends LegacyEntityRewriter<ClientboundPackets1_9_3, Protocol1_11_1To1_11> {
   public EntityPacketRewriter1_11_1(Protocol1_11_1To1_11 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol1_11_1To1_11)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.BYTE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.INT);
            this.handler(EntityPacketRewriter1_11_1.this.getObjectTrackerHandler());
            this.handler(EntityPacketRewriter1_11_1.this.getObjectRewriter(ObjectType::findById));
         }
      });
      this.registerTracker(ClientboundPackets1_9_3.ADD_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
      this.registerTracker(ClientboundPackets1_9_3.ADD_GLOBAL_ENTITY, EntityType.LIGHTNING_BOLT);
      ((Protocol1_11_1To1_11)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.VAR_INT);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.map(Types.ENTITY_DATA_LIST1_9);
            this.handler(EntityPacketRewriter1_11_1.this.getTrackerHandler());
            this.handler(EntityPacketRewriter1_11_1.this.getMobSpawnRewriter1_11(Types.ENTITY_DATA_LIST1_9));
         }
      });
      this.registerTracker(ClientboundPackets1_9_3.ADD_PAINTING, EntityType.PAINTING);
      this.registerJoinGame(ClientboundPackets1_9_3.LOGIN, EntityType.PLAYER);
      this.registerRespawn(ClientboundPackets1_9_3.RESPAWN);
      ((Protocol1_11_1To1_11)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_PLAYER, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.ENTITY_DATA_LIST1_9);
            this.handler(EntityPacketRewriter1_11_1.this.getTrackerAndDataHandler(Types.ENTITY_DATA_LIST1_9, EntityType.PLAYER));
         }
      });
      this.registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);
      this.registerSetEntityData(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types.ENTITY_DATA_LIST1_9);
   }

   protected void registerRewrites() {
      this.filter().type(EntityType.FIREWORK_ROCKET).cancel(7);
      this.filter().type(EntityType.PIG).cancel(14);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int typeId) {
      return EntityType.findById(typeId);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int typeId, int data) {
      return ObjectType.getEntityType(typeId, data);
   }
}
