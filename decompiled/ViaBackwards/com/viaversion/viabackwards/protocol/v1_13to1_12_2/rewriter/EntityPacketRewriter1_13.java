package com.viaversion.viabackwards.protocol.v1_13to1_12_2.rewriter;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.entities.storage.EntityPositionHandler;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.data.EntityIdMappings1_12_2;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.data.PaintingNames1_13;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.data.ParticleIdMappings1_12_2;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.storage.PlayerPositionStorage1_13;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.storage.ProtocolStorables1_13;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13.ObjectType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_12;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ServerboundPackets1_12_1;

public class EntityPacketRewriter1_13 extends LegacyEntityRewriter<ClientboundPackets1_13, Protocol1_13To1_12_2> {
   public EntityPacketRewriter1_13(Protocol1_13To1_12_2 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_13.PLAYER_POSITION, new PacketHandlers() {
         public void register() {
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               if (ViaBackwards.getConfig().isFix1_13FacePlayer()) {
                  ProtocolStorables1_13 storables = (ProtocolStorables1_13)wrapper.user().storables(EntityPacketRewriter1_13.this.protocol);
                  PlayerPositionStorage1_13 playerStorage = storables.playerPositionStorage();
                  byte bitField = (Byte)wrapper.get(Types.BYTE, 0);
                  playerStorage.setX(EntityPacketRewriter1_13.toSet(bitField, 0, playerStorage.x(), (Double)wrapper.get(Types.DOUBLE, 0)));
                  playerStorage.setY(EntityPacketRewriter1_13.toSet(bitField, 1, playerStorage.y(), (Double)wrapper.get(Types.DOUBLE, 1)));
                  playerStorage.setZ(EntityPacketRewriter1_13.toSet(bitField, 2, playerStorage.z(), (Double)wrapper.get(Types.DOUBLE, 2)));
               }
            });
         }
      });
      ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
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
            this.handler(EntityPacketRewriter1_13.this.getObjectTrackerHandler());
            this.handler(wrapper -> {
               int id = (Byte)wrapper.get(Types.BYTE, 0);
               int data = (Integer)wrapper.get(Types.INT, 0);
               ObjectType type = ObjectType.findById(id, data);
               if (type == ObjectType.FALLING_BLOCK) {
                  int combined = Protocol1_13To1_12_2.MAPPINGS.getNewBlockStateId(data);
                  combined = combined >> 4 & 4095 | (combined & 15) << 12;
                  wrapper.set(Types.INT, 0, combined);
               } else if (type == ObjectType.ITEM_FRAME) {
                  data = switch (data) {
                     case 3 -> 0;
                     case 4 -> 1;
                     case 5 -> 3;
                     default -> data;
                  };
                  wrapper.set(Types.INT, 0, data);
               } else if (type == ObjectType.TRIDENT) {
                  wrapper.set(Types.BYTE, 0, (byte)ObjectType.TIPPED_ARROW.getId());
               }
            });
         }
      });
      this.registerTracker(ClientboundPackets1_13.ADD_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
      this.registerTracker(ClientboundPackets1_13.ADD_GLOBAL_ENTITY, EntityType.LIGHTNING_BOLT);
      ((Protocol1_13To1_12_2)this.protocol)
         .registerClientbound(
            ClientboundPackets1_13.ADD_MOB,
            new PacketHandlers() {
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
                  this.map(Types1_13.ENTITY_DATA_LIST, Types.ENTITY_DATA_LIST1_12);
                  this.handler(
                     wrapper -> {
                        int type = (Integer)wrapper.get(Types.VAR_INT, 1);
                        com.viaversion.viaversion.api.minecraft.entities.EntityType entityType = EntityType.findById(type);
                        if (entityType != null) {
                           EntityPacketRewriter1_13.this.tracker(wrapper.user()).addEntity((Integer)wrapper.get(Types.VAR_INT, 0), entityType);
                           int oldId = EntityIdMappings1_12_2.getOldId(type);
                           if (oldId == -1) {
                              if (!EntityPacketRewriter1_13.this.hasData(entityType)) {
                                 ((Protocol1_13To1_12_2)EntityPacketRewriter1_13.this.protocol)
                                    .getLogger()
                                    .warning("Could not find entity type mapping " + type + "/" + entityType);
                              }
                           } else {
                              wrapper.set(Types.VAR_INT, 1, oldId);
                           }
                        }
                     }
                  );
                  this.handler(EntityPacketRewriter1_13.this.getMobSpawnRewriter1_11(Types.ENTITY_DATA_LIST1_12));
               }
            }
         );
      ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types1_13.ENTITY_DATA_LIST, Types.ENTITY_DATA_LIST1_12);
            this.handler(EntityPacketRewriter1_13.this.getTrackerAndDataHandler(Types.ENTITY_DATA_LIST1_12, EntityType.PLAYER));
         }
      });
      ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_13.ADD_PAINTING, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.handler(wrapper -> {
               int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
               EntityPacketRewriter1_13.this.tracker(wrapper.user()).addEntity(entityId, EntityType.PAINTING);
               int motive = (Integer)wrapper.read(Types.VAR_INT);
               String title = PaintingNames1_13.getStringId(motive);
               wrapper.write(Types.STRING, title);
            });
         }
      });
      this.registerJoinGame(ClientboundPackets1_13.LOGIN, EntityType.PLAYER);
      ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_13.RESPAWN, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.handler(wrapper -> {
               ProtocolStorables1_13 storables = (ProtocolStorables1_13)wrapper.user().storables(EntityPacketRewriter1_13.this.protocol);
               ClientWorld clientWorld = storables.clientWorld();
               int dimensionId = (Integer)wrapper.get(Types.INT, 0);
               if (clientWorld.setEnvironment(dimensionId)) {
                  EntityPacketRewriter1_13.this.tracker(wrapper.user()).clearEntities();
                  storables.backwardsBlockStorage().clear();
                  storables.noteBlockStorage().clear();
               }
            });
         }
      });
      this.registerSetEntityData(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13.ENTITY_DATA_LIST, Types.ENTITY_DATA_LIST1_12);
      ((Protocol1_13To1_12_2)this.protocol)
         .registerClientbound(
            ClientboundPackets1_13.PLAYER_LOOK_AT,
            null,
            wrapper -> {
               wrapper.cancel();
               if (ViaBackwards.getConfig().isFix1_13FacePlayer()) {
                  int anchor = (Integer)wrapper.read(Types.VAR_INT);
                  double x = (Double)wrapper.read(Types.DOUBLE);
                  double y = (Double)wrapper.read(Types.DOUBLE);
                  double z = (Double)wrapper.read(Types.DOUBLE);
                  ProtocolStorables1_13 storables = (ProtocolStorables1_13)wrapper.user().storables(this.protocol);
                  PlayerPositionStorage1_13 positionStorage = storables.playerPositionStorage();
                  PacketWrapper positionAndLook = wrapper.create(ClientboundPackets1_12_1.PLAYER_POSITION);
                  positionAndLook.write(Types.DOUBLE, 0.0);
                  positionAndLook.write(Types.DOUBLE, 0.0);
                  positionAndLook.write(Types.DOUBLE, 0.0);
                  EntityPositionHandler.writeFacingDegrees(
                     positionAndLook, positionStorage.x(), anchor == 1 ? positionStorage.y() + 1.62 : positionStorage.y(), positionStorage.z(), x, y, z
                  );
                  positionAndLook.write(Types.BYTE, (byte)7);
                  positionAndLook.write(Types.VAR_INT, -1);
                  positionAndLook.send(Protocol1_13To1_12_2.class);
               }
            }
         );
      PacketHandler movementRemapper = wrapper -> {
         if (ViaBackwards.getConfig().isFix1_13FacePlayer()) {
            ProtocolStorables1_13 storables = (ProtocolStorables1_13)wrapper.user().storables(this.protocol);
            storables.playerPositionStorage().setPosFromPacket(wrapper);
         }
      };
      ((Protocol1_13To1_12_2)this.protocol).registerServerbound(ServerboundPackets1_12_1.MOVE_PLAYER_POS, movementRemapper);
      ((Protocol1_13To1_12_2)this.protocol).registerServerbound(ServerboundPackets1_12_1.MOVE_PLAYER_POS_ROT, movementRemapper);
      ((Protocol1_13To1_12_2)this.protocol).registerServerbound(ServerboundPackets1_12_1.MOVE_VEHICLE, movementRemapper);
   }

   protected void registerRewrites() {
      this.mapEntityTypeWithData(EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER).plainName();
      this.mapEntityTypeWithData(EntityType.COD, EntityType.SQUID).plainName();
      this.mapEntityTypeWithData(EntityType.SALMON, EntityType.SQUID).plainName();
      this.mapEntityTypeWithData(EntityType.PUFFERFISH, EntityType.SQUID).plainName();
      this.mapEntityTypeWithData(EntityType.TROPICAL_FISH, EntityType.SQUID).plainName();
      this.mapEntityTypeWithData(EntityType.PHANTOM, EntityType.PARROT)
         .plainName()
         .spawnEntityData(storage -> storage.add(new EntityData(15, EntityDataTypes1_12.VAR_INT, 3)));
      this.mapEntityTypeWithData(EntityType.DOLPHIN, EntityType.SQUID).plainName();
      this.mapEntityTypeWithData(EntityType.TURTLE, EntityType.OCELOT).plainName();
      this.filter().handler((event, data) -> {
         int typeId = data.dataType().typeId();
         if (typeId == 4) {
            JsonElement element = (JsonElement)data.value();
            ((Protocol1_13To1_12_2)this.protocol).translatableRewriter().processText(event.user(), element);
            data.setDataType(EntityDataTypes1_12.COMPONENT);
         } else if (typeId == 5) {
            JsonElement element = (JsonElement)data.value();
            data.setTypeAndValue(EntityDataTypes1_12.STRING, ((Protocol1_13To1_12_2)this.protocol).jsonToLegacy(event.user(), element));
         } else if (typeId == 6) {
            Item item = (Item)data.getValue();
            data.setTypeAndValue(EntityDataTypes1_12.ITEM, ((Protocol1_13To1_12_2)this.protocol).getItemRewriter().handleItemToClient(event.user(), item));
         } else if (typeId == 15) {
            event.cancel();
         } else {
            data.setDataType(EntityDataTypes1_12.byId(typeId > 5 ? typeId - 1 : typeId));
         }
      });
      this.filter().type(EntityType.ZOMBIE).removeIndex(15);
      this.filter().type(EntityType.TURTLE).cancel(13);
      this.filter().type(EntityType.TURTLE).cancel(14);
      this.filter().type(EntityType.TURTLE).cancel(15);
      this.filter().type(EntityType.TURTLE).cancel(16);
      this.filter().type(EntityType.TURTLE).cancel(17);
      this.filter().type(EntityType.TURTLE).cancel(18);
      this.filter().type(EntityType.ABSTRACT_FISH).cancel(12);
      this.filter().type(EntityType.ABSTRACT_FISH).cancel(13);
      this.filter().type(EntityType.PHANTOM).cancel(12);
      this.filter().type(EntityType.BOAT).cancel(12);
      this.filter().type(EntityType.TRIDENT).cancel(7);
      this.filter().type(EntityType.WOLF).index(17).handler((event, data) -> data.setValue(15 - (Integer)data.getValue()));
      this.filter().type(EntityType.AREA_EFFECT_CLOUD).index(9).handler((event, data) -> {
         Particle particle = (Particle)data.getValue();
         ParticleIdMappings1_12_2.ParticleData particleData = ParticleIdMappings1_12_2.getMapping(particle.id());
         int firstArg = 0;
         int secondArg = 0;
         int[] particleArgs = particleData.rewriteMeta((Protocol1_13To1_12_2)this.protocol, particle.getArguments());
         if (particleArgs != null && particleArgs.length != 0) {
            if (particleData.getHandler().isBlockHandler() && particleArgs[0] == 0) {
               particleArgs[0] = 102;
            }

            firstArg = particleArgs[0];
            secondArg = particleArgs.length == 2 ? particleArgs[1] : 0;
         }

         event.createExtraData(new EntityData(9, EntityDataTypes1_12.VAR_INT, particleData.getHistoryId()));
         event.createExtraData(new EntityData(10, EntityDataTypes1_12.VAR_INT, firstArg));
         event.createExtraData(new EntityData(11, EntityDataTypes1_12.VAR_INT, secondArg));
         event.cancel();
      });
      this.filter().dataType(EntityDataTypes1_12.OPTIONAL_BLOCK_STATE).handler((event, data) -> {
         int blockState = (Integer)data.value();
         if (blockState != 0) {
            int combined = Protocol1_13To1_12_2.MAPPINGS.getNewBlockStateId(blockState);
            data.setValue(combined >> 4 & 4095 | (combined & 15) << 12);
         }
      });
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int typeId) {
      return EntityType.findById(typeId);
   }

   public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int typeId, int data) {
      return ObjectType.getEntityType(typeId, data);
   }

   public int newEntityId(int newId) {
      return EntityIdMappings1_12_2.getOldId(newId);
   }

   private static double toSet(int field, int bitIndex, double origin, double packetValue) {
      return (field & 1 << bitIndex) != 0 ? origin + packetValue : packetValue;
   }
}
