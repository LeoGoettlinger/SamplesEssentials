package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ClientVehicleStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.EntityTracker1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.GroundFlagTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.PlayerPositionStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ProtocolStorables1_21_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class EntityPacketRewriter1_21_2 extends EntityRewriter<ClientboundPacket1_21, Protocol1_21To1_21_2> {
   private static final String[] GOAT_HORN_INSTRUMENTS = new String[]{
      "ponder_goat_horn", "sing_goat_horn", "seek_goat_horn", "feel_goat_horn", "admire_goat_horn", "call_goat_horn", "yearn_goat_horn", "dream_goat_horn"
   };
   private static final float IMPULSE = 0.98F;
   private final boolean isVF = Via.getPlatform().getPlatformName().equals("ViaFabric");

   public EntityPacketRewriter1_21_2(Protocol1_21To1_21_2 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.appendClientbound(ClientboundPackets1_21.ADD_ENTITY, wrapper -> {
         int entityType = wrapper.get(Types.VAR_INT, 1);
         EntityType type = this.typeFromId(entityType);
         if (type != null && type.isOrHasParent(EntityTypes1_21_2.ABSTRACT_BOAT)) {
            int entityId = wrapper.get(Types.VAR_INT, 0);
            UUID uuid = wrapper.get(Types.UUID, 0);
            double x = wrapper.get(Types.DOUBLE, 0);
            double y = wrapper.get(Types.DOUBLE, 1);
            double z = wrapper.get(Types.DOUBLE, 2);
            float pitch = wrapper.get(Types.BYTE, 0).byteValue() * 360.0F / 256.0F;
            float yaw = wrapper.get(Types.BYTE, 1).byteValue() * 360.0F / 256.0F;
            int data = wrapper.get(Types.VAR_INT, 2);
            EntityTracker1_21_2 tracker = this.tracker(wrapper.user());
            EntityTracker1_21_2.BoatEntity entity = tracker.trackBoatEntity(entityId, uuid, data);
            entity.setPosition(x, y, z);
            entity.setRotation(yaw, pitch);
         }
      });
      this.protocol.appendClientbound(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, wrapper -> {
         PacketWrapper instrumentsPacket = wrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA);
         instrumentsPacket.write(Types.STRING, "minecraft:instrument");
         RegistryEntry[] entries = new RegistryEntry[GOAT_HORN_INSTRUMENTS.length];

         for (int i = 0; i < GOAT_HORN_INSTRUMENTS.length; i++) {
            CompoundTag tag = new CompoundTag();
            tag.putString("sound_event", "item.goat_horn.sound." + i);
            tag.putFloat("use_duration", 7.0F);
            tag.putInt("range", 256);
            tag.putString("description", "");
            entries[i] = new RegistryEntry(GOAT_HORN_INSTRUMENTS[i], tag);
         }

         instrumentsPacket.write(Types.REGISTRY_ENTRY_ARRAY, entries);
         instrumentsPacket.send(Protocol1_21To1_21_2.class);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_21.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.STRING_ARRAY);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.VAR_INT);
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.OPTIONAL_GLOBAL_POSITION);
            this.map(Types.VAR_INT);
            this.handler(EntityPacketRewriter1_21_2.this.worldDataTrackerHandlerByKey1_20_5(3));
            this.handler(EntityPacketRewriter1_21_2.this.playerTrackerHandler());
            this.create(Types.VAR_INT, 64);
         }
      });
      this.protocol.replaceClientbound(ClientboundPackets1_21.RESPAWN, wrapper -> {
         int dimensionId = wrapper.passthrough(Types.VAR_INT);
         String world = wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, 64);
         byte keepDataMask = wrapper.passthrough(Types.BYTE);
         EntityTracker1_21_2 entityTracker = this.tracker(wrapper.user());
         if (entityTracker.currentWorld() != null && !entityTracker.currentWorld().equals(world)) {
            ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
            if (storables.chunkLoadTracker() != null) {
               storables.chunkLoadTracker().clear();
            }
         }

         this.trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         storables.setGroundFlagTracker(new GroundFlagTracker());
         storables.setClientVehicleStorage(null);
         if ((keepDataMask & 1) == 0) {
            entityTracker.setPlayerMaxHealthAttributeValue(20.0);
         }

         if ((keepDataMask & 2) != 0) {
            boolean isBundling = storables.bundleStateTracker().isBundling();
            if (!isBundling) {
               PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
               bundleStart.send(Protocol1_21To1_21_2.class);
            }

            wrapper.send(Protocol1_21To1_21_2.class);
            wrapper.cancel();
            PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_21_2.SET_ENTITY_DATA);
            entityDataPacket.write(Types.VAR_INT, entityTracker.clientEntityId());
            List<EntityData> entityData = new ArrayList<>();
            entityData.add(new EntityData(6, VersionedTypes.V1_21_2.entityDataTypes.poseType, 0));
            entityData.add(new EntityData(9, VersionedTypes.V1_21_2.entityDataTypes.floatType, (float)entityTracker.playerMaxHealthAttributeValue()));
            entityDataPacket.write(VersionedTypes.V1_21_2.entityDataList, entityData);
            entityDataPacket.send(Protocol1_21To1_21_2.class);
            int teleportId = ThreadLocalRandom.current().nextInt();
            storables.teleportAckCancelStorage().cancelTeleportId(teleportId);
            PlayerPositionStorage positionStorage = storables.playerPositionStorage();
            if (positionStorage != null) {
               positionStorage.sendPing(wrapper.user(), ThreadLocalRandom.current().nextInt());
            }

            PacketWrapper positionPacket = wrapper.create(ClientboundPackets1_21_2.PLAYER_POSITION);
            positionPacket.write(Types.VAR_INT, teleportId);
            positionPacket.write(Types.DOUBLE, 0.0);
            positionPacket.write(Types.DOUBLE, 0.0);
            positionPacket.write(Types.DOUBLE, 0.0);
            positionPacket.write(Types.DOUBLE, 0.0);
            positionPacket.write(Types.DOUBLE, 0.0);
            positionPacket.write(Types.DOUBLE, 0.0);
            positionPacket.write(Types.FLOAT, -180.0F);
            positionPacket.write(Types.FLOAT, 0.0F);
            positionPacket.write(Types.INT, 7);
            positionPacket.send(Protocol1_21To1_21_2.class);
            if (!isBundling) {
               PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
               bundleEnd.send(Protocol1_21To1_21_2.class);
            }
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.PLAYER_POSITION, wrapper -> {
         wrapper.write(Types.VAR_INT, 0);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.DOUBLE, 0.0);
         wrapper.write(Types.DOUBLE, 0.0);
         wrapper.write(Types.DOUBLE, 0.0);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         int relativeArguments = wrapper.read(Types.BYTE) & 31;
         if ((relativeArguments & 1) != 0) {
            relativeArguments |= 32;
         }

         if ((relativeArguments & 2) != 0) {
            relativeArguments |= 64;
         }

         if ((relativeArguments & 4) != 0) {
            relativeArguments |= 128;
         }

         wrapper.write(Types.INT, relativeArguments);
         int teleportId = wrapper.read(Types.VAR_INT);
         wrapper.set(Types.VAR_INT, 0, teleportId);
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         PlayerPositionStorage positionStorage = storables.playerPositionStorage();
         if (positionStorage != null) {
            boolean isBundling = storables.bundleStateTracker().isBundling();
            if (!isBundling) {
               PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
               bundleStart.send(Protocol1_21To1_21_2.class);
            }

            positionStorage.sendPing(wrapper.user(), ThreadLocalRandom.current().nextInt());
            wrapper.send(Protocol1_21To1_21_2.class);
            wrapper.cancel();
            if (!isBundling) {
               PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
               bundleEnd.send(Protocol1_21To1_21_2.class);
            }
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.SET_PASSENGERS, wrapper -> {
         int vehicleId = wrapper.passthrough(Types.VAR_INT);
         int[] passengerIds = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         ClientVehicleStorage storage = storables.clientVehicleStorage();
         if (storage != null && vehicleId == storage.vehicleId()) {
            storables.setClientVehicleStorage(null);
         }

         EntityTracker1_21_2 tracker = this.tracker(wrapper.user());
         EntityTracker1_21_2.BoatEntity entity = tracker.trackedBoatEntity(vehicleId);
         if (entity != null) {
            entity.setPassengers(passengerIds);
         }

         int clientEntityId = this.<EntityTracker>tracker(wrapper.user()).clientEntityId();

         for (int passenger : passengerIds) {
            if (passenger == clientEntityId) {
               storables.setClientVehicleStorage(new ClientVehicleStorage(vehicleId));
               break;
            }
         }
      });
      this.protocol.appendClientbound(ClientboundPackets1_21.REMOVE_ENTITIES, wrapper -> {
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         ClientVehicleStorage vehicleStorage = storables.clientVehicleStorage();
         if (vehicleStorage != null) {
            int[] entityIds = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);

            for (int entityId : entityIds) {
               if (entityId == vehicleStorage.vehicleId()) {
                  storables.setClientVehicleStorage(null);
                  break;
               }
            }
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.PLAYER_INPUT, wrapper -> {
         wrapper.cancel();
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         ClientVehicleStorage vehicleStorage = storables.clientVehicleStorage();
         if (vehicleStorage != null) {
            byte flags = wrapper.read(Types.BYTE);
            boolean left = (flags & 4) != 0;
            boolean right = (flags & 8) != 0;
            float sidewaysMovement = left ? 0.98F : (right ? -0.98F : 0.0F);
            boolean forward = (flags & 1) != 0;
            boolean backward = (flags & 2) != 0;
            float forwardMovement = forward ? 0.98F : (backward ? -0.98F : 0.0F);
            byte updatedFlags = 0;
            if ((flags & 16) != 0) {
               updatedFlags = (byte)(updatedFlags | 1);
            }

            if ((flags & 32) != 0) {
               updatedFlags = (byte)(updatedFlags | 2);
            }

            if (wrapper.user().isServerSide() && this.isVF) {
               wrapper.write(Types.FLOAT, sidewaysMovement);
               wrapper.write(Types.FLOAT, forwardMovement);
               wrapper.write(Types.BYTE, flags);
            } else {
               vehicleStorage.storeMovement(sidewaysMovement, forwardMovement, updatedFlags);
            }
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.TELEPORT_ENTITY, ClientboundPackets1_21_2.ENTITY_POSITION_SYNC, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         double x = wrapper.passthrough(Types.DOUBLE);
         double y = wrapper.passthrough(Types.DOUBLE);
         double z = wrapper.passthrough(Types.DOUBLE);
         wrapper.write(Types.DOUBLE, 0.0);
         wrapper.write(Types.DOUBLE, 0.0);
         wrapper.write(Types.DOUBLE, 0.0);
         float yaw = wrapper.read(Types.BYTE).byteValue() * 360.0F / 256.0F;
         float pitch = wrapper.read(Types.BYTE).byteValue() * 360.0F / 256.0F;
         wrapper.write(Types.FLOAT, yaw);
         wrapper.write(Types.FLOAT, pitch);
         EntityTracker1_21_2 tracker = this.tracker(wrapper.user());
         EntityTracker1_21_2.BoatEntity trackedEntity = tracker.trackedBoatEntity(entityId);
         if (trackedEntity != null) {
            trackedEntity.setPosition(x, y, z);
            trackedEntity.setRotation(yaw, pitch);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.MOVE_ENTITY_POS, wrapper -> this.storeEntityPositionRotation(wrapper, true, false));
      this.protocol.registerClientbound(ClientboundPackets1_21.MOVE_ENTITY_POS_ROT, wrapper -> this.storeEntityPositionRotation(wrapper, true, true));
      this.protocol.registerClientbound(ClientboundPackets1_21.MOVE_ENTITY_ROT, wrapper -> this.storeEntityPositionRotation(wrapper, false, true));
      this.protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_POS, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         this.handleOnGround(wrapper);
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_POS_ROT, wrapper -> {
         double x = wrapper.passthrough(Types.DOUBLE);
         double y = wrapper.passthrough(Types.DOUBLE);
         double z = wrapper.passthrough(Types.DOUBLE);
         float yaw = wrapper.passthrough(Types.FLOAT);
         float pitch = wrapper.passthrough(Types.FLOAT);
         this.handleOnGround(wrapper);
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         PlayerPositionStorage playerPositionStorage = storables.playerPositionStorage();
         if (playerPositionStorage != null && playerPositionStorage.checkCaptureNextPlayerPositionPacket()) {
            boolean onGround = wrapper.get(Types.BOOLEAN, 0);
            playerPositionStorage.setPlayerPosition(new PlayerPositionStorage.PlayerPosition(x, y, z, yaw, pitch, onGround));
            wrapper.cancel();
         } else if (storables.teleportAckCancelStorage().checkShouldCancelPlayerPositionPacket()) {
            wrapper.cancel();
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_ROT, wrapper -> {
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         this.handleOnGround(wrapper);
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         ClientVehicleStorage vehicleStorage = storables.clientVehicleStorage();
         if (vehicleStorage != null && (!wrapper.user().isServerSide() || !this.isVF)) {
            wrapper.sendToServer(Protocol1_21To1_21_2.class);
            wrapper.cancel();
            PacketWrapper playerInput = wrapper.create(ServerboundPackets1_20_5.PLAYER_INPUT);
            playerInput.write(Types.FLOAT, vehicleStorage.sidewaysMovement());
            playerInput.write(Types.FLOAT, vehicleStorage.forwardMovement());
            playerInput.write(Types.BYTE, vehicleStorage.flags());
            playerInput.sendToServer(Protocol1_21To1_21_2.class);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.MOVE_PLAYER_STATUS_ONLY, wrapper -> {
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         GroundFlagTracker tracker = storables.groundFlagTracker();
         boolean prevOnGround = tracker.onGround();
         boolean prevHorizontalCollision = tracker.horizontalCollision();
         this.handleOnGround(wrapper);
         if (prevOnGround == tracker.onGround() && prevHorizontalCollision != tracker.horizontalCollision()) {
            wrapper.cancel();
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.ACCEPT_TELEPORTATION, wrapper -> {
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         PlayerPositionStorage playerPositionStorage = storables.playerPositionStorage();
         int teleportId = wrapper.passthrough(Types.VAR_INT);
         if (storables.teleportAckCancelStorage().checkShouldCancelTeleportAck(teleportId)) {
            wrapper.cancel();
            if (playerPositionStorage != null && playerPositionStorage.checkHasPlayerPosition()) {
               playerPositionStorage.reset();
               storables.teleportAckCancelStorage().checkShouldCancelPlayerPositionPacket();
            }
         } else {
            if (playerPositionStorage != null && playerPositionStorage.checkHasPlayerPosition()) {
               wrapper.sendToServer(Protocol1_21To1_21_2.class);
               wrapper.cancel();
               playerPositionStorage.sendMovePlayerPosRot(wrapper.user());
            }
         }
      });
   }

   private void handleOnGround(PacketWrapper wrapper) {
      ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
      GroundFlagTracker tracker = storables.groundFlagTracker();
      short data = wrapper.read(Types.UNSIGNED_BYTE);
      wrapper.write(Types.BOOLEAN, tracker.setOnGround((data & 1) != 0));
      tracker.setHorizontalCollision((data & 2) != 0);
   }

   private void storeEntityPositionRotation(PacketWrapper wrapper, boolean position, boolean rotation) {
      int entityId = wrapper.passthrough(Types.VAR_INT);
      EntityTracker1_21_2 tracker = this.tracker(wrapper.user());
      EntityTracker1_21_2.BoatEntity trackedEntity = tracker.trackedBoatEntity(entityId);
      if (trackedEntity != null) {
         if (position) {
            double x = wrapper.passthrough(Types.SHORT).shortValue() / 4096.0;
            double y = wrapper.passthrough(Types.SHORT).shortValue() / 4096.0;
            double z = wrapper.passthrough(Types.SHORT).shortValue() / 4096.0;
            trackedEntity.setPosition(trackedEntity.x() + x, trackedEntity.y() + y, trackedEntity.z() + z);
         }

         if (rotation) {
            float yaw = wrapper.passthrough(Types.BYTE).byteValue() * 360.0F / 256.0F;
            float pitch = wrapper.passthrough(Types.BYTE).byteValue() * 360.0F / 256.0F;
            trackedEntity.setRotation(yaw, pitch);
         }
      }
   }

   @Override
   protected void registerRewrites() {
      this.dataTypeMapper().register();
      this.registerEntityDataTypeHandler(
         VersionedTypes.V1_21_2.entityDataTypes.itemType,
         VersionedTypes.V1_21_2.entityDataTypes.blockStateType,
         VersionedTypes.V1_21_2.entityDataTypes.optionalBlockStateType,
         VersionedTypes.V1_21_2.entityDataTypes.particleType,
         VersionedTypes.V1_21_2.entityDataTypes.particlesType,
         VersionedTypes.V1_21_2.entityDataTypes.componentType,
         VersionedTypes.V1_21_2.entityDataTypes.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_21_2.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_21_2.ABSTRACT_BOAT).handler((event, data) -> {
         int dataIndex = event.index();
         if (dataIndex > 11) {
            event.setIndex(dataIndex - 1);
         } else if (dataIndex == 11) {
            event.cancel();
            EntityTracker1_21_2 tracker = this.tracker(event.user());
            EntityTracker1_21_2.BoatEntity entity = tracker.trackedBoatEntity(event.entityId());
            if (entity != null) {
               ProtocolStorables1_21_2 storables = event.user().storables(this.protocol);
               boolean isBundling = storables.bundleStateTracker().isBundling();
               if (!isBundling) {
                  PacketWrapper bundleStart = PacketWrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER, event.user());
                  bundleStart.send(Protocol1_21To1_21_2.class);
               }

               PacketWrapper removeEntityPacket = PacketWrapper.create(ClientboundPackets1_21_2.REMOVE_ENTITIES, event.user());
               removeEntityPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{event.entityId()});
               removeEntityPacket.send(Protocol1_21To1_21_2.class);
               int boatType = (Integer)data.getValue();
               EntityType entityType;
               if (tracker.entityType(event.entityId()).isOrHasParent(EntityTypes1_21_2.ABSTRACT_CHEST_BOAT)) {
                  entityType = this.entityTypeFromChestBoatType(boatType);
               } else {
                  entityType = this.entityTypeFromBoatType(boatType);
               }

               PacketWrapper spawnEntityPacket = PacketWrapper.create(ClientboundPackets1_21_2.ADD_ENTITY, event.user());
               spawnEntityPacket.write(Types.VAR_INT, event.entityId());
               spawnEntityPacket.write(Types.UUID, entity.uuid());
               spawnEntityPacket.write(Types.VAR_INT, entityType.getId());
               spawnEntityPacket.write(Types.DOUBLE, entity.x());
               spawnEntityPacket.write(Types.DOUBLE, entity.y());
               spawnEntityPacket.write(Types.DOUBLE, entity.z());
               spawnEntityPacket.write(Types.BYTE, (byte)Math.floor(entity.pitch() * 256.0F / 360.0F));
               spawnEntityPacket.write(Types.BYTE, (byte)Math.floor(entity.yaw() * 256.0F / 360.0F));
               spawnEntityPacket.write(Types.BYTE, (byte)0);
               spawnEntityPacket.write(Types.VAR_INT, entity.data());
               spawnEntityPacket.write(Types.SHORT, (short)0);
               spawnEntityPacket.write(Types.SHORT, (short)0);
               spawnEntityPacket.write(Types.SHORT, (short)0);
               spawnEntityPacket.send(Protocol1_21To1_21_2.class);
               tracker.updateBoatType(event.entityId(), entityType);
               PacketWrapper setEntityDataPacket = PacketWrapper.create(ClientboundPackets1_21_2.SET_ENTITY_DATA, event.user());
               setEntityDataPacket.write(Types.VAR_INT, event.entityId());
               setEntityDataPacket.write(VersionedTypes.V1_21_2.entityDataList, entity.entityData());
               setEntityDataPacket.send(Protocol1_21To1_21_2.class);
               if (entity.passengers() != null) {
                  PacketWrapper setPassengersPacket = PacketWrapper.create(ClientboundPackets1_21_2.SET_PASSENGERS, event.user());
                  setPassengersPacket.write(Types.VAR_INT, event.entityId());
                  setPassengersPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, entity.passengers());
                  setPassengersPacket.send(Protocol1_21To1_21_2.class);
               }

               if (!isBundling) {
                  PacketWrapper bundleEnd = PacketWrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER, event.user());
                  bundleEnd.send(Protocol1_21To1_21_2.class);
               }
            }
         }
      });
      this.filter().type(EntityTypes1_21_2.SALMON).addIndex(17);
      this.filter().type(EntityTypes1_21_2.AGEABLE_WATER_CREATURE).addIndex(16);
      this.filter().type(EntityTypes1_21_2.ABSTRACT_ARROW).addIndex(10);
   }

   @Override
   public void handleEntityData(int entityId, List<EntityData> dataList, UserConnection connection) {
      super.handleEntityData(entityId, dataList, connection);
      EntityTracker1_21_2 tracker = this.tracker(connection);
      EntityType entityType = tracker.entityType(entityId);
      if (entityType != null && entityType.isOrHasParent(EntityTypes1_21_2.ABSTRACT_BOAT)) {
         List<EntityData> entityData = tracker.trackedBoatEntity(entityId).entityData();
         entityData.removeIf(first -> dataList.stream().anyMatch(second -> first.id() == second.id()));

         for (EntityData data : dataList) {
            Object value = data.value();
            if (value instanceof Item item) {
               entityData.add(new EntityData(data.id(), data.dataType(), item.copy()));
            } else {
               entityData.add(new EntityData(data.id(), data.dataType(), value));
            }
         }
      }
   }

   private EntityType entityTypeFromBoatType(int boatType) {
      if (boatType == 0) {
         return EntityTypes1_21_2.OAK_BOAT;
      } else if (boatType == 1) {
         return EntityTypes1_21_2.SPRUCE_BOAT;
      } else if (boatType == 2) {
         return EntityTypes1_21_2.BIRCH_BOAT;
      } else if (boatType == 3) {
         return EntityTypes1_21_2.JUNGLE_BOAT;
      } else if (boatType == 4) {
         return EntityTypes1_21_2.ACACIA_BOAT;
      } else if (boatType == 5) {
         return EntityTypes1_21_2.CHERRY_BOAT;
      } else if (boatType == 6) {
         return EntityTypes1_21_2.DARK_OAK_BOAT;
      } else if (boatType == 7) {
         return EntityTypes1_21_2.MANGROVE_BOAT;
      } else {
         return boatType == 8 ? EntityTypes1_21_2.BAMBOO_RAFT : EntityTypes1_21_2.OAK_BOAT;
      }
   }

   private EntityType entityTypeFromChestBoatType(int chestBoatType) {
      if (chestBoatType == 0) {
         return EntityTypes1_21_2.OAK_CHEST_BOAT;
      } else if (chestBoatType == 1) {
         return EntityTypes1_21_2.SPRUCE_CHEST_BOAT;
      } else if (chestBoatType == 2) {
         return EntityTypes1_21_2.BIRCH_CHEST_BOAT;
      } else if (chestBoatType == 3) {
         return EntityTypes1_21_2.JUNGLE_CHEST_BOAT;
      } else if (chestBoatType == 4) {
         return EntityTypes1_21_2.ACACIA_CHEST_BOAT;
      } else if (chestBoatType == 5) {
         return EntityTypes1_21_2.CHERRY_CHEST_BOAT;
      } else if (chestBoatType == 6) {
         return EntityTypes1_21_2.DARK_OAK_CHEST_BOAT;
      } else if (chestBoatType == 7) {
         return EntityTypes1_21_2.MANGROVE_CHEST_BOAT;
      } else {
         return chestBoatType == 8 ? EntityTypes1_21_2.BAMBOO_CHEST_RAFT : EntityTypes1_21_2.OAK_CHEST_BOAT;
      }
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_2.getTypeFromId(type);
   }
}
