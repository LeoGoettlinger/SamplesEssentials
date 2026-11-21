package com.viaversion.viabackwards.protocol.v1_21_2to1_21.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.entities.EntityScaleHelper;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.Protocol1_21_2To1_21;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.BackwardsStorables1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.PlayerStorage;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.SignStorage;
import com.viaversion.viabackwards.utils.VelocityUtil;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ClientVehicleStorage;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_21_2 extends EntityRewriter<ClientboundPacket1_21_2, Protocol1_21_2To1_21> {
   private static final int REL_X = 0;
   private static final int REL_Y = 1;
   private static final int REL_Z = 2;
   private static final int REL_Y_ROT = 3;
   private static final int REL_X_ROT = 4;
   private static final int REL_DELTA_X = 5;
   private static final int REL_DELTA_Y = 6;
   private static final int REL_DELTA_Z = 7;
   private static final int REL_ROTATE_DELTA = 8;
   private static final int WATER_CREATURE_BABY_INDEX = 16;
   private boolean warned = ViaBackwards.getConfig().suppressEmulationWarnings();

   public EntityPacketRewriter1_21_2(Protocol1_21_2To1_21 protocol) {
      super(
         protocol,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).optionalComponentType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).booleanType
      );
   }

   public void registerPackets() {
      ((Protocol1_21_2To1_21)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_21_2.ADD_ENTITY,
            wrapper -> {
               int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.UUID);
               int entityTypeId = (Integer)wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.DOUBLE);
               wrapper.passthrough(Types.DOUBLE);
               wrapper.passthrough(Types.DOUBLE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               int data = (Integer)wrapper.passthrough(Types.VAR_INT);
               this.trackSpawnWithData1_19(wrapper, data);
               EntityType type = EntityTypes1_21_2.getTypeFromId(entityTypeId);
               if (type.isOrHasParent(EntityTypes1_21_2.ABSTRACT_BOAT)) {
                  wrapper.send(Protocol1_21_2To1_21.class);
                  wrapper.cancel();
                  List<EntityData> entityData = new ArrayList<>();
                  int boatType = type.isOrHasParent(EntityTypes1_21_2.ABSTRACT_CHEST_BOAT)
                     ? this.chestBoatTypeFromEntityType(type)
                     : this.boatTypeFromEntityType(type);
                  entityData.add(new EntityData(11, ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).varIntType, boatType));
                  PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_21.SET_ENTITY_DATA);
                  entityDataPacket.write(Types.VAR_INT, entityId);
                  entityDataPacket.write(VersionedTypes.V1_21.entityDataList, entityData);
                  entityDataPacket.send(Protocol1_21_2To1_21.class);
               }
            }
         );
      ((Protocol1_21_2To1_21)this.protocol)
         .getRegistryDataRewriter()
         .addEnchantmentEffectRewriter("change_item_damage", tag -> tag.putString("type", "damage_item"));
      ((Protocol1_21_2To1_21)this.protocol)
         .replaceClientbound(
            ClientboundConfigurationPackets1_21.REGISTRY_DATA,
            wrapper -> {
               String registryKey = Key.stripMinecraftNamespace((String)wrapper.passthrough(Types.STRING));
               RegistryEntry[] entries = (RegistryEntry[])wrapper.read(Types.REGISTRY_ENTRY_ARRAY);
               if (registryKey.equals("instrument")) {
                  wrapper.cancel();
               } else {
                  if (registryKey.equals("worldgen/biome")) {
                     for (RegistryEntry entry : entries) {
                        if (entry.tag() != null) {
                           CompoundTag effects = ((CompoundTag)entry.tag()).getCompoundTag("effects");
                           CompoundTag particle = effects.getCompoundTag("particle");
                           if (particle != null) {
                              CompoundTag particleOptions = particle.getCompoundTag("options");
                              String particleType = particleOptions.getString("type");
                              this.updateParticleFormat(particleOptions, Key.stripMinecraftNamespace(particleType));
                           }
                        }
                     }
                  }

                  wrapper.write(
                     Types.REGISTRY_ENTRY_ARRAY, ((Protocol1_21_2To1_21)this.protocol).getRegistryDataRewriter().handle(wrapper.user(), registryKey, entries)
                  );
               }
            }
         );
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.LOGIN, new PacketHandlers() {
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
            this.read(Types.VAR_INT);
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.RESPAWN, wrapper -> {
         int dimensionId = (Integer)wrapper.passthrough(Types.VAR_INT);
         String world = (String)wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.OPTIONAL_GLOBAL_POSITION);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.read(Types.VAR_INT);
         EntityTracker tracker = this.tracker(wrapper.user());
         if (tracker.currentWorld() != null && !tracker.currentWorld().equals(world)) {
            BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)wrapper.user().storables(this.protocol);
            storables.setSignStorage(new SignStorage());
         }

         this.trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);
      });
      ((Protocol1_21_2To1_21)this.protocol)
         .registerClientbound(ClientboundPackets1_21_2.ENTITY_POSITION_SYNC, ClientboundPackets1_21.TELEPORT_ENTITY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.read(Types.DOUBLE);
            wrapper.read(Types.DOUBLE);
            wrapper.read(Types.DOUBLE);
            float yaw = (Float)wrapper.read(Types.FLOAT);
            float pitch = (Float)wrapper.read(Types.FLOAT);
            this.writePackedRotation(wrapper, yaw, pitch);
         });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.PLAYER_ROTATION, ClientboundPackets1_21.PLAYER_LOOK_AT, wrapper -> {
         float yaw = (Float)wrapper.read(Types.FLOAT);
         float pitch = (Float)wrapper.read(Types.FLOAT);
         double yRadians = Math.toRadians(yaw);
         double xRadians = Math.toRadians(pitch);
         double factor = -Math.cos(-xRadians);
         double deltaX = Math.sin(-yRadians - (float) Math.PI) * factor;
         double deltaY = Math.sin(-xRadians);
         double deltaZ = Math.cos(-yRadians - (float) Math.PI) * factor;
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         wrapper.write(Types.VAR_INT, 0);
         wrapper.write(Types.DOUBLE, storage.x() + deltaX);
         wrapper.write(Types.DOUBLE, storage.y() + deltaY);
         wrapper.write(Types.DOUBLE, storage.z() + deltaZ);
         wrapper.write(Types.BOOLEAN, false);
         PacketWrapper entityMotionPacket = PacketWrapper.create(ServerboundPackets1_21_2.MOVE_PLAYER_ROT, wrapper.user());
         entityMotionPacket.write(Types.FLOAT, yaw);
         entityMotionPacket.write(Types.FLOAT, pitch);
         entityMotionPacket.write(Types.UNSIGNED_BYTE, (short)0);
         entityMotionPacket.sendToServer(Protocol1_21_2To1_21.class);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.TELEPORT_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         double x = (Double)wrapper.passthrough(Types.DOUBLE);
         double y = (Double)wrapper.passthrough(Types.DOUBLE);
         double z = (Double)wrapper.passthrough(Types.DOUBLE);
         double movementX = (Double)wrapper.read(Types.DOUBLE);
         double movementY = (Double)wrapper.read(Types.DOUBLE);
         double movementZ = (Double)wrapper.read(Types.DOUBLE);
         float yaw = (Float)wrapper.read(Types.FLOAT);
         float pitch = (Float)wrapper.read(Types.FLOAT);
         this.writePackedRotation(wrapper, yaw, pitch);
         int relativeArguments = (Integer)wrapper.read(Types.INT);
         wrapper.send(Protocol1_21_2To1_21.class);
         wrapper.cancel();
         this.handleRelativeArguments(wrapper, x, y, z, yaw, pitch, relativeArguments, movementX, movementY, movementZ, entityId);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.PLAYER_POSITION, wrapper -> {
         int teleportId = (Integer)wrapper.read(Types.VAR_INT);
         double x = (Double)wrapper.passthrough(Types.DOUBLE);
         double y = (Double)wrapper.passthrough(Types.DOUBLE);
         double z = (Double)wrapper.passthrough(Types.DOUBLE);
         double movementX = (Double)wrapper.read(Types.DOUBLE);
         double movementY = (Double)wrapper.read(Types.DOUBLE);
         double movementZ = (Double)wrapper.read(Types.DOUBLE);
         float yaw = (Float)wrapper.passthrough(Types.FLOAT);
         float pitch = (Float)wrapper.passthrough(Types.FLOAT);
         int relativeArguments = (Integer)wrapper.read(Types.INT);
         wrapper.write(Types.BYTE, (byte)relativeArguments);
         wrapper.write(Types.VAR_INT, teleportId);
         wrapper.send(Protocol1_21_2To1_21.class);
         wrapper.cancel();
         this.handleRelativeArguments(wrapper, x, y, z, yaw, pitch, relativeArguments, movementX, movementY, movementZ, null);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.PLAYER_COMMAND, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int action = (Integer)wrapper.passthrough(Types.VAR_INT);
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         if (action == 0) {
            storage.setPlayerCommandTrackedSneaking(true);
         } else if (action == 1) {
            storage.setPlayerCommandTrackedSneaking(false);
         } else if (action == 3) {
            storage.setPlayerCommandTrackedSprinting(true);
         } else if (action == 4) {
            storage.setPlayerCommandTrackedSprinting(false);
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.PLAYER_INPUT, wrapper -> {
         float sideways = (Float)wrapper.read(Types.FLOAT);
         float forward = (Float)wrapper.read(Types.FLOAT);
         byte flags = (Byte)wrapper.read(Types.BYTE);
         byte updatedFlags = 0;
         if (forward > 0.0F) {
            updatedFlags = (byte)(updatedFlags | 1);
         } else if (forward < 0.0F) {
            updatedFlags = (byte)(updatedFlags | 2);
         }

         if (sideways > 0.0F) {
            updatedFlags = (byte)(updatedFlags | 4);
         } else if (sideways < 0.0F) {
            updatedFlags = (byte)(updatedFlags | 8);
         }

         if ((flags & 1) != 0) {
            updatedFlags = (byte)(updatedFlags | 16);
         }

         boolean sneaking = (flags & 2) != 0;
         if (sneaking) {
            updatedFlags = (byte)(updatedFlags | 32);
         }

         wrapper.write(Types.BYTE, updatedFlags);
         this.sendSneakingPlayerCommand(wrapper, sneaking);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS, wrapper -> {
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         this.endPreviousTick(wrapper, storage);
         storage.setPosFromPacket(wrapper);
         this.fixOnGround(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, wrapper -> {
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         this.endPreviousTick(wrapper, storage);
         storage.setPosRotFromPacket(wrapper);
         this.fixOnGround(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_ROT, wrapper -> {
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         this.endPreviousTick(wrapper, storage);
         storage.setRotFromPacket(wrapper);
         this.fixOnGround(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_STATUS_ONLY, wrapper -> {
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         this.endPreviousTick(wrapper, storage);
         this.fixOnGround(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_VEHICLE, wrapper -> {
         PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
         storage.setPosFromPacket(wrapper);
      });
      ((Protocol1_21_2To1_21)this.protocol).replaceClientbound(ClientboundPackets1_21_2.PLAYER_INFO_UPDATE, wrapper -> {
         BitSet actions = (BitSet)wrapper.read(Types.PROFILE_ACTIONS_ENUM1_21_2);
         BitSet updatedActions = new BitSet(6);

         for (int i = 0; i < 6; i++) {
            if (actions.get(i)) {
               updatedActions.set(i);
            }
         }

         wrapper.write(Types.PROFILE_ACTIONS_ENUM1_19_3, updatedActions);
         int entries = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int ix = 0; ix < entries; ix++) {
            wrapper.passthrough(Types.UUID);
            if (actions.get(0)) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
            }

            if (actions.get(1) && (Boolean)wrapper.passthrough(Types.BOOLEAN)) {
               wrapper.passthrough(Types.UUID);
               wrapper.passthrough(Types.PROFILE_KEY);
            }

            if (actions.get(2)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(3)) {
               wrapper.passthrough(Types.BOOLEAN);
            }

            if (actions.get(4)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(5)) {
               Tag displayName = (Tag)wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG);
               ((Protocol1_21_2To1_21)this.protocol).getComponentRewriter().processTag(wrapper.user(), displayName);
            }

            if (actions.get(6)) {
               wrapper.read(Types.VAR_INT);
            }
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).registerClientbound(ClientboundPackets1_21_2.SET_PASSENGERS, wrapper -> {
         int vehicleId = (Integer)wrapper.passthrough(Types.VAR_INT);
         int[] passengerIds = (int[])wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
         BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)wrapper.user().storables(this.protocol);
         ClientVehicleStorage storage = storables.clientVehicleStorage();
         if (storage != null && vehicleId == storage.vehicleId()) {
            storables.setClientVehicleStorage(null);
            this.sendSneakingPlayerCommand(wrapper, false);
         }

         int clientEntityId = this.tracker(wrapper.user()).clientEntityId();

         for (int passenger : passengerIds) {
            if (passenger == clientEntityId) {
               storables.setClientVehicleStorage(new ClientVehicleStorage(vehicleId));
               break;
            }
         }
      });
      ((Protocol1_21_2To1_21)this.protocol).appendClientbound(ClientboundPackets1_21_2.REMOVE_ENTITIES, wrapper -> {
         BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)wrapper.user().storables(this.protocol);
         ClientVehicleStorage vehicleStorage = storables.clientVehicleStorage();
         if (vehicleStorage != null) {
            int[] entityIds = (int[])wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);

            for (int entityId : entityIds) {
               if (entityId == vehicleStorage.vehicleId()) {
                  storables.setClientVehicleStorage(null);
                  this.sendSneakingPlayerCommand(wrapper, false);
                  break;
               }
            }
         }
      });
   }

   private void updateParticleFormat(CompoundTag options, String particleType) {
      if ("dust_color_transition".equals(particleType)) {
         this.replaceColor(options, "from_color");
         this.replaceColor(options, "to_color");
      } else if ("dust".equals(particleType)) {
         this.replaceColor(options, "color");
      }
   }

   private void replaceColor(CompoundTag options, String to_color) {
      IntTag toColorTag = options.getIntTag(to_color);
      if (toColorTag != null) {
         int rgb = toColorTag.asInt();
         float r = (rgb >> 16 & 0xFF) / 255.0F;
         float g = (rgb >> 8 & 0xFF) / 255.0F;
         float b = (rgb & 0xFF) / 255.0F;
         options.put(to_color, new ListTag(List.of(new FloatTag(r), new FloatTag(g), new FloatTag(b))));
      }
   }

   private void writePackedRotation(PacketWrapper wrapper, float yaw, float pitch) {
      wrapper.write(Types.BYTE, (byte)Math.floor(yaw * 256.0F / 360.0F));
      wrapper.write(Types.BYTE, (byte)Math.floor(pitch * 256.0F / 360.0F));
   }

   private void handleRelativeArguments(
      PacketWrapper wrapper,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      int relativeArguments,
      double movementX,
      double movementY,
      double movementZ,
      @Nullable Integer entityId
   ) {
      PlayerStorage storage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
      if ((relativeArguments & 1) != 0) {
         x += storage.x();
      }

      if ((relativeArguments & 2) != 0) {
         y += storage.y();
      }

      if ((relativeArguments & 4) != 0) {
         z += storage.z();
      }

      if ((relativeArguments & 8) != 0) {
         yaw += storage.yRot();
      }

      if ((relativeArguments & 16) != 0) {
         pitch += storage.xRot();
      }

      if ((relativeArguments & 256) != 0) {
         double deltaYaw = Math.toRadians(storage.yRot() - yaw);
         double deltaYawCos = Math.cos(deltaYaw);
         double deltaYawSin = Math.sin(deltaYaw);
         movementX = movementX * deltaYawCos + movementZ * deltaYawSin;
         movementZ = movementZ * deltaYawCos - movementX * deltaYawSin;
         double deltaPitch = Math.toRadians(storage.xRot() - pitch);
         double deltaPitchCos = Math.cos(deltaPitch);
         double deltaPitchSin = Math.sin(deltaPitch);
         movementY = movementY * deltaPitchCos + movementZ * deltaPitchSin;
         movementZ = movementZ * deltaPitchCos - movementY * deltaPitchSin;
      }

      boolean relativeDeltaX = (relativeArguments & 32) != 0;
      boolean relativeDeltaY = (relativeArguments & 64) != 0;
      boolean relativeDeltaZ = (relativeArguments & 128) != 0;
      storage.setPosition(x, y, z);
      storage.setRotation(yaw, pitch);
      if (relativeDeltaX && relativeDeltaY && relativeDeltaZ) {
         if (entityId != null && entityId != this.tracker(wrapper.user()).clientEntityId()) {
            return;
         }

         PacketWrapper explosionPacket = wrapper.create(ClientboundPackets1_21.EXPLODE);
         explosionPacket.write(Types.DOUBLE, 0.0);
         explosionPacket.write(Types.DOUBLE, 20000.0);
         explosionPacket.write(Types.DOUBLE, 0.0);
         explosionPacket.write(Types.FLOAT, 0.0F);
         explosionPacket.write(Types.VAR_INT, 0);
         explosionPacket.write(Types.FLOAT, (float)movementX);
         explosionPacket.write(Types.FLOAT, (float)movementY);
         explosionPacket.write(Types.FLOAT, (float)movementZ);
         explosionPacket.write(Types.VAR_INT, 0);
         explosionPacket.write(VersionedTypes.V1_21.particle(), new Particle(0));
         explosionPacket.write(VersionedTypes.V1_21.particle(), new Particle(0));
         explosionPacket.write(Types.SOUND_EVENT, Holder.of(new SoundEvent("intentionally_empty", null)));
         explosionPacket.send(Protocol1_21_2To1_21.class);
      } else if (!relativeDeltaX && !relativeDeltaY && !relativeDeltaZ) {
         PacketWrapper entityMotionPacket = wrapper.create(ClientboundPackets1_21.SET_ENTITY_MOTION);
         entityMotionPacket.write(Types.VAR_INT, entityId != null ? entityId : this.tracker(wrapper.user()).clientEntityId());
         entityMotionPacket.write(Types.SHORT, VelocityUtil.toLegacyVelocity(movementX));
         entityMotionPacket.write(Types.SHORT, VelocityUtil.toLegacyVelocity(movementY));
         entityMotionPacket.write(Types.SHORT, VelocityUtil.toLegacyVelocity(movementZ));
         entityMotionPacket.send(Protocol1_21_2To1_21.class);
      } else if (!this.warned) {
         ((Protocol1_21_2To1_21)this.protocol)
            .getLogger()
            .warning(
               "Mixed combinations of relative and absolute delta movements are not supported for 1.21.1 players. This will result in incorrect movement for the player. "
            );
         this.warned = true;
      }
   }

   private void sendSneakingPlayerCommand(PacketWrapper wrapper, boolean sneaking) {
      PlayerStorage sneakingStorage = ((BackwardsStorables1_21_2)wrapper.user().storables(this.protocol)).playerStorage();
      if (sneakingStorage.setSneaking(sneaking)) {
         PacketWrapper playerCommandPacket = wrapper.create(ServerboundPackets1_21_2.PLAYER_COMMAND);
         playerCommandPacket.write(Types.VAR_INT, this.tracker(wrapper.user()).clientEntityId());
         playerCommandPacket.write(Types.VAR_INT, sneaking ? 0 : 1);
         playerCommandPacket.write(Types.VAR_INT, 0);
         playerCommandPacket.sendToServer(Protocol1_21_2To1_21.class);
      }
   }

   private int boatTypeFromEntityType(EntityType type) {
      if (type == EntityTypes1_21_2.OAK_BOAT) {
         return 0;
      } else if (type == EntityTypes1_21_2.SPRUCE_BOAT) {
         return 1;
      } else if (type == EntityTypes1_21_2.BIRCH_BOAT) {
         return 2;
      } else if (type == EntityTypes1_21_2.JUNGLE_BOAT) {
         return 3;
      } else if (type == EntityTypes1_21_2.ACACIA_BOAT) {
         return 4;
      } else if (type == EntityTypes1_21_2.CHERRY_BOAT) {
         return 5;
      } else if (type == EntityTypes1_21_2.DARK_OAK_BOAT) {
         return 6;
      } else if (type == EntityTypes1_21_2.MANGROVE_BOAT) {
         return 7;
      } else {
         return type == EntityTypes1_21_2.BAMBOO_RAFT ? 8 : 0;
      }
   }

   private int chestBoatTypeFromEntityType(EntityType type) {
      if (type == EntityTypes1_21_2.OAK_CHEST_BOAT) {
         return 0;
      } else if (type == EntityTypes1_21_2.SPRUCE_CHEST_BOAT) {
         return 1;
      } else if (type == EntityTypes1_21_2.BIRCH_CHEST_BOAT) {
         return 2;
      } else if (type == EntityTypes1_21_2.JUNGLE_CHEST_BOAT) {
         return 3;
      } else if (type == EntityTypes1_21_2.ACACIA_CHEST_BOAT) {
         return 4;
      } else if (type == EntityTypes1_21_2.CHERRY_CHEST_BOAT) {
         return 5;
      } else if (type == EntityTypes1_21_2.DARK_OAK_CHEST_BOAT) {
         return 6;
      } else if (type == EntityTypes1_21_2.MANGROVE_CHEST_BOAT) {
         return 7;
      } else {
         return type == EntityTypes1_21_2.BAMBOO_CHEST_RAFT ? 8 : 0;
      }
   }

   private void endPreviousTick(PacketWrapper wrapper, PlayerStorage storage) {
      if (storage.startTick()) {
         wrapper.create(ServerboundPackets1_21_2.CLIENT_TICK_END).sendToServer(Protocol1_21_2To1_21.class);
      }
   }

   private void fixOnGround(PacketWrapper wrapper) {
      boolean data = (Boolean)wrapper.read(Types.BOOLEAN);
      wrapper.write(Types.UNSIGNED_BYTE, Short.valueOf((short)(data ? 1 : 0)));
   }

   protected void registerRewrites() {
      this.dataTypeMapper().register();
      this.registerEntityDataTypeHandler1_20_3(
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).itemType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).blockStateType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).optionalBlockStateType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).particleType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).particlesType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).componentType,
         ((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_21_2.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_21_2.CREAKING).cancel(17);
      this.filter().type(EntityTypes1_21_2.CREAKING).cancel(16);
      this.filter().type(EntityTypes1_21_2.ABSTRACT_BOAT).addIndex(11);
      this.filter().type(EntityTypes1_21_2.SALMON).removeIndex(17);
      this.filter().type(EntityTypes1_21_2.ABSTRACT_ARROW).removeIndex(10);
      EntityScaleHelper scaleHelper = new EntityScaleHelper(this, ClientboundPackets1_21.UPDATE_ATTRIBUTES);
      scaleHelper.addBabyScale(EntityTypes1_21_2.SQUID, 0.5F, 16);
      scaleHelper.addBabyScale(EntityTypes1_21_2.GLOW_SQUID, 0.5F, 16);
      scaleHelper.addBabyScale(EntityTypes1_21_2.DOLPHIN, 0.65F, 16);
      this.filter().type(EntityTypes1_21_2.AGEABLE_WATER_CREATURE).removeIndex(16);
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_21_2.getTypeFromId(type);
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes1_21_2.CREAKING, EntityTypes1_21_2.WARDEN).tagName();
      this.mapEntityTypeWithData(EntityTypes1_21_2.CREAKING_TRANSIENT, EntityTypes1_21_2.WARDEN).tagName();
   }
}
