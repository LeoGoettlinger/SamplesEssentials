package com.viaversion.viabackwards.protocol.v1_10to1_9_3.rewriter;

import com.viaversion.viabackwards.api.entities.storage.EntityReplacement;
import com.viaversion.viabackwards.api.entities.storage.WrappedEntityData;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viabackwards.protocol.v1_10to1_9_3.Protocol1_10To1_9_3;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.ObjectType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import java.util.List;

public class EntityPacketRewriter1_10 extends LegacyEntityRewriter<ClientboundPackets1_9_3, Protocol1_10To1_9_3> {
   public EntityPacketRewriter1_10(Protocol1_10To1_9_3 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol1_10To1_9_3)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, new PacketHandlers() {
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
            this.handler(EntityPacketRewriter1_10.this.getObjectTrackerHandler());
            this.handler(EntityPacketRewriter1_10.this.getObjectRewriter(ObjectType::findById));
            this.handler(((Protocol1_10To1_9_3)EntityPacketRewriter1_10.this.protocol).getItemRewriter().getFallingBlockHandler());
         }
      });
      this.registerTracker(ClientboundPackets1_9_3.ADD_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
      this.registerTracker(ClientboundPackets1_9_3.ADD_GLOBAL_ENTITY, EntityType.LIGHTNING_BOLT);
      ((Protocol1_10To1_9_3)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.UNSIGNED_BYTE);
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
            this.handler(EntityPacketRewriter1_10.this.getTrackerHandler(Types.UNSIGNED_BYTE, 0));
            this.handler(wrapper -> {
               int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
               com.viaversion.viaversion.api.minecraft.entities.EntityType type = EntityPacketRewriter1_10.this.tracker(wrapper.user()).entityType(entityId);
               if (type != null) {
                  List<EntityData> entityDataList = (List<EntityData>)wrapper.get(Types.ENTITY_DATA_LIST1_9, 0);
                  EntityPacketRewriter1_10.this.handleEntityData((Integer)wrapper.get(Types.VAR_INT, 0), entityDataList, wrapper.user());
                  EntityReplacement entityReplacement = EntityPacketRewriter1_10.this.entityDataForType(type);
                  if (entityReplacement != null) {
                     WrappedEntityData storage = new WrappedEntityData(entityDataList);
                     wrapper.set(Types.UNSIGNED_BYTE, 0, (short)entityReplacement.replacementId());
                     if (entityReplacement.hasBaseData()) {
                        entityReplacement.defaultData().createData(storage);
                     }
                  }
               }
            });
         }
      });
      this.registerTracker(ClientboundPackets1_9_3.ADD_PAINTING, EntityType.PAINTING);
      this.registerJoinGame(ClientboundPackets1_9_3.LOGIN, EntityType.PLAYER);
      this.registerRespawn(ClientboundPackets1_9_3.RESPAWN);
      ((Protocol1_10To1_9_3)this.protocol)
         .registerClientbound(
            ClientboundPackets1_9_3.ADD_PLAYER,
            new PacketHandlers() {
               public void register() {
                  this.map(Types.VAR_INT);
                  this.map(Types.UUID);
                  this.map(Types.DOUBLE);
                  this.map(Types.DOUBLE);
                  this.map(Types.DOUBLE);
                  this.map(Types.BYTE);
                  this.map(Types.BYTE);
                  this.map(Types.ENTITY_DATA_LIST1_9);
                  this.handler(
                     EntityPacketRewriter1_10.this.getTrackerAndDataHandler(
                        Types.ENTITY_DATA_LIST1_9, com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.EntityType.PLAYER
                     )
                  );
               }
            }
         );
      this.registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);
      this.registerSetEntityData(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types.ENTITY_DATA_LIST1_9);
   }

   protected void registerRewrites() {
      this.mapEntityTypeWithData(EntityType.POLAR_BEAR, EntityType.SHEEP).plainName();
      this.filter().type(EntityType.POLAR_BEAR).index(13).handler((event, data) -> {
         boolean b = (Boolean)data.getValue();
         data.setTypeAndValue(EntityDataTypes1_9.BYTE, Byte.valueOf((byte)(b ? 14 : 0)));
      });
      this.filter().type(EntityType.ZOMBIE).index(13).handler((event, data) -> {
         if ((Integer)data.getValue() == 6) {
            data.setValue(0);
         }
      });
      this.filter().type(EntityType.SKELETON).index(12).handler((event, data) -> {
         if ((Integer)data.getValue() == 2) {
            data.setValue(0);
         }
      });
      this.filter().type(EntityType.POTION).addIndex(6);
      this.filter().removeIndex(5);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int typeId) {
      return EntityType.findById(typeId);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int typeId, int data) {
      return com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.ObjectType.getEntityType(typeId, data);
   }
}
