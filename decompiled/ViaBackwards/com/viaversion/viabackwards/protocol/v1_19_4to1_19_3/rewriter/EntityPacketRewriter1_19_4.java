package com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.entities.storage.EntityReplacement;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.storage.EntityTracker1_19_4;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.storage.LinkedEntityStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_3;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.utils.TextUtils;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_19_4 extends EntityRewriter<ClientboundPackets1_19_4, Protocol1_19_4To1_19_3> {
   private static final double TEXT_DISPLAY_LINE_HEIGHT = 0.25;

   public EntityPacketRewriter1_19_4(Protocol1_19_4To1_19_3 protocol) {
      super(protocol, Types1_19_3.ENTITY_DATA_TYPES.optionalComponentType, Types1_19_3.ENTITY_DATA_TYPES.booleanType);
   }

   public void registerPackets() {
      this.registerSetEntityData(ClientboundPackets1_19_4.SET_ENTITY_DATA, Types1_19_4.ENTITY_DATA_LIST, Types1_19_3.ENTITY_DATA_LIST);
      ((Protocol1_19_4To1_19_3)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_19_4.ADD_ENTITY,
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
                  this.map(Types.VAR_INT);
                  this.handler(
                     wrapper -> {
                        int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
                        int entityType = (Integer)wrapper.get(Types.VAR_INT, 1);
                        if (ViaBackwards.getConfig().mapDisplayEntities()
                           || entityType != EntityTypes1_19_4.BLOCK_DISPLAY.getId()
                              && entityType != EntityTypes1_19_4.ITEM_DISPLAY.getId()
                              && entityType != EntityTypes1_19_4.TEXT_DISPLAY.getId()) {
                           double y = (Double)wrapper.get(Types.DOUBLE, 1);
                           if (entityType == EntityTypes1_19_4.TEXT_DISPLAY.getId()) {
                              wrapper.set(Types.DOUBLE, 1, y - 0.25);
                           }

                           TrackedEntity entity = EntityPacketRewriter1_19_4.this.trackAndRewrite(wrapper, entityType, entityId);
                           if (entity.entityType() == EntityTypes1_19_4.FALLING_BLOCK) {
                              int blockState = (Integer)wrapper.get(Types.VAR_INT, 2);
                              wrapper.set(
                                 Types.VAR_INT,
                                 2,
                                 ((Protocol1_19_4To1_19_3)EntityPacketRewriter1_19_4.this.protocol).getMappingData().getNewBlockStateId(blockState)
                              );
                           }

                           LinkedEntityStorage storage = new LinkedEntityStorage();
                           double x = (Double)wrapper.get(Types.DOUBLE, 0);
                           double z = (Double)wrapper.get(Types.DOUBLE, 2);
                           storage.setPosition(x, y, z);
                           entity.put(storage);
                        } else {
                           wrapper.cancel();
                        }
                     }
                  );
               }
            }
         );
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.ADD_PLAYER, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.UUID);
         double x = (Double)wrapper.passthrough(Types.DOUBLE);
         double y = (Double)wrapper.passthrough(Types.DOUBLE);
         double z = (Double)wrapper.passthrough(Types.DOUBLE);
         EntityTracker1_19_4 tracker = (EntityTracker1_19_4)this.tracker(wrapper.user());
         TrackedEntity entity = tracker.addEntity(entityId, EntityTypes1_19_4.PLAYER);
         LinkedEntityStorage storage = new LinkedEntityStorage();
         storage.setPosition(x, y, z);
         entity.put(storage);
      });
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.LOGIN, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.STRING_ARRAY);
            this.map(Types.NAMED_COMPOUND_TAG);
            this.map(Types.STRING);
            this.map(Types.STRING);
            this.handler(EntityPacketRewriter1_19_4.this.dimensionDataHandler());
            this.handler(EntityPacketRewriter1_19_4.this.biomeSizeTracker());
            this.handler(EntityPacketRewriter1_19_4.this.worldDataTrackerHandlerByKey());
            this.handler(wrapper -> {
               CompoundTag registry = (CompoundTag)wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
               TagUtil.removeNamespaced(registry, "trim_pattern");
               TagUtil.removeNamespaced(registry, "trim_material");
               TagUtil.removeNamespaced(registry, "damage_type");

               for (CompoundTag biomeTag : TagUtil.getRegistryEntries(registry, "worldgen/biome")) {
                  CompoundTag biomeData = biomeTag.getCompoundTag("element");
                  NumberTag hasPrecipitation = biomeData.getNumberTag("has_precipitation");
                  biomeData.putString("precipitation", hasPrecipitation.asByte() == 1 ? "rain" : "none");
               }
            });
         }
      });
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.PLAYER_POSITION, new PacketHandlers() {
         protected void register() {
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.BYTE);
            this.map(Types.VAR_INT);
            this.create(Types.BOOLEAN, false);
         }
      });
      ((Protocol1_19_4To1_19_3)this.protocol)
         .registerClientbound(ClientboundPackets1_19_4.DAMAGE_EVENT, ClientboundPackets1_19_3.ENTITY_EVENT, new PacketHandlers() {
            public void register() {
               this.map(Types.VAR_INT, Types.INT);
               this.read(Types.VAR_INT);
               this.read(Types.OPTIONAL_VAR_INT);
               this.read(Types.OPTIONAL_VAR_INT);
               this.handler(wrapper -> {
                  if ((Boolean)wrapper.read(Types.BOOLEAN)) {
                     wrapper.read(Types.DOUBLE);
                     wrapper.read(Types.DOUBLE);
                     wrapper.read(Types.DOUBLE);
                  }
               });
               this.create(Types.BYTE, (byte)2);
            }
         });
      ((Protocol1_19_4To1_19_3)this.protocol)
         .registerClientbound(ClientboundPackets1_19_4.HURT_ANIMATION, ClientboundPackets1_19_3.ANIMATE, new PacketHandlers() {
            public void register() {
               this.map(Types.VAR_INT);
               this.read(Types.FLOAT);
               this.create(Types.UNSIGNED_BYTE, (short)1);
            }
         });
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.RESPAWN, new PacketHandlers() {
         public void register() {
            this.map(Types.STRING);
            this.map(Types.STRING);
            this.handler(EntityPacketRewriter1_19_4.this.worldDataTrackerHandlerByKey());
         }
      });
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.UPDATE_MOB_EFFECT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BYTE);
         int duration = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, duration == -1 ? 999999 : duration);
      });
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.TELEPORT_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         double x = (Double)wrapper.passthrough(Types.DOUBLE);
         double y = (Double)wrapper.passthrough(Types.DOUBLE);
         double z = (Double)wrapper.passthrough(Types.DOUBLE);
         EntityTracker1_19_4 tracker = (EntityTracker1_19_4)this.tracker(wrapper.user());
         TrackedEntity entity = tracker.entity(entityId);
         if (entity != null) {
            boolean textDisplay = entity.entityType() == EntityTypes1_19_4.TEXT_DISPLAY;
            if (textDisplay) {
               wrapper.set(Types.DOUBLE, 1, y - 0.25);
            }

            LinkedEntityStorage storage = (LinkedEntityStorage)entity.get(LinkedEntityStorage.class);
            if (storage != null) {
               double deltaX = x - storage.x();
               double deltaY = y - storage.y();
               double deltaZ = z - storage.z();
               storage.setPosition(x, y, z);
               if (textDisplay) {
                  this.teleportTextDisplayLines(wrapper.user(), storage);
               }

               int[] passengers = storage.passengers();
               if (passengers != null) {
                  for (int passenger : passengers) {
                     LinkedEntityStorage passengerStorage = this.riddenTextDisplayStorage(tracker, passenger, entityId);
                     if (passengerStorage != null) {
                        passengerStorage.addRelativePosition(deltaX, deltaY, deltaZ);
                        this.sendTeleport(wrapper.user(), passenger, passengerStorage.x(), passengerStorage.y() - 0.25, passengerStorage.z());
                        this.teleportTextDisplayLines(wrapper.user(), passengerStorage);
                     }
                  }
               }
            }
         }
      });
      PacketHandler entityPositionHandler = wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         EntityTracker1_19_4 tracker = (EntityTracker1_19_4)this.tracker(wrapper.user());
         TrackedEntity entity = tracker.entity(entityId);
         if (entity != null) {
            LinkedEntityStorage storage = (LinkedEntityStorage)entity.get(LinkedEntityStorage.class);
            if (storage != null) {
               short compressedX = (Short)wrapper.passthrough(Types.SHORT);
               short compressedY = (Short)wrapper.passthrough(Types.SHORT);
               short compressedZ = (Short)wrapper.passthrough(Types.SHORT);
               double deltaX = compressedX / 4096.0;
               double deltaY = compressedY / 4096.0;
               double deltaZ = compressedZ / 4096.0;
               storage.addRelativePosition(deltaX, deltaY, deltaZ);
               if (entity.entityType() == EntityTypes1_19_4.TEXT_DISPLAY) {
                  this.moveTextDisplayLines(wrapper.user(), storage, compressedX, compressedY, compressedZ);
               }

               int[] passengers = storage.passengers();
               if (passengers != null) {
                  for (int passenger : passengers) {
                     LinkedEntityStorage passengerStorage = this.riddenTextDisplayStorage(tracker, passenger, entityId);
                     if (passengerStorage != null) {
                        passengerStorage.addRelativePosition(deltaX, deltaY, deltaZ);
                        this.sendRelativeMove(wrapper.user(), passenger, compressedX, compressedY, compressedZ);
                        this.moveTextDisplayLines(wrapper.user(), passengerStorage, compressedX, compressedY, compressedZ);
                     }
                  }
               }
            }
         }
      };
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.MOVE_ENTITY_POS, entityPositionHandler);
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.MOVE_ENTITY_POS_ROT, entityPositionHandler);
      ((Protocol1_19_4To1_19_3)this.protocol).registerClientbound(ClientboundPackets1_19_4.SET_PASSENGERS, wrapper -> {
         int vehicleId = (Integer)wrapper.passthrough(Types.VAR_INT);
         int[] passengers = (int[])wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
         EntityTracker1_19_4 tracker = (EntityTracker1_19_4)this.tracker(wrapper.user());
         LinkedEntityStorage vehicleStorage = tracker.linkedEntityStorage(vehicleId);
         if (vehicleStorage == null) {
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, passengers);
         } else {
            if (vehicleStorage.passengers() != null) {
               for (int previousPassenger : vehicleStorage.passengers()) {
                  LinkedEntityStorage passengerStorage = tracker.linkedEntityStorage(previousPassenger);
                  if (passengerStorage != null && passengerStorage.isVehicle(vehicleId)) {
                     passengerStorage.setVehicleId(null);
                  }
               }
            }

            boolean hasTextDisplay = false;
            IntList filteredPassengers = new IntArrayList(passengers.length);

            for (int passenger : passengers) {
               if (tracker.entityType(passenger) != EntityTypes1_19_4.TEXT_DISPLAY) {
                  filteredPassengers.add(passenger);
               } else {
                  hasTextDisplay = true;
                  LinkedEntityStorage passengerStorage = tracker.linkedEntityStorage(passenger);
                  if (passengerStorage == null) {
                     filteredPassengers.add(passenger);
                  } else {
                     passengerStorage.setVehicleId(vehicleId);
                     if (passengerStorage.entities() == null) {
                        filteredPassengers.add(passenger);
                     } else {
                        double distanceX = passengerStorage.x() - vehicleStorage.x();
                        double distanceY = passengerStorage.y() - vehicleStorage.y();
                        double distanceZ = passengerStorage.z() - vehicleStorage.z();
                        if (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ > 16.0) {
                           passengerStorage.setPosition(vehicleStorage.x(), vehicleStorage.y(), vehicleStorage.z());
                           this.sendTeleport(wrapper.user(), passenger, passengerStorage.x(), passengerStorage.y() - 0.25, passengerStorage.z());
                           this.teleportTextDisplayLines(wrapper.user(), passengerStorage);
                        }
                     }
                  }
               }
            }

            vehicleStorage.setPassengers(hasTextDisplay ? passengers : null);
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, filteredPassengers.size() == passengers.length ? passengers : filteredPassengers.toIntArray());
         }
      });
   }

   public void registerRewrites() {
      this.filter().handler((event, data) -> {
         int id = data.dataType().typeId();
         if (id >= 25) {
            event.cancel();
         } else {
            if (id >= 15) {
               id--;
            }

            data.setDataType(Types1_19_3.ENTITY_DATA_TYPES.byId(id));
         }
      });
      this.registerEntityDataTypeHandler(
         Types1_19_3.ENTITY_DATA_TYPES.itemType,
         null,
         Types1_19_3.ENTITY_DATA_TYPES.optionalBlockStateType,
         Types1_19_3.ENTITY_DATA_TYPES.particleType,
         Types1_19_3.ENTITY_DATA_TYPES.componentType,
         Types1_19_3.ENTITY_DATA_TYPES.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_19_4.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_19_4.BOAT).index(11).handler((event, data) -> {
         int boatType = (Integer)data.value();
         if (boatType > 4) {
            data.setValue(boatType - 1);
         }
      });
      this.filter().type(EntityTypes1_19_4.TEXT_DISPLAY).index(22).handler((event, data) -> {
         event.setIndex(2);
         data.setDataType(Types1_19_3.ENTITY_DATA_TYPES.optionalComponentType);
         event.createExtraData(new EntityData(3, Types1_19_3.ENTITY_DATA_TYPES.booleanType, true));
         EntityTracker1_19_4 tracker = (EntityTracker1_19_4)this.tracker(event.user());
         JsonElement component = (JsonElement)data.value();
         if (!containsNewLine(component)) {
            this.clearTextDisplayLines(event.user(), tracker, event.entityId());
         } else {
            TextComponent[] lines = TextUtils.split(SerializerVersion.V1_19_4.toComponent(component), "\n", false);
            if (lines.length <= 1) {
               this.clearTextDisplayLines(event.user(), tracker, event.entityId());
            } else {
               data.setValue(SerializerVersion.V1_19_4.toJson(lines[lines.length - 1]));
               LinkedEntityStorage storage = tracker.linkedEntityStorage(event.entityId());
               if (storage != null) {
                  int extraLines = lines.length - 1;
                  int[] entities = storage.entities();
                  if (entities == null || entities.length != extraLines) {
                     boolean mountedOnClient = entities == null && storage.vehicleId() != null;
                     tracker.clearLinkedEntities(storage);
                     LinkedEntityStorage vehicleStorage = mountedOnClient ? tracker.linkedEntityStorage(storage.vehicleId()) : null;
                     if (vehicleStorage != null) {
                        storage.setPosition(vehicleStorage.x(), vehicleStorage.y(), vehicleStorage.z());
                     }

                     entities = new int[extraLines];

                     for (int i = 0; i < extraLines; i++) {
                        double y = storage.y() + 0.25 * i;
                        entities[i] = tracker.spawnEntity(EntityTypes1_19_3.ARMOR_STAND, storage.x(), y, storage.z(), 0);
                     }

                     storage.setEntities(entities);
                     if (vehicleStorage != null) {
                        this.sendVehiclePassengers(event.user(), tracker, storage.vehicleId());
                        this.sendTeleport(event.user(), event.entityId(), storage.x(), storage.y() - 0.25, storage.z());
                     }
                  }

                  for (int i = 0; i < extraLines; i++) {
                     this.sendLineData(event.user(), entities[i], SerializerVersion.V1_19_4.toJson(lines[extraLines - 1 - i]));
                  }
               }
            }
         }
      });
      this.filter().type(EntityTypes1_19_4.BLOCK_DISPLAY).index(22).handler((event, data) -> {
         int value = (Integer)data.value();
         EntityTracker1_19_4 tracker = (EntityTracker1_19_4)this.tracker(event.user());
         LinkedEntityStorage storage = (LinkedEntityStorage)event.trackedEntity().get(LinkedEntityStorage.class);
         if (storage != null) {
            tracker.clearLinkedEntities(storage);
            int linkedEntity = tracker.spawnEntity(EntityTypes1_19_3.FALLING_BLOCK, storage.x(), storage.y(), storage.z(), value);
            storage.setEntities(linkedEntity);
            PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_19_3.SET_PASSENGERS, event.user());
            wrapper.write(Types.VAR_INT, event.entityId());
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{linkedEntity});
            wrapper.send(Protocol1_19_4To1_19_3.class);
         }
      });
      this.filter().type(EntityTypes1_19_4.ITEM_DISPLAY).index(22).handler((event, data) -> {
         Item value = (Item)data.value();
         PacketWrapper setEquipment = PacketWrapper.create(ClientboundPackets1_19_3.SET_EQUIPMENT, event.user());
         setEquipment.write(Types.VAR_INT, event.entityId());
         setEquipment.write(Types.BYTE, (byte)5);
         setEquipment.write(Types.ITEM1_13_2, value);
         setEquipment.send(Protocol1_19_4To1_19_3.class);
      });
      this.filter().type(EntityTypes1_19_4.DISPLAY).handler((event, data) -> {
         if (event.index() > 7) {
            event.cancel();
         }
      });
      this.filter().type(EntityTypes1_19_4.INTERACTION).cancel(8);
      this.filter().type(EntityTypes1_19_4.INTERACTION).cancel(9);
      this.filter().type(EntityTypes1_19_4.INTERACTION).cancel(10);
      this.filter().type(EntityTypes1_19_4.SNIFFER).cancel(17);
      this.filter().type(EntityTypes1_19_4.SNIFFER).cancel(18);
      this.filter().type(EntityTypes1_19_4.ABSTRACT_HORSE).addIndex(18);
   }

   private void clearTextDisplayLines(UserConnection connection, EntityTracker1_19_4 tracker, int entityId) {
      LinkedEntityStorage storage = tracker.linkedEntityStorage(entityId);
      if (storage != null && storage.entities() != null) {
         tracker.clearLinkedEntities(storage);
         this.sendVehiclePassengers(connection, tracker, storage.vehicleId());
      }
   }

   private LinkedEntityStorage riddenTextDisplayStorage(EntityTracker1_19_4 tracker, int entityId, int vehicleId) {
      TrackedEntity entity = tracker.entity(entityId);
      if (entity != null && entity.entityType() == EntityTypes1_19_4.TEXT_DISPLAY) {
         LinkedEntityStorage storage = (LinkedEntityStorage)entity.get(LinkedEntityStorage.class);
         return storage != null && storage.entities() != null && storage.isVehicle(vehicleId) ? storage : null;
      } else {
         return null;
      }
   }

   private boolean isMultiLineTextDisplay(EntityTracker1_19_4 tracker, int entityId) {
      if (tracker.entityType(entityId) != EntityTypes1_19_4.TEXT_DISPLAY) {
         return false;
      } else {
         LinkedEntityStorage storage = tracker.linkedEntityStorage(entityId);
         return storage != null && storage.entities() != null;
      }
   }

   private int[] withoutMultiLineTextDisplays(EntityTracker1_19_4 tracker, int[] passengers) {
      IntList filtered = new IntArrayList(passengers.length);

      for (int passenger : passengers) {
         if (!this.isMultiLineTextDisplay(tracker, passenger)) {
            filtered.add(passenger);
         }
      }

      return filtered.size() == passengers.length ? passengers : filtered.toIntArray();
   }

   private void sendVehiclePassengers(UserConnection connection, EntityTracker1_19_4 tracker, @Nullable Integer vehicleId) {
      if (vehicleId != null) {
         LinkedEntityStorage vehicleStorage = tracker.linkedEntityStorage(vehicleId);
         if (vehicleStorage != null && vehicleStorage.passengers() != null) {
            PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_19_3.SET_PASSENGERS, connection);
            wrapper.write(Types.VAR_INT, vehicleId);
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, this.withoutMultiLineTextDisplays(tracker, vehicleStorage.passengers()));
            wrapper.send(Protocol1_19_4To1_19_3.class);
         }
      }
   }

   private void teleportTextDisplayLines(UserConnection connection, LinkedEntityStorage storage) {
      int[] entities = storage.entities();
      if (entities != null) {
         for (int i = 0; i < entities.length; i++) {
            this.sendTeleport(connection, entities[i], storage.x(), storage.y() + 0.25 * i, storage.z());
         }
      }
   }

   private void moveTextDisplayLines(UserConnection connection, LinkedEntityStorage storage, short deltaX, short deltaY, short deltaZ) {
      int[] entities = storage.entities();
      if (entities != null) {
         for (int entity : entities) {
            this.sendRelativeMove(connection, entity, deltaX, deltaY, deltaZ);
         }
      }
   }

   private void sendTeleport(UserConnection connection, int entityId, double x, double y, double z) {
      PacketWrapper teleport = PacketWrapper.create(ClientboundPackets1_19_3.TELEPORT_ENTITY, connection);
      teleport.write(Types.VAR_INT, entityId);
      teleport.write(Types.DOUBLE, x);
      teleport.write(Types.DOUBLE, y);
      teleport.write(Types.DOUBLE, z);
      teleport.write(Types.BYTE, (byte)0);
      teleport.write(Types.BYTE, (byte)0);
      teleport.write(Types.BOOLEAN, false);
      teleport.send(Protocol1_19_4To1_19_3.class);
   }

   private void sendRelativeMove(UserConnection connection, int entityId, short deltaX, short deltaY, short deltaZ) {
      PacketWrapper move = PacketWrapper.create(ClientboundPackets1_19_3.MOVE_ENTITY_POS, connection);
      move.write(Types.VAR_INT, entityId);
      move.write(Types.SHORT, deltaX);
      move.write(Types.SHORT, deltaY);
      move.write(Types.SHORT, deltaZ);
      move.write(Types.BOOLEAN, false);
      move.send(Protocol1_19_4To1_19_3.class);
   }

   private static boolean containsNewLine(JsonElement element) {
      if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         return primitive.isString() && primitive.getAsString().indexOf(10) != -1;
      } else {
         if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
               if (containsNewLine(entry)) {
                  return true;
               }
            }
         } else if (element.isJsonObject()) {
            for (Entry<String, JsonElement> entryx : element.getAsJsonObject().entrySet()) {
               if (containsNewLine(entryx.getValue())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void sendLineData(UserConnection connection, int entityId, JsonElement text) {
      List<EntityData> data = new ArrayList<>();
      data.add(new EntityData(0, Types1_19_3.ENTITY_DATA_TYPES.byteType, (byte)32));
      data.add(new EntityData(2, Types1_19_3.ENTITY_DATA_TYPES.optionalComponentType, text));
      data.add(new EntityData(3, Types1_19_3.ENTITY_DATA_TYPES.booleanType, true));
      data.add(new EntityData(5, Types1_19_3.ENTITY_DATA_TYPES.booleanType, true));
      data.add(new EntityData(15, Types1_19_3.ENTITY_DATA_TYPES.byteType, (byte)17));
      PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_19_3.SET_ENTITY_DATA, connection);
      wrapper.write(Types.VAR_INT, entityId);
      wrapper.write(Types1_19_3.ENTITY_DATA_LIST, data);
      wrapper.send(Protocol1_19_4To1_19_3.class);
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      EntityReplacement.EntityDataCreator displayDataCreator = storage -> {
         storage.add(new EntityData(0, Types1_19_3.ENTITY_DATA_TYPES.byteType, (byte)32));
         storage.add(new EntityData(5, Types1_19_3.ENTITY_DATA_TYPES.booleanType, true));
         storage.add(new EntityData(15, Types1_19_3.ENTITY_DATA_TYPES.byteType, (byte)17));
      };
      this.mapEntityTypeWithData(EntityTypes1_19_4.TEXT_DISPLAY, EntityTypes1_19_4.ARMOR_STAND).spawnEntityData(displayDataCreator);
      this.mapEntityTypeWithData(EntityTypes1_19_4.ITEM_DISPLAY, EntityTypes1_19_4.ARMOR_STAND).spawnEntityData(displayDataCreator);
      this.mapEntityTypeWithData(EntityTypes1_19_4.BLOCK_DISPLAY, EntityTypes1_19_4.ARMOR_STAND).spawnEntityData(displayDataCreator);
      this.mapEntityTypeWithData(EntityTypes1_19_4.INTERACTION, EntityTypes1_19_4.ARMOR_STAND).spawnEntityData(displayDataCreator);
      this.mapEntityTypeWithData(EntityTypes1_19_4.SNIFFER, EntityTypes1_19_4.RAVAGER).jsonName();
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_19_4.getTypeFromId(type);
   }
}
