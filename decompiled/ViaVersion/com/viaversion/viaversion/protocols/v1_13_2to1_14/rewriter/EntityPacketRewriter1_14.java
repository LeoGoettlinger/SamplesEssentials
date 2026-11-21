package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.ArrayList;
import java.util.List;

public class EntityPacketRewriter1_14 extends EntityRewriter<ClientboundPackets1_13, Protocol1_13_2To1_14> {
   public EntityPacketRewriter1_14(Protocol1_13_2To1_14 protocol) {
      super(protocol);
   }

   @Override
   protected void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_13.ADD_EXPERIENCE_ORB, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         this.<EntityTracker>tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.EXPERIENCE_ORB);
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.ADD_GLOBAL_ENTITY, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         if (wrapper.passthrough(Types.BYTE) == 1) {
            this.<EntityTracker>tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.LIGHTNING_BOLT);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.BYTE, Types.VAR_INT);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.INT);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.handler(wrapper -> {
               int entityId = wrapper.get(Types.VAR_INT, 0);
               int typeId = wrapper.get(Types.VAR_INT, 1);
               int data = wrapper.get(Types.INT, 0);
               EntityTypes1_13.EntityType type1_13 = EntityTypes1_13.ObjectType.getEntityType(typeId, data);
               if (type1_13 != null) {
                  typeId = EntityPacketRewriter1_14.this.newEntityId(type1_13.getId());
                  EntityType type1_14 = EntityTypes1_14.getTypeFromId(typeId);
                  if (type1_14.is(EntityTypes1_14.FALLING_BLOCK)) {
                     wrapper.set(Types.INT, 0, EntityPacketRewriter1_14.this.protocol.getMappingData().getNewBlockStateId(data));
                  } else if (type1_14.is(EntityTypes1_14.ITEM) && data > 0 || type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                     if (type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                        wrapper.set(Types.INT, 0, data - 1);
                     }

                     PacketWrapper velocity = wrapper.create(ClientboundPackets1_14.SET_ENTITY_MOTION);
                     velocity.write(Types.VAR_INT, entityId);
                     velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 0));
                     velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 1));
                     velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 2));
                     velocity.scheduleSend(Protocol1_13_2To1_14.class);
                  }

                  wrapper.user().<EntityTracker>getEntityTracker(EntityPacketRewriter1_14.this.protocol).addEntity(entityId, type1_14);
                  wrapper.set(Types.VAR_INT, 1, typeId);
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
            this.map(Types1_13_2.ENTITY_DATA_LIST, Types1_14.ENTITY_DATA_LIST);
            this.handler(wrapper -> {
               int entityType = wrapper.get(Types.VAR_INT, 1);
               if (EntityTypes1_13.EntityType.findById(entityType) == null) {
                  wrapper.cancel();
               } else {
                  EntityPacketRewriter1_14.this.trackerAndRewriterHandler(Types1_14.ENTITY_DATA_LIST).handle(wrapper);
               }
            });
         }
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_13.ADD_PAINTING,
            new PacketHandlers() {
               @Override
               public void register() {
                  this.map(Types.VAR_INT);
                  this.map(Types.UUID);
                  this.map(Types.VAR_INT);
                  this.map(Types.BLOCK_POSITION1_8, Types.BLOCK_POSITION1_14);
                  this.map(Types.BYTE);
                  this.handler(
                     wrapper -> EntityPacketRewriter1_14.this.<EntityTracker>tracker(wrapper.user())
                        .addEntity(wrapper.get(Types.VAR_INT, 0), EntityTypes1_14.PAINTING)
                  );
               }
            }
         );
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
            this.map(Types1_13_2.ENTITY_DATA_LIST, Types1_14.ENTITY_DATA_LIST);
            this.handler(EntityPacketRewriter1_14.this.trackerAndRewriterHandler(Types1_14.ENTITY_DATA_LIST, EntityTypes1_14.PLAYER));
         }
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_13.ANIMATE,
            new PacketHandlers() {
               @Override
               public void register() {
                  this.map(Types.VAR_INT);
                  this.handler(
                     wrapper -> {
                        short animation = wrapper.passthrough(Types.UNSIGNED_BYTE);
                        if (animation == 2) {
                           EntityTracker1_14 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_14.this.protocol);
                           int entityId = wrapper.get(Types.VAR_INT, 0);
                           tracker.setSleeping(entityId, false);
                           PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_14.SET_ENTITY_DATA);
                           entityDataPacket.write(Types.VAR_INT, entityId);
                           List<EntityData> entityDataList = new ArrayList<>();
                           if (tracker.clientEntityId() != entityId) {
                              entityDataList.add(
                                 new EntityData(6, Types1_14.ENTITY_DATA_TYPES.poseType, EntityPacketRewriter1_14.recalculatePlayerPose(entityId, tracker))
                              );
                           }

                           entityDataList.add(new EntityData(12, Types1_14.ENTITY_DATA_TYPES.optionalBlockPositionType, null));
                           entityDataPacket.write(Types1_14.ENTITY_DATA_LIST, entityDataList);
                           entityDataPacket.scheduleSend(Protocol1_13_2To1_14.class);
                        }
                     }
                  );
               }
            }
         );
      this.protocol.registerClientbound(ClientboundPackets1_13.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.INT);
            this.handler(wrapper -> {
               ClientWorld clientChunks = wrapper.user().<ProtocolStorables>storables(EntityPacketRewriter1_14.this.protocol).clientWorld();
               int dimensionId = wrapper.get(Types.INT, 1);
               clientChunks.setEnvironment(dimensionId);
            });
            this.handler(EntityPacketRewriter1_14.this.playerTrackerHandler());
            this.handler(wrapper -> {
               short difficulty = wrapper.read(Types.UNSIGNED_BYTE);
               PacketWrapper difficultyPacket = wrapper.create(ClientboundPackets1_14.CHANGE_DIFFICULTY);
               difficultyPacket.write(Types.UNSIGNED_BYTE, difficulty);
               difficultyPacket.write(Types.BOOLEAN, false);
               difficultyPacket.scheduleSend((Class<? extends Protocol>)EntityPacketRewriter1_14.this.protocol.getClass());
               wrapper.passthrough(Types.UNSIGNED_BYTE);
               wrapper.passthrough(Types.STRING);
               wrapper.write(Types.VAR_INT, 64);
            });
            this.handler(wrapper -> {
               wrapper.send(Protocol1_13_2To1_14.class);
               wrapper.cancel();
               WorldPacketRewriter1_14.sendViewDistancePacket(wrapper.user());
            });
         }
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_13.PLAYER_SLEEP,
            ClientboundPackets1_14.SET_ENTITY_DATA,
            new PacketHandlers() {
               @Override
               public void register() {
                  this.map(Types.VAR_INT);
                  this.handler(
                     wrapper -> {
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(EntityPacketRewriter1_14.this.protocol);
                        int entityId = wrapper.get(Types.VAR_INT, 0);
                        tracker.setSleeping(entityId, true);
                        BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_8);
                        List<EntityData> entityDataList = new ArrayList<>();
                        entityDataList.add(new EntityData(12, Types1_14.ENTITY_DATA_TYPES.optionalBlockPositionType, position));
                        if (tracker.clientEntityId() != entityId) {
                           entityDataList.add(
                              new EntityData(6, Types1_14.ENTITY_DATA_TYPES.poseType, EntityPacketRewriter1_14.recalculatePlayerPose(entityId, tracker))
                           );
                        }

                        wrapper.write(Types1_14.ENTITY_DATA_LIST, entityDataList);
                     }
                  );
               }
            }
         );
      this.registerSetEntityData(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13_2.ENTITY_DATA_LIST, Types1_14.ENTITY_DATA_LIST);
   }

   @Override
   protected void registerRewrites() {
      this.filter().mapDataType(Types1_14.ENTITY_DATA_TYPES::byId);
      this.registerEntityDataTypeHandler(
         Types1_14.ENTITY_DATA_TYPES.itemType, Types1_14.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_14.ENTITY_DATA_TYPES.particleType
      );
      this.filter().type(EntityTypes1_14.ENTITY).addIndex(6);
      this.registerBlockStateHandler(EntityTypes1_14.ABSTRACT_MINECART, 10);
      this.filter().type(EntityTypes1_14.LIVING_ENTITY).addIndex(12);
      this.filter().type(EntityTypes1_14.LIVING_ENTITY).index(8).handler((event, data) -> {
         float value = ((Number)data.getValue()).floatValue();
         if (Float.isNaN(value) && Via.getConfig().is1_14HealthNaNFix()) {
            data.setValue(1.0F);
         }
      });
      this.filter().type(EntityTypes1_14.MOB).index(13).handler((event, data) -> {
         EntityTracker1_14 tracker = this.tracker(event.user());
         int entityId = event.entityId();
         tracker.setInsentientData(entityId, (byte)(((Number)data.getValue()).byteValue() & -5 | tracker.getInsentientData(entityId) & 4));
         data.setValue(tracker.getInsentientData(entityId));
      });
      this.filter().type(EntityTypes1_14.PLAYER).handler((event, data) -> {
         EntityTracker1_14 tracker = this.tracker(event.user());
         int entityId = event.entityId();
         if (entityId != tracker.clientEntityId()) {
            if (data.id() == 0) {
               byte flags = ((Number)data.getValue()).byteValue();
               tracker.setEntityFlags(entityId, flags);
            } else if (data.id() == 7) {
               tracker.setRiptide(entityId, (((Number)data.getValue()).byteValue() & 4) != 0);
            }

            if (data.id() == 0 || data.id() == 7) {
               event.createExtraData(new EntityData(6, Types1_14.ENTITY_DATA_TYPES.poseType, recalculatePlayerPose(entityId, tracker)));
            }
         }
      });
      this.filter().type(EntityTypes1_14.ZOMBIE).handler((event, data) -> {
         if (data.id() == 16) {
            EntityTracker1_14 tracker = this.tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte)(tracker.getInsentientData(entityId) & -5 | (data.getValue() ? 4 : 0)));
            event.createExtraData(new EntityData(13, Types1_14.ENTITY_DATA_TYPES.byteType, tracker.getInsentientData(entityId)));
            event.cancel();
         } else if (data.id() > 16) {
            data.setId(data.id() - 1);
         }
      });
      this.filter().type(EntityTypes1_14.HORSE).index(18).handler((event, data) -> {
         event.cancel();
         int armorType = data.<Integer>value();
         Item armorItem = null;
         if (armorType == 1) {
            armorItem = new DataItem(this.protocol.getMappingData().getNewItemId(727), (byte)1, null);
         } else if (armorType == 2) {
            armorItem = new DataItem(this.protocol.getMappingData().getNewItemId(728), (byte)1, null);
         } else if (armorType == 3) {
            armorItem = new DataItem(this.protocol.getMappingData().getNewItemId(729), (byte)1, null);
         }

         PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_14.SET_EQUIPPED_ITEM, null, event.user());
         equipmentPacket.write(Types.VAR_INT, event.entityId());
         equipmentPacket.write(Types.VAR_INT, 4);
         equipmentPacket.write(Types.ITEM1_13_2, armorItem);

         try {
            equipmentPacket.scheduleSend(Protocol1_13_2To1_14.class);
         } catch (Exception var7) {
            throw new RuntimeException(var7);
         }
      });
      this.filter()
         .type(EntityTypes1_14.VILLAGER)
         .index(15)
         .handler(
            (event, data) -> data.setTypeAndValue(
               Types1_14.ENTITY_DATA_TYPES.villagerDataType, new VillagerData(2, getNewProfessionId(data.<Integer>value()), 0)
            )
         );
      this.filter()
         .type(EntityTypes1_14.ZOMBIE_VILLAGER)
         .index(18)
         .handler(
            (event, data) -> data.setTypeAndValue(
               Types1_14.ENTITY_DATA_TYPES.villagerDataType, new VillagerData(2, getNewProfessionId(data.<Integer>value()), 0)
            )
         );
      this.filter().type(EntityTypes1_14.ABSTRACT_ARROW).addIndex(9);
      this.filter().type(EntityTypes1_14.FIREWORK_ROCKET).index(8).handler((event, data) -> {
         data.setDataType(Types1_14.ENTITY_DATA_TYPES.optionalVarIntType);
         if (data.getValue().equals(0)) {
            data.setValue(null);
         }
      });
      this.filter().type(EntityTypes1_14.ABSTRACT_SKELETON).index(14).handler((event, data) -> {
         EntityTracker1_14 tracker = this.tracker(event.user());
         int entityId = event.entityId();
         tracker.setInsentientData(entityId, (byte)(tracker.getInsentientData(entityId) & -5 | (data.getValue() ? 4 : 0)));
         event.createExtraData(new EntityData(13, Types1_14.ENTITY_DATA_TYPES.byteType, tracker.getInsentientData(entityId)));
         event.cancel();
      });
      this.filter().type(EntityTypes1_14.ABSTRACT_ILLAGER).handler((event, data) -> {
         if (event.index() == 14) {
            EntityTracker1_14 tracker = this.tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte)(tracker.getInsentientData(entityId) & -5 | (((Number)data.getValue()).byteValue() != 0 ? 4 : 0)));
            event.createExtraData(new EntityData(13, Types1_14.ENTITY_DATA_TYPES.byteType, tracker.getInsentientData(entityId)));
            event.cancel();
         } else if (event.index() > 14) {
            data.setId(data.id() - 1);
         }
      });
      this.filter().type(EntityTypes1_14.OCELOT).removeIndex(17);
      this.filter().type(EntityTypes1_14.OCELOT).removeIndex(16);
      this.filter().type(EntityTypes1_14.OCELOT).removeIndex(15);
      this.filter().type(EntityTypes1_14.ABSTRACT_RAIDER).addIndex(14);
   }

   @Override
   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      if (Via.getConfig().translateOcelotToCat()) {
         this.mapEntityType(EntityTypes1_13.EntityType.OCELOT, EntityTypes1_14.CAT);
      }
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_14.getTypeFromId(type);
   }

   private static boolean isSneaking(byte flags) {
      return (flags & 2) != 0;
   }

   private static boolean isSwimming(byte flags) {
      return (flags & 16) != 0;
   }

   private static int getNewProfessionId(int old) {
      return switch (old) {
         case 0 -> 5;
         case 1 -> 9;
         case 2 -> 4;
         case 3 -> 1;
         case 4 -> 2;
         case 5 -> 11;
         default -> 0;
      };
   }

   private static boolean isFallFlying(int entityFlags) {
      return (entityFlags & 128) != 0;
   }

   public static int recalculatePlayerPose(int entityId, EntityTracker1_14 tracker) {
      byte flags = tracker.getEntityFlags(entityId);
      int pose = 0;
      if (isFallFlying(flags)) {
         pose = 1;
      } else if (tracker.isSleeping(entityId)) {
         pose = 2;
      } else if (isSwimming(flags)) {
         pose = 3;
      } else if (tracker.isRiptide(entityId)) {
         pose = 4;
      } else if (isSneaking(flags)) {
         pose = 5;
      }

      return pose;
   }
}
