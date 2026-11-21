package com.viaversion.viabackwards.protocol.v1_11to1_10.rewriter;

import com.viaversion.viabackwards.api.entities.storage.WrappedEntityData;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viabackwards.protocol.v1_11to1_10.data.SplashPotionMappings1_10;
import com.viaversion.viabackwards.protocol.v1_11to1_10.storage.ChestedHorseStorage;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.ObjectType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import java.util.List;

public class EntityPacketRewriter1_11 extends LegacyEntityRewriter<ClientboundPackets1_9_3, Protocol1_11To1_10> {
   public EntityPacketRewriter1_11(Protocol1_11To1_10 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol1_11To1_10)this.protocol).registerClientbound(ClientboundPackets1_9_3.LEVEL_EVENT, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BLOCK_POSITION1_8);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int type = (Integer)wrapper.get(Types.INT, 0);
               if (type == 2002 || type == 2007) {
                  if (type == 2007) {
                     wrapper.set(Types.INT, 0, 2002);
                  }

                  int mappedData = SplashPotionMappings1_10.getOldData((Integer)wrapper.get(Types.INT, 1));
                  if (mappedData != -1) {
                     wrapper.set(Types.INT, 1, mappedData);
                  }
               }
            });
         }
      });
      ((Protocol1_11To1_10)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, new PacketHandlers() {
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
            this.handler(EntityPacketRewriter1_11.this.getObjectTrackerHandler());
            this.handler(EntityPacketRewriter1_11.this.getObjectRewriter(ObjectType::findById));
            this.handler(((Protocol1_11To1_10)EntityPacketRewriter1_11.this.protocol).getItemRewriter().getFallingBlockHandler());
         }
      });
      this.registerTracker(ClientboundPackets1_9_3.ADD_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
      this.registerTracker(ClientboundPackets1_9_3.ADD_GLOBAL_ENTITY, EntityType.LIGHTNING_BOLT);
      ((Protocol1_11To1_10)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.VAR_INT, Types.UNSIGNED_BYTE);
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
            this.handler(EntityPacketRewriter1_11.this.getTrackerHandler(Types.UNSIGNED_BYTE, 0));
            this.handler(EntityPacketRewriter1_11.this.getMobSpawnRewriter(Types.ENTITY_DATA_LIST1_9));
            this.handler(wrapper -> {
               List<EntityData> entityDataList = (List<EntityData>)wrapper.get(Types.ENTITY_DATA_LIST1_9, 0);
               if (entityDataList.isEmpty()) {
                  entityDataList.add(new EntityData(0, EntityDataTypes1_9.BYTE, (byte)0));
               }
            });
         }
      });
      this.registerTracker(ClientboundPackets1_9_3.ADD_PAINTING, EntityType.PAINTING);
      this.registerJoinGame(ClientboundPackets1_9_3.LOGIN, EntityType.PLAYER);
      this.registerRespawn(ClientboundPackets1_9_3.RESPAWN);
      ((Protocol1_11To1_10)this.protocol).registerClientbound(ClientboundPackets1_9_3.ADD_PLAYER, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.ENTITY_DATA_LIST1_9);
            this.handler(EntityPacketRewriter1_11.this.getTrackerAndDataHandler(Types.ENTITY_DATA_LIST1_9, EntityType.PLAYER));
            this.handler(wrapper -> {
               List<EntityData> entityDataList = (List<EntityData>)wrapper.get(Types.ENTITY_DATA_LIST1_9, 0);
               if (entityDataList.isEmpty()) {
                  entityDataList.add(new EntityData(0, EntityDataTypes1_9.BYTE, (byte)0));
               }
            });
         }
      });
      this.registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);
      this.registerSetEntityData(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types.ENTITY_DATA_LIST1_9);
      ((Protocol1_11To1_10)this.protocol).registerClientbound(ClientboundPackets1_9_3.ENTITY_EVENT, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               int entityId = (Integer)wrapper.get(Types.INT, 0);
               if (entityId == EntityPacketRewriter1_11.this.tracker(wrapper.user()).clientEntityId()) {
                  byte entityStatus = (Byte)wrapper.get(Types.BYTE, 0);
                  if (entityStatus == 35) {
                     wrapper.clearPacket();
                     wrapper.setPacketType(ClientboundPackets1_9_3.GAME_EVENT);
                     wrapper.write(Types.UNSIGNED_BYTE, (short)10);
                     wrapper.write(Types.FLOAT, 0.0F);
                  }
               }
            });
         }
      });
   }

   protected void registerRewrites() {
      this.mapEntityTypeWithData(EntityType.ELDER_GUARDIAN, EntityType.GUARDIAN);
      this.mapEntityTypeWithData(EntityType.WITHER_SKELETON, EntityType.SKELETON).spawnEntityData(storage -> storage.add(this.getSkeletonTypeData(1)));
      this.mapEntityTypeWithData(EntityType.STRAY, EntityType.SKELETON).plainName().spawnEntityData(storage -> storage.add(this.getSkeletonTypeData(2)));
      this.mapEntityTypeWithData(EntityType.HUSK, EntityType.ZOMBIE).plainName().spawnEntityData(storage -> this.handleZombieType(storage, 6));
      this.mapEntityTypeWithData(EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE).spawnEntityData(storage -> this.handleZombieType(storage, 1));
      this.mapEntityTypeWithData(EntityType.HORSE, EntityType.HORSE).spawnEntityData(storage -> storage.add(this.getHorseDataType(0)));
      this.mapEntityTypeWithData(EntityType.DONKEY, EntityType.HORSE).spawnEntityData(storage -> storage.add(this.getHorseDataType(1)));
      this.mapEntityTypeWithData(EntityType.MULE, EntityType.HORSE).spawnEntityData(storage -> storage.add(this.getHorseDataType(2)));
      this.mapEntityTypeWithData(EntityType.SKELETON_HORSE, EntityType.HORSE).spawnEntityData(storage -> storage.add(this.getHorseDataType(4)));
      this.mapEntityTypeWithData(EntityType.ZOMBIE_HORSE, EntityType.HORSE).spawnEntityData(storage -> storage.add(this.getHorseDataType(3)));
      this.mapEntityTypeWithData(EntityType.EVOKER_FANGS, EntityType.SHULKER);
      this.mapEntityTypeWithData(EntityType.EVOKER, EntityType.VILLAGER).plainName();
      this.mapEntityTypeWithData(EntityType.VEX, EntityType.BAT).plainName();
      this.mapEntityTypeWithData(EntityType.VINDICATOR, EntityType.VILLAGER)
         .plainName()
         .spawnEntityData(storage -> storage.add(new EntityData(13, EntityDataTypes1_9.VAR_INT, 4)));
      this.mapEntityTypeWithData(EntityType.LLAMA, EntityType.HORSE).plainName().spawnEntityData(storage -> storage.add(this.getHorseDataType(1)));
      this.mapEntityTypeWithData(EntityType.LLAMA_SPIT, EntityType.SNOWBALL);
      this.mapObjectType(ObjectType.LLAMA_SPIT, ObjectType.SNOWBALL, -1);
      this.mapObjectType(ObjectType.EVOKER_FANGS, ObjectType.FALLING_BLOCK, 4294);
      this.filter().type(EntityType.GUARDIAN).index(12).handler((event, data) -> {
         boolean b = (Boolean)data.getValue();
         int bitmask = b ? 2 : 0;
         if (event.entityType() == EntityType.ELDER_GUARDIAN) {
            bitmask |= 4;
         }

         data.setTypeAndValue(EntityDataTypes1_9.BYTE, (byte)bitmask);
      });
      this.filter().type(EntityType.ABSTRACT_SKELETON).index(12).toIndex(13);
      this.filter().type(EntityType.ZOMBIE).handler((event, data) -> {
         switch (data.id()) {
            case 13:
               event.cancel();
               break;
            case 14:
               event.setIndex(15);
               break;
            case 15:
               event.setIndex(14);
               break;
            case 16:
               event.setIndex(13);
               data.setValue(1 + (Integer)data.getValue());
         }
      });
      this.filter().type(EntityType.EVOKER).index(12).handler((event, data) -> {
         event.setIndex(13);
         data.setTypeAndValue(EntityDataTypes1_9.VAR_INT, ((Byte)data.getValue()).intValue());
      });
      this.filter().type(EntityType.VEX).index(12).handler((event, data) -> data.setValue((byte)0));
      this.filter().type(EntityType.VINDICATOR).index(12).handler((event, data) -> {
         event.setIndex(13);
         data.setTypeAndValue(EntityDataTypes1_9.VAR_INT, ((Number)data.getValue()).intValue() == 1 ? 2 : 4);
      });
      this.filter().type(EntityType.ABSTRACT_HORSE).index(13).handler((event, data) -> {
         byte b = (Byte)data.getValue();
         ChestedHorseStorage chestedHorseStorage = (ChestedHorseStorage)event.trackedEntity().get(ChestedHorseStorage.class);
         if (chestedHorseStorage != null && chestedHorseStorage.isChested()) {
            b = (byte)(b | 8);
            data.setValue(b);
         }
      });
      this.filter().type(EntityType.CHESTED_HORSE).handler((event, data) -> {
         if (!event.trackedEntity().has(ChestedHorseStorage.class)) {
            event.trackedEntity().put(new ChestedHorseStorage());
         }
      });
      this.filter().type(EntityType.HORSE).index(16).toIndex(17);
      this.filter().type(EntityType.CHESTED_HORSE).index(15).handler((event, data) -> {
         ChestedHorseStorage storage = (ChestedHorseStorage)event.trackedEntity().get(ChestedHorseStorage.class);
         boolean b = (Boolean)data.getValue();
         storage.setChested(b);
         event.cancel();
      });
      this.filter().type(EntityType.LLAMA).handler((event, data) -> {
         ChestedHorseStorage storage = (ChestedHorseStorage)event.trackedEntity().get(ChestedHorseStorage.class);
         int index = event.index();
         switch (index) {
            case 16:
               storage.setLiamaStrength((Integer)data.getValue());
               event.cancel();
               break;
            case 17:
               storage.setLiamaCarpetColor((Integer)data.getValue());
               event.cancel();
               break;
            case 18:
               storage.setLiamaVariant((Integer)data.getValue());
               event.cancel();
         }
      });
      this.filter().type(EntityType.ABSTRACT_HORSE).index(14).toIndex(16);
      this.filter().type(EntityType.VILLAGER).index(13).handler((event, data) -> {
         if ((Integer)data.getValue() == 5) {
            data.setValue(0);
         }
      });
      this.filter().type(EntityType.SHULKER).cancel(15);
   }

   private EntityData getSkeletonTypeData(int type) {
      return new EntityData(12, EntityDataTypes1_9.VAR_INT, type);
   }

   private EntityData getZombieTypeData(int type) {
      return new EntityData(13, EntityDataTypes1_9.VAR_INT, type);
   }

   private void handleZombieType(WrappedEntityData storage, int type) {
      EntityData meta = storage.get(13);
      if (meta == null) {
         storage.add(this.getZombieTypeData(type));
      }
   }

   private EntityData getHorseDataType(int type) {
      return new EntityData(14, EntityDataTypes1_9.VAR_INT, type);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int typeId) {
      return EntityType.findById(typeId);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int typeId, int data) {
      return ObjectType.getEntityType(typeId, data);
   }
}
