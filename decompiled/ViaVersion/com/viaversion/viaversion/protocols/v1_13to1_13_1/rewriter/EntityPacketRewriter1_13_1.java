package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;

import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class EntityPacketRewriter1_13_1 extends EntityRewriter<ClientboundPackets1_13, Protocol1_13To1_13_1> {
   public EntityPacketRewriter1_13_1(Protocol1_13To1_13_1 protocol) {
      super(protocol);
   }

   @Override
   protected void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_13.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.INT);
            this.handler(wrapper -> {
               ClientWorld clientWorld = wrapper.user().<ProtocolStorables>storables(EntityPacketRewriter1_13_1.this.protocol).clientWorld();
               int dimensionId = wrapper.get(Types.INT, 1);
               clientWorld.setEnvironment(dimensionId);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.RESPAWN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.handler(wrapper -> {
               ClientWorld clientWorld = wrapper.user().<ProtocolStorables>storables(EntityPacketRewriter1_13_1.this.protocol).clientWorld();
               int dimensionId = wrapper.get(Types.INT, 0);
               if (clientWorld.setEnvironment(dimensionId)) {
                  EntityPacketRewriter1_13_1.this.<EntityTracker>tracker(wrapper.user()).clearEntities();
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
         @Override
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
            this.handler(wrapper -> {
               int entityId = wrapper.get(Types.VAR_INT, 0);
               byte type = wrapper.get(Types.BYTE, 0);
               int data = wrapper.get(Types.INT, 0);
               EntityTypes1_13.EntityType entType = EntityTypes1_13.ObjectType.getEntityType(type, data);
               if (entType != null) {
                  if (entType.is(EntityTypes1_13.EntityType.FALLING_BLOCK)) {
                     wrapper.set(Types.INT, 0, EntityPacketRewriter1_13_1.this.protocol.getMappingData().getNewBlockStateId(data));
                  }

                  wrapper.user().<EntityTracker>getEntityTracker(EntityPacketRewriter1_13_1.this.protocol).addEntity(entityId, entType);
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
         @Override
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
            this.map(Types1_13.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_13_1.this.trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST));
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types1_13.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_13_1.this.trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST, EntityTypes1_13.EntityType.PLAYER));
         }
      });
      this.registerSetEntityData(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13.ENTITY_DATA_LIST);
   }

   @Override
   protected void registerRewrites() {
      this.registerEntityDataTypeHandler(
         Types1_13.ENTITY_DATA_TYPES.itemType, Types1_13.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_13.ENTITY_DATA_TYPES.particleType
      );
      this.registerBlockStateHandler(EntityTypes1_13.EntityType.ABSTRACT_MINECART, 9);
      this.filter().type(EntityTypes1_13.EntityType.ABSTRACT_ARROW).addIndex(7);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_13.EntityType.findById(type);
   }

   @Override
   public EntityType objectTypeFromId(int type, int data) {
      return EntityTypes1_13.ObjectType.getEntityType(type, data);
   }
}
