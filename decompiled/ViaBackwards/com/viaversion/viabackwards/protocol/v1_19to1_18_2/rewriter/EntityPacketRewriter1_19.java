package com.viaversion.viabackwards.protocol.v1_19to1_18_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.DimensionRegistryStorage;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.EntityTracker1_19;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.LastDeathPosition;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.StoredPainting;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.UUID;

public final class EntityPacketRewriter1_19 extends EntityRewriter<ClientboundPackets1_19, Protocol1_19To1_18_2> {
   public EntityPacketRewriter1_19(Protocol1_19To1_18_2 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      this.registerTracker(ClientboundPackets1_19.ADD_EXPERIENCE_ORB, EntityTypes1_19.EXPERIENCE_ORB);
      this.registerTracker(ClientboundPackets1_19.ADD_PLAYER, EntityTypes1_19.PLAYER);
      this.registerSetEntityData(ClientboundPackets1_19.SET_ENTITY_DATA, Types1_19.ENTITY_DATA_LIST, Types1_18.ENTITY_DATA_LIST);
      ((Protocol1_19To1_18_2)this.protocol).replaceClientbound(ClientboundPackets1_19.ADD_ENTITY, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.VAR_INT);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               byte headYaw = (Byte)wrapper.read(Types.BYTE);
               int data = (Integer)wrapper.read(Types.VAR_INT);
               int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
               int typeId = (Integer)wrapper.get(Types.VAR_INT, 1);
               TrackedEntity entity = EntityPacketRewriter1_19.this.trackAndRewrite(wrapper, typeId, entityId);
               if (entity.entityType().isOrHasParent(EntityTypes1_19.LIVING_ENTITY)) {
                  wrapper.write(Types.BYTE, headYaw);
                  byte pitch = (Byte)wrapper.get(Types.BYTE, 0);
                  byte yaw = (Byte)wrapper.get(Types.BYTE, 1);
                  wrapper.set(Types.BYTE, 0, yaw);
                  wrapper.set(Types.BYTE, 1, pitch);
                  wrapper.setPacketType(ClientboundPackets1_18.ADD_MOB);
               } else if (entity.entityType() == EntityTypes1_19.PAINTING) {
                  wrapper.cancel();
                  int x = ((Double)wrapper.get(Types.DOUBLE, 0)).intValue();
                  int y = ((Double)wrapper.get(Types.DOUBLE, 1)).intValue();
                  int z = ((Double)wrapper.get(Types.DOUBLE, 2)).intValue();
                  BlockPosition position = new BlockPosition(x, y, z);
                  entity.put(new StoredPainting(entityId, (UUID)wrapper.get(Types.UUID, 0), position, data));
               } else {
                  if (entity.entityType() == EntityTypes1_19.FALLING_BLOCK) {
                     data = ((Protocol1_19To1_18_2)EntityPacketRewriter1_19.this.protocol).getMappingData().getNewBlockStateId(data);
                  }

                  wrapper.write(Types.INT, data);
               }
            });
         }
      });
      ((Protocol1_19To1_18_2)this.protocol).registerClientbound(ClientboundPackets1_19.TELEPORT_ENTITY, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         if (this.tracker(wrapper.user()).entityType(entityId) == EntityTypes1_19.PAINTING) {
            double x = (Double)wrapper.read(Types.DOUBLE);
            double y = (Double)wrapper.read(Types.DOUBLE);
            double z = (Double)wrapper.read(Types.DOUBLE);
            TrackedEntity entity = this.tracker(wrapper.user()).entity(entityId);
            StoredPainting storedPainting = entity != null ? (StoredPainting)entity.get(StoredPainting.class) : null;
            if (storedPainting == null) {
               wrapper.write(Types.DOUBLE, x + 1.0);
               wrapper.write(Types.DOUBLE, y + 1.0);
               wrapper.write(Types.DOUBLE, z + 1.0);
            } else {
               double width = 0.0;
               double height = 0.0;
               int type = storedPainting.type();
               if (type <= 6) {
                  width = 16.0;
                  height = 16.0;
               } else if (type <= 11) {
                  width = 32.0;
                  height = 16.0;
               } else if (type <= 13) {
                  width = 16.0;
                  height = 32.0;
               } else if (type > 19 && (type < 25 || type > 28)) {
                  if (type == 20) {
                     width = 64.0;
                     height = 32.0;
                  } else if (type <= 23) {
                     width = 64.0;
                     height = 64.0;
                  } else if (type == 24 || type == 29) {
                     width = 64.0;
                     height = 48.0;
                  }
               } else {
                  width = 32.0;
                  height = 32.0;
               }

               double resX = x + 0.5;
               double resY = y + 0.5;
               double resZ = z + 0.5;
               double againstWall = -0.46875;
               switch (storedPainting.direction()) {
                  case 0:
                     resZ += -0.46875;
                     break;
                  case 1:
                     resX -= -0.46875;
                     break;
                  case 2:
                     resZ -= -0.46875;
                     break;
                  case 3:
                     resX += -0.46875;
               }

               double horizontalOffset = width % 32.0 == 0.0 ? 0.5 : 0.0;
               double verticalOffset = height % 32.0 == 0.0 ? 0.5 : 0.0;
               resY += verticalOffset;
               switch (storedPainting.direction()) {
                  case 0:
                     resX += horizontalOffset;
                     break;
                  case 1:
                     resZ += horizontalOffset;
                     break;
                  case 2:
                     resX -= horizontalOffset;
                     break;
                  case 3:
                     resZ -= horizontalOffset;
               }

               wrapper.write(Types.DOUBLE, resX);
               wrapper.write(Types.DOUBLE, resY);
               wrapper.write(Types.DOUBLE, resZ);
            }
         }
      });
      ((Protocol1_19To1_18_2)this.protocol).registerClientbound(ClientboundPackets1_19.UPDATE_MOB_EFFECT, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               wrapper.read(Types.OPTIONAL_NAMED_COMPOUND_TAG);
               if (ViaBackwards.getConfig().mapDarknessEffect()) {
                  EntityTracker1_19 tracker = (EntityTracker1_19)EntityPacketRewriter1_19.this.tracker(wrapper.user());
                  int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
                  int effectId = (Integer)wrapper.get(Types.VAR_INT, 1);
                  if (effectId == 33) {
                     tracker.getAffectedByDarkness().add(entityId);
                     wrapper.set(Types.VAR_INT, 1, 15);
                  } else if (effectId == 15) {
                     tracker.getAffectedByBlindness().add(entityId);
                  }
               }
            });
         }
      });
      ((Protocol1_19To1_18_2)this.protocol).registerClientbound(ClientboundPackets1_19.REMOVE_MOB_EFFECT, new PacketHandlers() {
         protected void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               if (ViaBackwards.getConfig().mapDarknessEffect()) {
                  int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
                  int effectId = (Integer)wrapper.get(Types.VAR_INT, 1);
                  EntityTracker1_19 tracker = (EntityTracker1_19)EntityPacketRewriter1_19.this.tracker(wrapper.user());
                  if (effectId == 33) {
                     tracker.getAffectedByDarkness().rem(entityId);
                     if (!tracker.getAffectedByBlindness().contains(entityId)) {
                        wrapper.set(Types.VAR_INT, 1, 15);
                     }
                  } else if (effectId == 15) {
                     tracker.getAffectedByBlindness().rem(entityId);
                     if (tracker.getAffectedByDarkness().contains(entityId)) {
                        wrapper.cancel();
                     }
                  }
               }
            });
         }
      });
      ((Protocol1_19To1_18_2)this.protocol).registerClientbound(ClientboundPackets1_19.LOGIN, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.STRING_ARRAY);
            this.map(Types.NAMED_COMPOUND_TAG);
            this.handler(wrapper -> {
               DimensionRegistryStorage dimensionRegistryStorage = (DimensionRegistryStorage)wrapper.user().get(DimensionRegistryStorage.class);
               dimensionRegistryStorage.clear();
               String dimensionKey = Key.stripMinecraftNamespace((String)wrapper.read(Types.STRING));
               CompoundTag registry = (CompoundTag)wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
               ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(registry, "dimension_type");
               boolean found = false;

               for (CompoundTag dimension : dimensions) {
                  String name = Key.stripMinecraftNamespace(dimension.getString("name"));
                  CompoundTag dimensionData = dimension.getCompoundTag("element");
                  dimensionRegistryStorage.addDimension(name, dimensionData.copy());
                  if (!found && name.equals(dimensionKey)) {
                     wrapper.write(Types.NAMED_COMPOUND_TAG, dimensionData);
                     found = true;
                  }
               }

               if (!found) {
                  throw new IllegalArgumentException("Could not find dimension " + dimensionKey + " in dimension registry");
               } else {
                  ListTag<CompoundTag> biomes = TagUtil.getRegistryEntries(registry, "worldgen/biome");

                  for (CompoundTag biome : biomes) {
                     CompoundTag biomeCompound = biome.getCompoundTag("element");
                     biomeCompound.putString("category", "none");
                  }

                  EntityPacketRewriter1_19.this.tracker(wrapper.user()).setBiomesSent(biomes.size());

                  for (CompoundTag chatType : TagUtil.removeRegistryEntries(registry, "chat_type")) {
                     NumberTag idTag = chatType.getNumberTag("id");
                     dimensionRegistryStorage.addChatType(idTag.asInt(), chatType);
                  }
               }
            });
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               GlobalBlockPosition lastDeathPosition = (GlobalBlockPosition)wrapper.read(Types.OPTIONAL_GLOBAL_POSITION);
               if (lastDeathPosition != null) {
                  wrapper.user().put(new LastDeathPosition(lastDeathPosition));
               } else {
                  wrapper.user().remove(LastDeathPosition.class);
               }
            });
            this.handler(EntityPacketRewriter1_19.this.worldDataTrackerHandler(1));
            this.handler(EntityPacketRewriter1_19.this.playerTrackerHandler());
         }
      });
      ((Protocol1_19To1_18_2)this.protocol).registerClientbound(ClientboundPackets1_19.RESPAWN, new PacketHandlers() {
         public void register() {
            this.handler(wrapper -> {
               String dimensionKey = (String)wrapper.read(Types.STRING);
               CompoundTag dimension = ((DimensionRegistryStorage)wrapper.user().get(DimensionRegistryStorage.class)).dimension(dimensionKey);
               if (dimension == null) {
                  throw new IllegalArgumentException("Could not find dimension " + dimensionKey + " in dimension registry");
               } else {
                  wrapper.write(Types.NAMED_COMPOUND_TAG, dimension);
               }
            });
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               GlobalBlockPosition lastDeathPosition = (GlobalBlockPosition)wrapper.read(Types.OPTIONAL_GLOBAL_POSITION);
               if (lastDeathPosition != null) {
                  wrapper.user().put(new LastDeathPosition(lastDeathPosition));
               } else {
                  wrapper.user().remove(LastDeathPosition.class);
               }
            });
            this.handler(EntityPacketRewriter1_19.this.worldDataTrackerHandler(0));
         }
      });
      ((Protocol1_19To1_18_2)this.protocol).registerClientbound(ClientboundPackets1_19.PLAYER_INFO, wrapper -> {
         int action = (Integer)wrapper.passthrough(Types.VAR_INT);
         int entries = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < entries; i++) {
            wrapper.passthrough(Types.UUID);
            if (action == 0) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.OPTIONAL_COMPONENT);
               wrapper.read(Types.OPTIONAL_PROFILE_KEY);
            } else if (action == 1 || action == 2) {
               wrapper.passthrough(Types.VAR_INT);
            } else if (action == 3) {
               wrapper.passthrough(Types.OPTIONAL_COMPONENT);
            }
         }
      });
   }

   protected void registerRewrites() {
      this.filter().handler((event, data) -> {
         if (data.dataType().typeId() <= Types1_18.ENTITY_DATA_TYPES.poseType.typeId()) {
            data.setDataType(Types1_18.ENTITY_DATA_TYPES.byId(data.dataType().typeId()));
         }

         EntityDataType type = data.dataType();
         if (type == Types1_18.ENTITY_DATA_TYPES.particleType) {
            Particle particle = (Particle)data.getValue();
            ParticleMappings particleMappings = ((Protocol1_19To1_18_2)this.protocol).getMappingData().getParticleMappings();
            if (particle.id() == particleMappings.id("sculk_charge")) {
               event.cancel();
               return;
            }

            if (particle.id() == particleMappings.id("shriek")) {
               event.cancel();
               return;
            }

            if (particle.id() == particleMappings.id("vibration")) {
               event.cancel();
               return;
            }

            ((Protocol1_19To1_18_2)this.protocol).getParticleRewriter().rewriteParticle(event.user(), particle);
         } else if (type == Types1_18.ENTITY_DATA_TYPES.poseType) {
            int pose = (Integer)data.value();
            if (pose >= 8) {
               data.setValue(0);
            }
         }
      });
      this.registerEntityDataTypeHandler(
         Types1_18.ENTITY_DATA_TYPES.itemType,
         null,
         Types1_18.ENTITY_DATA_TYPES.optionalBlockStateType,
         null,
         Types1_18.ENTITY_DATA_TYPES.componentType,
         Types1_18.ENTITY_DATA_TYPES.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_19.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_19.PAINTING).index(8).handler((event, data) -> {
         event.cancel();
         TrackedEntity entity = this.tracker(event.user()).entity(event.entityId());
         StoredPainting storedPainting = entity != null ? (StoredPainting)entity.get(StoredPainting.class) : null;
         if (storedPainting != null) {
            int type = (Integer)data.value();
            storedPainting.setType(type);

            type = switch (type) {
               case 25 -> 19;
               case 26 -> 16;
               case 27 -> 18;
               case 28 -> 14;
               case 29 -> 25;
               default -> type;
            };
            PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_18.ADD_PAINTING, event.user());
            packet.write(Types.VAR_INT, storedPainting.entityId());
            packet.write(Types.UUID, storedPainting.uuid());
            packet.write(Types.VAR_INT, type);
            packet.write(Types.BLOCK_POSITION1_14, storedPainting.position());
            packet.write(Types.BYTE, storedPainting.direction());

            try {
               packet.send(Protocol1_19To1_18_2.class);
            } catch (Exception var8) {
               throw new RuntimeException(var8);
            }
         }
      });
      this.filter().type(EntityTypes1_19.CAT).index(19).handler((event, data) -> data.setDataType(Types1_18.ENTITY_DATA_TYPES.varIntType));
      this.filter().type(EntityTypes1_19.FROG).cancel(16);
      this.filter().type(EntityTypes1_19.FROG).cancel(17);
      this.filter().type(EntityTypes1_19.FROG).cancel(18);
      this.filter().type(EntityTypes1_19.WARDEN).cancel(16);
      this.filter().type(EntityTypes1_19.GOAT).cancel(18);
      this.filter().type(EntityTypes1_19.GOAT).cancel(19);
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes1_19.FROG, EntityTypes1_19.RABBIT).jsonName();
      this.mapEntityTypeWithData(EntityTypes1_19.TADPOLE, EntityTypes1_19.PUFFERFISH).jsonName();
      this.mapEntityTypeWithData(EntityTypes1_19.CHEST_BOAT, EntityTypes1_19.BOAT);
      this.mapEntityTypeWithData(EntityTypes1_19.WARDEN, EntityTypes1_19.IRON_GOLEM).jsonName();
      this.mapEntityTypeWithData(EntityTypes1_19.ALLAY, EntityTypes1_19.VEX).jsonName();
   }

   public EntityType typeFromId(int typeId) {
      return EntityTypes1_19.getTypeFromId(typeId);
   }
}
