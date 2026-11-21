package com.viaversion.viabackwards.protocol.v1_13_1to1_13.rewriter;

import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viabackwards.protocol.v1_13_1to1_13.Protocol1_13_1To1_13;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13.ObjectType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import java.util.List;

public class EntityPacketRewriter1_13_1 extends LegacyEntityRewriter<ClientboundPackets1_13, Protocol1_13_1To1_13> {
   public EntityPacketRewriter1_13_1(Protocol1_13_1To1_13 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol1_13_1To1_13)this.protocol).registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
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
               int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
               byte type = (Byte)wrapper.get(Types.BYTE, 0);
               int data = (Integer)wrapper.get(Types.INT, 0);
               EntityType entType = ObjectType.getEntityType(type, data);
               if (entType != null) {
                  if (entType.is(EntityType.FALLING_BLOCK)) {
                     wrapper.set(Types.INT, 0, ((Protocol1_13_1To1_13)EntityPacketRewriter1_13_1.this.protocol).getMappingData().getNewBlockStateId(data));
                  }

                  EntityPacketRewriter1_13_1.this.tracker(wrapper.user()).addEntity(entityId, entType);
               }
            });
         }
      });
      this.registerTracker(ClientboundPackets1_13.ADD_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
      this.registerTracker(ClientboundPackets1_13.ADD_GLOBAL_ENTITY, EntityType.LIGHTNING_BOLT);
      ((Protocol1_13_1To1_13)this.protocol).registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
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
            this.handler(EntityPacketRewriter1_13_1.this.getTrackerHandler());
            this.handler(wrapper -> {
               List<EntityData> entityDataList = (List<EntityData>)wrapper.get(Types1_13.ENTITY_DATA_LIST, 0);
               EntityPacketRewriter1_13_1.this.handleEntityData((Integer)wrapper.get(Types.VAR_INT, 0), entityDataList, wrapper.user());
            });
         }
      });
      ((Protocol1_13_1To1_13)this.protocol).registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types1_13.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_13_1.this.getTrackerAndDataHandler(Types1_13.ENTITY_DATA_LIST, EntityType.PLAYER));
         }
      });
      this.registerTracker(ClientboundPackets1_13.ADD_PAINTING, EntityType.PAINTING);
      this.registerJoinGame(ClientboundPackets1_13.LOGIN, EntityType.PLAYER);
      this.registerRespawn(ClientboundPackets1_13.RESPAWN);
      this.registerSetEntityData(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13.ENTITY_DATA_LIST);
   }

   protected void registerRewrites() {
      this.filter().handler((event, data) -> {
         if (data.dataType() == Types1_13.ENTITY_DATA_TYPES.itemType) {
            data.setValue(((Protocol1_13_1To1_13)this.protocol).getItemRewriter().handleItemToClient(event.user(), (Item)data.getValue()));
         } else if (data.dataType() == Types1_13.ENTITY_DATA_TYPES.optionalBlockStateType) {
            int value = (Integer)data.getValue();
            data.setValue(((Protocol1_13_1To1_13)this.protocol).getMappingData().getNewBlockStateId(value));
         } else if (data.dataType() == Types1_13.ENTITY_DATA_TYPES.particleType) {
            ((Protocol1_13_1To1_13)this.protocol).getParticleRewriter().rewriteParticle(event.user(), (Particle)data.getValue());
         } else if (data.dataType() == Types1_13.ENTITY_DATA_TYPES.optionalComponentType || data.dataType() == Types1_13.ENTITY_DATA_TYPES.componentType) {
            JsonElement element = (JsonElement)data.value();
            ((Protocol1_13_1To1_13)this.protocol).translatableRewriter().processText(event.user(), element);
         }
      });
      this.filter().type(EntityType.ABSTRACT_ARROW).cancel(7);
      this.filter().type(EntityType.SPECTRAL_ARROW).index(8).toIndex(7);
      this.filter().type(EntityType.TRIDENT).index(8).toIndex(7);
      this.registerBlockStateHandler(EntityType.ABSTRACT_MINECART, 9);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int typeId) {
      return EntityType.findById(typeId);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int typeId, int data) {
      return ObjectType.getEntityType(typeId, data);
   }
}
