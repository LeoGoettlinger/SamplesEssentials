package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.google.common.collect.Maps;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_18;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityPacketRewriter1_19 extends EntityRewriter<ClientboundPackets1_18, Protocol1_18_2To1_19> {
   public EntityPacketRewriter1_19(Protocol1_18_2To1_19 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.registerTracker(ClientboundPackets1_18.ADD_PLAYER, EntityTypes1_19.PLAYER);
      this.registerSetEntityData(ClientboundPackets1_18.SET_ENTITY_DATA, Types1_18.ENTITY_DATA_LIST, Types1_19.ENTITY_DATA_LIST);
      this.protocol.replaceClientbound(ClientboundPackets1_18.ADD_ENTITY, new PacketHandlers() {
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
            this.handler(wrapper -> {
               byte yaw = wrapper.get(Types.BYTE, 1);
               wrapper.write(Types.BYTE, yaw);
            });
            this.map(Types.INT, Types.VAR_INT);
            this.handler(EntityPacketRewriter1_19.this.trackerHandler());
            this.handler(wrapper -> {
               int entityId = wrapper.get(Types.VAR_INT, 0);
               EntityType entityType = EntityPacketRewriter1_19.this.<EntityTracker>tracker(wrapper.user()).entityType(entityId);
               if (entityType == EntityTypes1_19.FALLING_BLOCK) {
                  wrapper.set(Types.VAR_INT, 2, EntityPacketRewriter1_19.this.protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.VAR_INT, 2)));
               }
            });
         }
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_18.ADD_PAINTING,
            ClientboundPackets1_19.ADD_ENTITY,
            wrapper -> {
               int entityId = wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.UUID);
               wrapper.write(Types.VAR_INT, EntityTypes1_19.PAINTING.getId());
               int motive = wrapper.read(Types.VAR_INT);
               BlockPosition blockPosition = wrapper.read(Types.BLOCK_POSITION1_14);
               byte direction = wrapper.read(Types.BYTE);
               wrapper.write(Types.DOUBLE, blockPosition.x() + 0.5);
               wrapper.write(Types.DOUBLE, blockPosition.y() + 0.5);
               wrapper.write(Types.DOUBLE, blockPosition.z() + 0.5);
               wrapper.write(Types.BYTE, (byte)0);
               wrapper.write(Types.BYTE, (byte)0);
               wrapper.write(Types.BYTE, (byte)0);
               wrapper.write(Types.VAR_INT, this.to3dId(direction));
               wrapper.write(Types.SHORT, (short)0);
               wrapper.write(Types.SHORT, (short)0);
               wrapper.write(Types.SHORT, (short)0);
               wrapper.send(Protocol1_18_2To1_19.class);
               wrapper.cancel();
               PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_19.SET_ENTITY_DATA);
               entityDataPacket.write(Types.VAR_INT, entityId);
               List<EntityData> entityData = new ArrayList<>();
               entityData.add(
                  new EntityData(
                     8, Types1_19.ENTITY_DATA_TYPES.paintingVariantType, this.protocol.getMappingData().getPaintingMappings().getNewIdOrDefault(motive, 0)
                  )
               );
               entityDataPacket.write(Types1_19.ENTITY_DATA_LIST, entityData);
               entityDataPacket.send(Protocol1_18_2To1_19.class);
            }
         );
      this.protocol.registerClientbound(ClientboundPackets1_18.ADD_MOB, ClientboundPackets1_19.ADD_ENTITY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.UUID);
            this.map(Types.VAR_INT);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.handler(wrapper -> {
               byte yaw = wrapper.read(Types.BYTE);
               byte pitch = wrapper.read(Types.BYTE);
               wrapper.write(Types.BYTE, pitch);
               wrapper.write(Types.BYTE, yaw);
            });
            this.map(Types.BYTE);
            this.create(Types.VAR_INT, 0);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.handler(EntityPacketRewriter1_19.this.trackerHandler());
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_18.UPDATE_MOB_EFFECT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.create(Types.OPTIONAL_NAMED_COMPOUND_TAG, null);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_18.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.STRING_ARRAY);
            this.map(Types.NAMED_COMPOUND_TAG);
            this.handler(wrapper -> {
               CompoundTag tag = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
               tag.put("minecraft:chat_type", EntityPacketRewriter1_19.this.protocol.getMappingData().chatRegistry());
               ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(tag, "dimension_type");
               Map<String, DimensionData> dimensionDataMap = new HashMap<>(dimensions.size());
               Map<CompoundTag, String> dimensionsMap = new HashMap<>(dimensions.size());

               for (CompoundTag dimension : dimensions) {
                  NumberTag idTag = dimension.getNumberTag("id");
                  CompoundTag element = dimension.getCompoundTag("element");
                  String name = dimension.getStringTag("name").getValue();
                  EntityPacketRewriter1_19.this.addMonsterSpawnData(element);
                  dimensionDataMap.put(Key.stripMinecraftNamespace(name), new DimensionDataImpl(idTag.asInt(), element));
                  dimensionsMap.put(element.copy(), name);
               }

               EntityPacketRewriter1_19.this.<EntityTracker>tracker(wrapper.user()).setDimensions(dimensionDataMap);
               DimensionRegistryStorage registryStorage = wrapper.user().get(DimensionRegistryStorage.class);
               registryStorage.setDimensions(dimensionsMap);
               EntityPacketRewriter1_19.this.writeDimensionKey(wrapper, registryStorage);
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
            this.create(Types.OPTIONAL_GLOBAL_POSITION, null);
            this.handler(EntityPacketRewriter1_19.this.playerTrackerHandler());
            this.handler(EntityPacketRewriter1_19.this.worldDataTrackerHandlerByKey());
            this.handler(EntityPacketRewriter1_19.this.biomeSizeTracker());
            this.handler(wrapper -> {
               PacketWrapper displayPreviewPacket = wrapper.create(ClientboundPackets1_19.SET_DISPLAY_CHAT_PREVIEW);
               displayPreviewPacket.write(Types.BOOLEAN, false);
               displayPreviewPacket.scheduleSend(Protocol1_18_2To1_19.class);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_18.RESPAWN, new PacketHandlers() {
         @Override
         public void register() {
            this.handler(wrapper -> EntityPacketRewriter1_19.this.writeDimensionKey(wrapper, wrapper.user().get(DimensionRegistryStorage.class)));
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.create(Types.OPTIONAL_GLOBAL_POSITION, null);
            this.handler(EntityPacketRewriter1_19.this.worldDataTrackerHandlerByKey());
         }
      });
   }

   private void writeDimensionKey(PacketWrapper wrapper, DimensionRegistryStorage registryStorage) {
      CompoundTag currentDimension = wrapper.read(Types.NAMED_COMPOUND_TAG);
      this.addMonsterSpawnData(currentDimension);
      String dimensionKey = registryStorage.dimensionKey(currentDimension);
      if (dimensionKey == null) {
         if (Via.getConfig().logOtherConversionWarnings()) {
            this.protocol
               .getLogger()
               .warning(
                  "The server tried to send dimension data from a dimension the client wasn't told about on join. Plugins and mods have to make sure they are not creating new dimension types while players are online, and proxies need to make sure they don't scramble dimension data. Received dimension: "
                     + currentDimension
                     + ". Known dimensions: "
                     + registryStorage.dimensions()
               );
         }

         dimensionKey = (String)registryStorage.dimensions()
            .entrySet()
            .stream()
            .map(it -> new Pair<>(it, Maps.difference(currentDimension.getValue(), it.getKey().getValue()).entriesInCommon()))
            .filter(it -> it.value().containsKey("min_y") && it.value().containsKey("height"))
            .max(Comparator.comparingInt(it -> it.value().size()))
            .orElseThrow(() -> new IllegalArgumentException("Dimension not found in registry data from join packet: " + currentDimension))
            .key()
            .getValue();
      }

      wrapper.write(Types.STRING, dimensionKey);
   }

   private int to3dId(int id) {
      return switch (id) {
         case -1 -> 1;
         case 0 -> 3;
         case 1 -> 4;
         case 2 -> 2;
         case 3 -> 5;
         default -> throw new IllegalArgumentException("Unknown 2d id: " + id);
      };
   }

   private void addMonsterSpawnData(CompoundTag dimension) {
      dimension.put("monster_spawn_block_light_limit", new IntTag(0));
      dimension.put("monster_spawn_light_level", new IntTag(11));
   }

   @Override
   protected void registerRewrites() {
      this.filter().mapDataType(Types1_19.ENTITY_DATA_TYPES::byId);
      this.filter().dataType(Types1_19.ENTITY_DATA_TYPES.particleType).handler((event, data) -> {
         Particle particle = (Particle)data.getValue();
         ParticleMappings particleMappings = this.protocol.getMappingData().getParticleMappings();
         if (particle.id() == particleMappings.id("vibration")) {
            particle.getArguments().remove(0);
            String resourceLocation = Key.stripMinecraftNamespace(particle.<String>getArgument(0).getValue());
            if (resourceLocation.equals("entity")) {
               particle.getArguments().add(2, new Particle.ParticleData<>(Types.FLOAT, 0.0F));
            }
         }

         this.protocol.getParticleRewriter().rewriteParticle(event.user(), particle);
      });
      this.registerEntityDataTypeHandler(Types1_19.ENTITY_DATA_TYPES.itemType, Types1_19.ENTITY_DATA_TYPES.optionalBlockStateType, null);
      this.registerBlockStateHandler(EntityTypes1_19.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_19.CAT).index(19).mapDataType(typeId -> Types1_19.ENTITY_DATA_TYPES.catVariantType);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_19.getTypeFromId(type);
   }
}
