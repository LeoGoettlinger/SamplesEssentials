package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.Protocol1_21_9To1_21_7;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.MannequinData;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.PlayerRotationStorage;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.ProtocolStorables1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.tracker.EntityTracker1_21_9;
import com.viaversion.viabackwards.utils.VelocityUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.ResolvableProfile;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.GameProfile.Property;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandler;
import com.viaversion.viaversion.util.Copyable;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_21_9 extends EntityRewriter<ClientboundPacket1_21_9, Protocol1_21_9To1_21_7> {
   public EntityPacketRewriter1_21_9(Protocol1_21_9To1_21_7 protocol) {
      super(
         protocol,
         ((EntityDataTypes1_21_9)VersionedTypes.V1_21_9.entityDataTypes).optionalComponentType,
         ((EntityDataTypes1_21_9)VersionedTypes.V1_21_9.entityDataTypes).booleanType
      );
   }

   public void registerPackets() {
      ((Protocol1_21_9To1_21_7)this.protocol).replaceClientbound(ClientboundPackets1_21_9.ADD_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         UUID uuid = (UUID)wrapper.passthrough(Types.UUID);
         int entityTypeId = (Integer)wrapper.passthrough(Types.VAR_INT);
         double x = (Double)wrapper.passthrough(Types.DOUBLE);
         double y = (Double)wrapper.passthrough(Types.DOUBLE);
         double z = (Double)wrapper.passthrough(Types.DOUBLE);
         Vector3d movement = (Vector3d)wrapper.read(Types.LOW_PRECISION_VECTOR);
         byte pitch = (Byte)wrapper.passthrough(Types.BYTE);
         byte yaw = (Byte)wrapper.passthrough(Types.BYTE);
         byte headYaw = (Byte)wrapper.passthrough(Types.BYTE);
         int data = (Integer)wrapper.passthrough(Types.VAR_INT);
         TrackedEntity entity = this.trackAndRewrite(wrapper, entityTypeId, entityId);
         if (entity.entityType() == EntityTypes1_21_9.FALLING_BLOCK) {
            int mappedBlockStateId = ((Protocol1_21_9To1_21_7)this.protocol).getMappingData().getNewBlockStateId(data);
            wrapper.set(Types.VAR_INT, 2, mappedBlockStateId);
         }

         this.writeMovementShorts(wrapper, movement);
         if (EntityTypes1_21_9.getTypeFromId(entityTypeId) == EntityTypes1_21_9.MANNEQUIN) {
            String name = this.randomHackyEmptyName();
            MannequinData mannequinData = new MannequinData(uuid, name);
            TrackedEntity trackedEntity = this.tracker(wrapper.user()).entity(entityId);
            trackedEntity.put(mannequinData);
            this.sendInitialPlayerInfoUpdate(wrapper.user(), mannequinData, new Property[0]);
            mannequinData.setPosition(x, y, z);
            mannequinData.setRotation(yaw, pitch);
            mannequinData.setHeadYaw(headYaw);
         }
      });
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.TELEPORT_ENTITY, this::trackMannequinTeleport);
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.ENTITY_POSITION_SYNC, this::trackMannequinTeleport);
      ((Protocol1_21_9To1_21_7)this.protocol)
         .registerClientbound(ClientboundPackets1_21_9.MOVE_ENTITY_POS, wrapper -> this.storeMovementMannequinData(wrapper, true, false));
      ((Protocol1_21_9To1_21_7)this.protocol)
         .registerClientbound(ClientboundPackets1_21_9.MOVE_ENTITY_POS_ROT, wrapper -> this.storeMovementMannequinData(wrapper, true, true));
      ((Protocol1_21_9To1_21_7)this.protocol)
         .registerClientbound(ClientboundPackets1_21_9.MOVE_ENTITY_ROT, wrapper -> this.storeMovementMannequinData(wrapper, false, true));
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.ROTATE_HEAD, wrapper -> {
         int vehicleId = (Integer)wrapper.passthrough(Types.VAR_INT);
         byte headRotation = (Byte)wrapper.passthrough(Types.BYTE);
         EntityTracker1_21_9 tracker = (EntityTracker1_21_9)this.tracker(wrapper.user());
         TrackedEntity trackedEntity = tracker.entity(vehicleId);
         if (trackedEntity != null) {
            MannequinData data = (MannequinData)trackedEntity.get(MannequinData.class);
            if (data != null) {
               data.setHeadYaw(headRotation);
            }
         }
      });
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.SET_PASSENGERS, wrapper -> {
         int vehicleId = (Integer)wrapper.passthrough(Types.VAR_INT);
         int[] passengerIds = (int[])wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
         EntityTracker1_21_9 tracker = (EntityTracker1_21_9)this.tracker(wrapper.user());
         TrackedEntity trackedEntity = tracker.entity(vehicleId);
         if (trackedEntity != null) {
            MannequinData data = (MannequinData)trackedEntity.get(MannequinData.class);
            if (data != null) {
               data.setPassengers(passengerIds);
            }
         }
      });
      ((Protocol1_21_9To1_21_7)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_21_9.SET_EQUIPMENT,
            wrapper -> {
               int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
               TrackedEntity trackedEntity = this.tracker(wrapper.user()).entity(entityId);
               MannequinData mannequinData = trackedEntity != null ? (MannequinData)trackedEntity.get(MannequinData.class) : null;

               byte slot;
               do {
                  slot = (Byte)wrapper.passthrough(Types.BYTE);
                  Item item = ((Protocol1_21_9To1_21_7)this.protocol)
                     .getItemRewriter()
                     .handleItemToClient(wrapper.user(), (Item)wrapper.read(((Protocol1_21_9To1_21_7)this.protocol).getItemRewriter().itemType()));
                  wrapper.write(((Protocol1_21_9To1_21_7)this.protocol).getItemRewriter().mappedItemType(), item);
                  if (mannequinData != null) {
                     mannequinData.setEquipment((byte)(slot & 127), item);
                  }
               } while (slot < 0);
            }
         );
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.SET_ENTITY_MOTION, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         this.writeMovementShorts(wrapper, (Vector3d)wrapper.read(Types.LOW_PRECISION_VECTOR));
      });
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.PLAYER_ROTATION, wrapper -> {
         ProtocolStorables1_21_9 storables = (ProtocolStorables1_21_9)wrapper.user().storables(this.protocol);
         PlayerRotationStorage storage = storables.playerRotationStorage();
         float yRot = (Float)wrapper.read(Types.FLOAT);
         if ((Boolean)wrapper.read(Types.BOOLEAN)) {
            yRot += storage.yaw();
         }

         float xRot = (Float)wrapper.read(Types.FLOAT);
         if ((Boolean)wrapper.read(Types.BOOLEAN)) {
            xRot += storage.pitch();
         }

         wrapper.write(Types.FLOAT, yRot);
         wrapper.write(Types.FLOAT, xRot);
         storage.setRotation(yRot, xRot);
      });
      ((Protocol1_21_9To1_21_7)this.protocol).registerClientbound(ClientboundPackets1_21_9.SET_DEFAULT_SPAWN_POSITION, wrapper -> {
         GlobalBlockPosition pos = (GlobalBlockPosition)wrapper.read(Types.GLOBAL_POSITION);
         wrapper.write(Types.BLOCK_POSITION1_14, new BlockPosition(pos.x(), pos.y(), pos.z()));
         wrapper.passthrough(Types.FLOAT);
         wrapper.read(Types.FLOAT);
      });
      ((Protocol1_21_9To1_21_7)this.protocol).registerServerbound(ServerboundPackets1_21_6.MOVE_PLAYER_POS_ROT, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         this.storePlayerRotation(wrapper);
      });
      ((Protocol1_21_9To1_21_7)this.protocol).registerServerbound(ServerboundPackets1_21_6.MOVE_PLAYER_ROT, this::storePlayerRotation);
   }

   private void trackMannequinTeleport(PacketWrapper wrapper) {
      int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
      EntityTracker1_21_9 tracker = (EntityTracker1_21_9)this.tracker(wrapper.user());
      TrackedEntity trackedEntity = tracker.entity(entityId);
      if (trackedEntity != null) {
         MannequinData mannequinData = (MannequinData)trackedEntity.get(MannequinData.class);
         if (mannequinData != null) {
            double x = (Double)wrapper.passthrough(Types.DOUBLE);
            double y = (Double)wrapper.passthrough(Types.DOUBLE);
            double z = (Double)wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            byte yaw = (byte)Math.floor((Float)wrapper.passthrough(Types.FLOAT) * 256.0F / 360.0F);
            byte pitch = (byte)Math.floor((Float)wrapper.passthrough(Types.FLOAT) * 256.0F / 360.0F);
            mannequinData.setPosition(x, y, z);
            mannequinData.setRotation(yaw, pitch);
         }
      }
   }

   private void storeMovementMannequinData(PacketWrapper wrapper, boolean position, boolean rotation) {
      int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
      TrackedEntity trackedEntity = this.tracker(wrapper.user()).entity(entityId);
      if (trackedEntity != null) {
         MannequinData mannequinData = (MannequinData)trackedEntity.get(MannequinData.class);
         if (mannequinData != null) {
            if (position) {
               double deltaX = ((Short)wrapper.passthrough(Types.SHORT)).shortValue() / 4096.0;
               double deltaY = ((Short)wrapper.passthrough(Types.SHORT)).shortValue() / 4096.0;
               double deltaZ = ((Short)wrapper.passthrough(Types.SHORT)).shortValue() / 4096.0;
               mannequinData.setPosition(mannequinData.x() + deltaX, mannequinData.y() + deltaY, mannequinData.z() + deltaZ);
            }

            if (rotation) {
               byte yaw = (Byte)wrapper.passthrough(Types.BYTE);
               byte pitch = (Byte)wrapper.passthrough(Types.BYTE);
               mannequinData.setRotation(yaw, pitch);
            }
         }
      }
   }

   private void sendInitialPlayerInfoUpdate(UserConnection connection, MannequinData mannequinData, Property[] properties) {
      PacketWrapper playerInfo = PacketWrapper.create(ClientboundPackets1_21_6.PLAYER_INFO_UPDATE, connection);
      BitSet actions = new BitSet(8);

      for (int i = 0; i < 8; i++) {
         actions.set(i);
      }

      playerInfo.write(Types.PROFILE_ACTIONS_ENUM1_21_4, actions);
      playerInfo.write(Types.VAR_INT, 1);
      playerInfo.write(Types.UUID, mannequinData.uuid());
      playerInfo.write(Types.STRING, mannequinData.name());
      playerInfo.write(Types.PROFILE_PROPERTY_ARRAY, properties);
      playerInfo.write(Types.BOOLEAN, false);
      playerInfo.write(Types.VAR_INT, 0);
      playerInfo.write(Types.BOOLEAN, false);
      playerInfo.write(Types.VAR_INT, 0);
      playerInfo.write(Types.TRUSTED_OPTIONAL_TAG, null);
      playerInfo.write(Types.VAR_INT, 1000);
      playerInfo.write(Types.BOOLEAN, true);
      playerInfo.send(Protocol1_21_9To1_21_7.class);
      this.sendPlayerTeamDisplayName(connection, mannequinData, mannequinData.displayName());
   }

   private void sendPlayerInfoDisplayNameUpdate(UserConnection connection, MannequinData mannequinData, @Nullable Tag displayName) {
      PacketWrapper playerInfo = PacketWrapper.create(ClientboundPackets1_21_6.PLAYER_INFO_UPDATE, connection);
      BitSet actions = new BitSet(8);
      actions.set(5);
      playerInfo.write(Types.PROFILE_ACTIONS_ENUM1_21_4, actions);
      playerInfo.write(Types.VAR_INT, 1);
      playerInfo.write(Types.UUID, mannequinData.uuid());
      playerInfo.write(Types.TRUSTED_OPTIONAL_TAG, displayName);
      playerInfo.send(Protocol1_21_9To1_21_7.class);
      this.sendPlayerTeamDisplayName(connection, mannequinData, displayName);
   }

   private void sendPlayerTeamDisplayName(UserConnection connection, MannequinData mannequinData, Tag displayName) {
      Tag nonNullDisplayName = (Tag)(displayName != null ? displayName : new StringTag("Mannequin"));
      PacketWrapper addTeam = PacketWrapper.create(ClientboundPackets1_21_6.SET_PLAYER_TEAM, connection);
      addTeam.write(Types.STRING, mannequinData.name());
      addTeam.write(Types.BYTE, Byte.valueOf((byte)(mannequinData.hasTeam() ? 2 : 0)));
      addTeam.write(Types.TRUSTED_TAG, nonNullDisplayName);
      addTeam.write(Types.BYTE, (byte)0);
      addTeam.write(Types.VAR_INT, 0);
      addTeam.write(Types.VAR_INT, 0);
      addTeam.write(Types.VAR_INT, 15);
      addTeam.write(Types.TRUSTED_TAG, nonNullDisplayName);
      addTeam.write(Types.TRUSTED_TAG, new StringTag(""));
      if (!mannequinData.hasTeam()) {
         addTeam.write(Types.STRING_ARRAY, new String[]{mannequinData.name()});
         mannequinData.setHasTeam(true);
      }

      addTeam.send(Protocol1_21_9To1_21_7.class);
   }

   private String randomHackyEmptyName() {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < 7; i++) {
         int random = ThreadLocalRandom.current().nextInt("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".length());
         builder.append('§').append("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".charAt(random));
      }

      builder.append("§r");
      return builder.toString();
   }

   private void writeMovementShorts(PacketWrapper wrapper, Vector3d movement) {
      wrapper.write(Types.SHORT, VelocityUtil.toLegacyVelocity(movement.x()));
      wrapper.write(Types.SHORT, VelocityUtil.toLegacyVelocity(movement.y()));
      wrapper.write(Types.SHORT, VelocityUtil.toLegacyVelocity(movement.z()));
   }

   private void storePlayerRotation(PacketWrapper wrapper) {
      float yaw = (Float)wrapper.passthrough(Types.FLOAT);
      float pitch = (Float)wrapper.passthrough(Types.FLOAT);
      ProtocolStorables1_21_9 storables = (ProtocolStorables1_21_9)wrapper.user().storables(this.protocol);
      storables.playerRotationStorage().setRotation(yaw, pitch);
   }

   protected void registerRewrites() {
      EntityDataTypes1_21_5 entityDataTypes = (EntityDataTypes1_21_5)((Protocol1_21_9To1_21_7)this.protocol).mappedTypes().entityDataTypes();
      this.dataTypeMapper()
         .added(entityDataTypes.compoundTagType)
         .removed(((EntityDataTypes1_21_9)VersionedTypes.V1_21_9.entityDataTypes).copperGolemState)
         .removed(((EntityDataTypes1_21_9)VersionedTypes.V1_21_9.entityDataTypes).weatheringCopperState)
         .removed(((EntityDataTypes1_21_9)VersionedTypes.V1_21_9.entityDataTypes).mannequinProfileType)
         .register();
      this.registerEntityDataTypeHandler1_20_3(
         entityDataTypes.itemType,
         entityDataTypes.blockStateType,
         entityDataTypes.optionalBlockStateType,
         entityDataTypes.particleType,
         entityDataTypes.particlesType,
         entityDataTypes.componentType,
         entityDataTypes.optionalComponentType
      );
      this.filter().type(EntityTypes1_21_9.COPPER_GOLEM).cancel(16);
      this.filter().type(EntityTypes1_21_9.COPPER_GOLEM).cancel(17);
      EntityDataHandler shoulderDataHandler = (event, data) -> {
         CompoundTag entityTag = new CompoundTag();
         Integer value = (Integer)data.value();
         if (value != null) {
            entityTag.putString("id", "parrot");
            entityTag.putInt("Variant", value);
         }

         data.setTypeAndValue(((EntityDataTypes1_21_5)((Protocol1_21_9To1_21_7)this.protocol).mappedTypes().entityDataTypes).compoundTagType, entityTag);
      };
      this.filter().type(EntityTypes1_21_9.PLAYER).index(19).handler(shoulderDataHandler);
      this.filter().type(EntityTypes1_21_9.PLAYER).index(20).handler(shoulderDataHandler);
      this.filter().type(EntityTypes1_21_9.PLAYER).handler((event, data) -> {
         if (event.index() == 15) {
            event.setIndex(18);
         } else if (event.index() == 16) {
            event.setIndex(17);
         } else if (event.index() == 17 || event.index() == 18) {
            event.setIndex(event.index() - 2);
         }
      });
      this.filter().type(EntityTypes1_21_9.MANNEQUIN).handler((event, data) -> {
         if (event.index() == 2) {
            Tag displayName = (Tag)data.value();
            MannequinData mannequinData = (MannequinData)event.trackedEntity().get(MannequinData.class);
            mannequinData.setDisplayName(displayName);
            this.sendPlayerInfoDisplayNameUpdate(event.user(), mannequinData, displayName);
         } else if (event.index() == 17) {
            ProtocolStorables1_21_9 storables = (ProtocolStorables1_21_9)event.user().storables(this.protocol);
            boolean isBundling = storables.bundleStateTracker().isBundling();
            if (!isBundling) {
               PacketWrapper bundleStart = PacketWrapper.create(ClientboundPackets1_21_6.BUNDLE_DELIMITER, event.user());
               bundleStart.send(Protocol1_21_9To1_21_7.class);
            }

            ResolvableProfile profile = (ResolvableProfile)data.value();
            MannequinData mannequinData = (MannequinData)event.trackedEntity().get(MannequinData.class);
            UUID uuid = mannequinData.uuid();
            PacketWrapper removeEntityPacket = PacketWrapper.create(ClientboundPackets1_21_6.REMOVE_ENTITIES, event.user());
            removeEntityPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{event.entityId()});
            removeEntityPacket.send(Protocol1_21_9To1_21_7.class);
            PacketWrapper playerInfoRemove = PacketWrapper.create(ClientboundPackets1_21_6.PLAYER_INFO_REMOVE, event.user());
            playerInfoRemove.write(Types.UUID_ARRAY, new UUID[]{uuid});
            playerInfoRemove.send(Protocol1_21_9To1_21_7.class);
            this.sendInitialPlayerInfoUpdate(event.user(), mannequinData, profile.profile().properties());
            PacketWrapper spawnEntityPacket = PacketWrapper.create(ClientboundPackets1_21_6.ADD_ENTITY, event.user());
            spawnEntityPacket.write(Types.VAR_INT, event.entityId());
            spawnEntityPacket.write(Types.UUID, mannequinData.uuid());
            spawnEntityPacket.write(Types.VAR_INT, EntityTypes1_21_6.PLAYER.getId());
            spawnEntityPacket.write(Types.DOUBLE, mannequinData.x());
            spawnEntityPacket.write(Types.DOUBLE, mannequinData.y());
            spawnEntityPacket.write(Types.DOUBLE, mannequinData.z());
            spawnEntityPacket.write(Types.BYTE, mannequinData.pitch());
            spawnEntityPacket.write(Types.BYTE, mannequinData.yaw());
            spawnEntityPacket.write(Types.BYTE, mannequinData.headYaw());
            spawnEntityPacket.write(Types.VAR_INT, 0);
            spawnEntityPacket.write(Types.SHORT, (short)0);
            spawnEntityPacket.write(Types.SHORT, (short)0);
            spawnEntityPacket.write(Types.SHORT, (short)0);
            spawnEntityPacket.send(Protocol1_21_9To1_21_7.class);
            PacketWrapper setEntityDataPacket = PacketWrapper.create(ClientboundPackets1_21_6.SET_ENTITY_DATA, event.user());
            setEntityDataPacket.write(Types.VAR_INT, event.entityId());
            setEntityDataPacket.write(VersionedTypes.V1_21_6.entityDataList, mannequinData.entityData());
            setEntityDataPacket.send(Protocol1_21_9To1_21_7.class);
            if (mannequinData.passengers() != null) {
               PacketWrapper setPassengersPacket = PacketWrapper.create(ClientboundPackets1_21_6.SET_PASSENGERS, event.user());
               setPassengersPacket.write(Types.VAR_INT, event.entityId());
               setPassengersPacket.write(Types.VAR_INT_ARRAY_PRIMITIVE, mannequinData.passengers());
               setPassengersPacket.send(Protocol1_21_9To1_21_7.class);
            }

            if (!mannequinData.itemMap().isEmpty()) {
               PacketWrapper equipment = PacketWrapper.create(ClientboundPackets1_21_6.SET_EQUIPMENT, event.user());
               equipment.write(Types.VAR_INT, event.entityId());
               int i = 0;

               for (Entry<Byte, Item> itemEntry : mannequinData.itemMap().entrySet()) {
                  boolean more = i < mannequinData.itemMap().size() - 1;
                  equipment.write(Types.BYTE, more ? (byte)(itemEntry.getKey() | -128) : itemEntry.getKey());
                  equipment.write(((Protocol1_21_9To1_21_7)this.protocol).getItemRewriter().mappedItemType(), itemEntry.getValue());
                  i++;
               }

               equipment.send(Protocol1_21_9To1_21_7.class);
            }

            if (!isBundling) {
               PacketWrapper bundleStart = PacketWrapper.create(ClientboundPackets1_21_6.BUNDLE_DELIMITER, event.user());
               bundleStart.send(Protocol1_21_9To1_21_7.class);
            }

            event.cancel();
         } else if (event.index() == 15) {
            event.setIndex(18);
         } else if (event.index() == 16) {
            event.setIndex(17);
         } else if (event.index() == 18) {
            event.cancel();
         } else if (event.index() == 19) {
            event.cancel();
         }
      });
   }

   @Override
   public void handleEntityData(int entityId, List<EntityData> dataList, UserConnection connection) {
      super.handleEntityData(entityId, dataList, connection);
      EntityTracker1_21_9 tracker = (EntityTracker1_21_9)this.tracker(connection);
      EntityType entityType = tracker.entityType(entityId);
      if (entityType != null && entityType.isOrHasParent(EntityTypes1_21_9.MANNEQUIN)) {
         MannequinData mannequinData = (MannequinData)tracker.entity(entityId).get(MannequinData.class);
         if (mannequinData != null) {
            List<EntityData> entityData = mannequinData.entityData();
            entityData.removeIf(first -> dataList.stream().anyMatch(second -> first.id() == second.id()));

            for (EntityData data : dataList) {
               Object value = data.value();
               entityData.add(new EntityData(data.id(), data.dataType(), Copyable.copy(value)));
            }
         }
      }
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes1_21_9.COPPER_GOLEM, EntityTypes1_21_9.FROG).tagName();
      this.mapEntityTypeWithData(EntityTypes1_21_9.MANNEQUIN, EntityTypes1_21_9.PLAYER);
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_21_9.getTypeFromId(type);
   }
}
