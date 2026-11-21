package com.viaversion.viaversion.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.Int2IntMapMappings;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataFilter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEventImpl;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class EntityRewriter<C extends ClientboundPacketType, T extends Protocol<C, ?, ?, ?>>
   extends RewriterBase<T>
   implements com.viaversion.viaversion.api.rewriter.EntityRewriter<T> {
   protected final List<EntityDataFilter> entityDataFilters = new ArrayList<>();
   protected final boolean trackMappedType;
   protected Mappings typeMappings;
   private EntityType fallingBlockType;

   protected EntityRewriter(T protocol) {
      this(protocol, true);
   }

   protected EntityRewriter(T protocol, boolean trackMappedType) {
      super(protocol);
      this.trackMappedType = trackMappedType;
      protocol.put(this);
   }

   public EntityDataFilter.Builder filter() {
      return new EntityDataFilter.Builder(this);
   }

   public EntityDataFilter.DataTypeMapper dataTypeMapper() {
      Preconditions.checkNotNull(this.protocol.mappedTypes(), "Protocol does not override mappedTypes, use filter().mapDataType instead");
      return new EntityDataFilter.DataTypeMapper(this);
   }

   public void registerFilter(EntityDataFilter filter) {
      Preconditions.checkArgument(!this.entityDataFilters.contains(filter));
      this.entityDataFilters.add(filter);
   }

   @Override
   public void handleEntityData(int entityId, List<EntityData> dataList, UserConnection connection) {
      TrackedEntity entity = this.<EntityTracker>tracker(connection).entity(entityId);
      EntityType type = entity != null ? entity.entityType() : null;
      int size = dataList.size();

      for (int i = 0; i < size; i++) {
         EntityData entityData = dataList.get(i);
         EntityDataHandlerEvent event = null;

         for (EntityDataFilter filter : this.entityDataFilters) {
            if (filter.isFiltered(type, entityData)) {
               if (event == null) {
                  event = new EntityDataHandlerEventImpl(connection, entity, entityId, entityData, dataList);
               }

               try {
                  filter.handler().handle(event, entityData);
               } catch (Exception var13) {
                  this.logException(var13, type, dataList, entityData);
                  dataList.remove(i--);
                  size--;
                  break;
               }

               if (event.cancelled()) {
                  dataList.remove(i--);
                  size--;
                  break;
               }
            }
         }

         if (event != null && event.hasExtraData()) {
            dataList.addAll(event.extraData());
         }
      }

      if (entity != null) {
         entity.sentEntityData(true);
      }
   }

   @Override
   public int newEntityId(int id) {
      return this.typeMappings != null ? this.typeMappings.getNewIdOrDefault(id, id) : id;
   }

   @Override
   public void onMappingDataLoaded() {
      if (this.protocol.getMappingData() != null && !Mappings.isFullIdentity(this.protocol.getMappingData().getEntityMappings())) {
         this.mapTypes();
      }
   }

   @Nullable
   public Mappings typeMappings() {
      return this.typeMappings;
   }

   @Override
   public String mappedEntityIdentifier(String identifier) {
      if (this.typeMappings instanceof FullMappings fullMappings) {
         String mappedIdentifier = fullMappings.mappedIdentifier(identifier);
         if (mappedIdentifier != null) {
            return mappedIdentifier;
         }
      }

      return identifier;
   }

   @Nullable
   @Override
   public EntityType typeFromId(String type) {
      FullMappings mappings = this.protocol().getMappingData().getEntityMappings();
      int id = this.trackMappedType ? mappings.mappedId(type) : mappings.id(type);
      return id == -1 ? null : this.typeFromId(id);
   }

   public void mapEntityType(EntityType type, EntityType mappedType) {
      Preconditions.checkArgument(type.getClass() != mappedType.getClass(), "EntityTypes should not be of the same class/enum");
      this.mapEntityType(type.getId(), mappedType.getId());
   }

   protected void mapEntityType(int id, int mappedId) {
      if (this.typeMappings == null) {
         this.typeMappings = Int2IntMapMappings.of();
      }

      this.typeMappings.setNewId(id, mappedId);
   }

   public void mapTypes() {
      Preconditions.checkArgument(this.typeMappings == null, "Type mappings have already been set - manual type mappings should be set *after* this");
      Preconditions.checkNotNull(this.protocol.getMappingData().getEntityMappings(), "Protocol does not have entity mappings");
      this.typeMappings = this.protocol.getMappingData().getEntityMappings();
   }

   public void registerEntityDataTypeHandler(@Nullable EntityDataType itemType, @Nullable EntityDataType blockStateType, @Nullable EntityDataType particleType) {
      this.registerEntityDataTypeHandler(itemType, null, blockStateType, particleType, null);
   }

   public void registerEntityDataTypeHandler(
      @Nullable EntityDataType itemType,
      @Nullable EntityDataType blockStateType,
      @Nullable EntityDataType optionalBlockStateType,
      @Nullable EntityDataType particleType,
      @Nullable EntityDataType particlesType
   ) {
      this.registerEntityDataTypeHandler(itemType, blockStateType, optionalBlockStateType, particleType, particlesType, null, null);
   }

   public void registerEntityDataTypeHandler(
      @Nullable EntityDataType itemType,
      @Nullable EntityDataType blockStateType,
      @Nullable EntityDataType optionalBlockStateType,
      @Nullable EntityDataType particleType,
      @Nullable EntityDataType particlesType,
      @Nullable EntityDataType componentType,
      @Nullable EntityDataType optionalComponentType
   ) {
      this.filter().handler((event, data) -> {
         EntityDataType type = data.dataType();
         if (type == itemType) {
            data.setValue(this.protocol.getItemRewriter().handleItemToClient(event.user(), data.value()));
         } else if (type == blockStateType) {
            int value = data.<Integer>value();
            data.setValue(this.protocol.getMappingData().getNewBlockStateId(value));
         } else if (type == optionalBlockStateType) {
            int value = data.<Integer>value();
            if (value != 0) {
               data.setValue(this.protocol.getMappingData().getNewBlockStateId(value));
            }
         } else if (type == particleType) {
            this.protocol.getParticleRewriter().rewriteParticle(event.user(), data.value());
         } else if (type == particlesType) {
            Particle[] particles = data.value();

            for (Particle particle : particles) {
               this.protocol.getParticleRewriter().rewriteParticle(event.user(), particle);
            }
         } else if ((type == componentType || type == optionalComponentType) && this.protocol.getComponentRewriter() != null) {
            Tag component = data.value();
            this.protocol.getComponentRewriter().processTag(event.user(), component);
         }
      });
   }

   public void registerBlockStateHandler(EntityType entityType, int index) {
      this.filter().type(entityType).index(index).handler((event, data) -> {
         int state = (Integer)data.getValue();
         data.setValue(this.protocol.getMappingData().getNewBlockStateId(state));
      });
   }

   public void registerTracker(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.VAR_INT);
         this.trackerHandler().handle(wrapper);
      });
   }

   public void registerTrackerWithData(C packetType) {
      this.protocol.registerClientbound(packetType, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map((Type<T>)Types.UUID);
            this.map(Types.VAR_INT);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.INT);
            this.handler(EntityRewriter.this.trackerHandler());
            this.handler(wrapper -> {
               int entityId = wrapper.get(Types.VAR_INT, 0);
               EntityType entityType = EntityRewriter.this.<EntityTracker>tracker(wrapper.user()).entityType(entityId);
               if (entityType == EntityRewriter.this.fallingBlockType()) {
                  wrapper.set(Types.INT, 0, EntityRewriter.this.protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.INT, 0)));
               }
            });
         }
      });
   }

   public void registerTrackerWithData1_19(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> this.handleAddEntity(wrapper, false));
   }

   public void registerTrackerWithData1_21_9(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> this.handleAddEntity(wrapper, true));
   }

   protected TrackedEntity handleAddEntity(PacketWrapper wrapper, boolean hasMovement) {
      int entityId = wrapper.passthrough(Types.VAR_INT);
      wrapper.passthrough(Types.UUID);
      int entityTypeId = wrapper.passthrough(Types.VAR_INT);
      wrapper.passthrough(Types.DOUBLE);
      wrapper.passthrough(Types.DOUBLE);
      wrapper.passthrough(Types.DOUBLE);
      if (hasMovement) {
         wrapper.passthrough(Types.LOW_PRECISION_VECTOR);
      }

      wrapper.passthrough(Types.BYTE);
      wrapper.passthrough(Types.BYTE);
      wrapper.passthrough(Types.BYTE);
      int data = wrapper.passthrough(Types.VAR_INT);
      TrackedEntity entity = this.trackAndRewrite(wrapper, entityTypeId, entityId);
      if (this.protocol.getMappingData() != null && entity.entityType() == this.fallingBlockType()) {
         int mappedBlockStateId = this.protocol.getMappingData().getNewBlockStateId(data);
         wrapper.set(Types.VAR_INT, 2, mappedBlockStateId);
      }

      return entity;
   }

   public void registerTracker(C packetType, EntityType entityType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         this.<EntityTracker>tracker(wrapper.user()).addEntity(entityId, entityType);
      });
   }

   public void registerRemoveEntities(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int[] entityIds = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
         EntityTracker entityTracker = this.tracker(wrapper.user());

         for (int entity : entityIds) {
            entityTracker.removeEntity(entity);
         }
      });
   }

   public void registerSetEntityData(C packetType, @Nullable Type<List<EntityData>> dataType, Type<List<EntityData>> mappedDataType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         List<EntityData> entityData;
         if (dataType != null) {
            entityData = wrapper.read(dataType);
            wrapper.write(mappedDataType, entityData);
         } else {
            entityData = wrapper.passthrough(mappedDataType);
         }

         this.handleEntityData(entityId, entityData, wrapper.user());
      });
   }

   public void registerSetEntityData(C packetType, Type<List<EntityData>> dataType) {
      this.registerSetEntityData(packetType, null, dataType);
   }

   public void registerSetEntityData(C packetType) {
      this.registerSetEntityData(packetType, this.protocol.types().entityDataList(), this.protocol.mappedTypes().entityDataList());
   }

   public void registerLogin1_20_5(C packetType) {
      this.registerLogin1_20_5(packetType, Types.BYTE);
   }

   public void registerLogin26_3(C packetType) {
      this.registerLogin1_20_5(packetType, Types.VAR_INT);
   }

   public void registerLogin1_20_5(C packetType, Type<? extends Number> gamemodeType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int entityId = wrapper.passthrough(Types.INT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.STRING_ARRAY);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         int dimensionId = wrapper.passthrough(Types.VAR_INT);
         String world = wrapper.passthrough(Types.STRING);
         this.trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);
         wrapper.passthrough(Types.LONG);
         int gamemode = wrapper.passthrough(gamemodeType).intValue();
         this.<EntityTracker>tracker(wrapper.user()).setInstaBuild(gamemode == GameMode.CREATIVE.id());
         this.trackPlayer(wrapper.user(), entityId);
      });
   }

   public void registerRespawn1_20_5(C packetType) {
      this.registerRespawn1_20_5(packetType, Types.BYTE);
   }

   public void registerRespawn26_3(C packetType) {
      this.registerRespawn1_20_5(packetType, Types.VAR_INT);
   }

   public void registerRespawn1_20_5(C packetType, Type<? extends Number> gamemodeType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int dimensionId = wrapper.passthrough(Types.VAR_INT);
         String world = wrapper.passthrough(Types.STRING);
         this.trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);
         wrapper.passthrough(Types.LONG);
         int gamemode = wrapper.passthrough(gamemodeType).intValue();
         this.<EntityTracker>tracker(wrapper.user()).setInstaBuild(gamemode == GameMode.CREATIVE.id());
      });
   }

   public void registerPlayerAbilities(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         byte flags = wrapper.passthrough(Types.BYTE);
         this.<EntityTracker>tracker(wrapper.user()).setInstaBuild((flags & 8) != 0);
      });
   }

   public void registerGameEvent(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         short event = wrapper.passthrough(Types.UNSIGNED_BYTE);
         if (event == 3) {
            int value = (int)Math.floor(wrapper.passthrough(Types.FLOAT) + 0.5F);
            this.<EntityTracker>tracker(wrapper.user()).setInstaBuild(value == GameMode.CREATIVE.id());
         }
      });
   }

   public PacketHandler trackerHandler() {
      return this.trackerAndRewriterHandler(null);
   }

   public PacketHandler playerTrackerHandler() {
      return wrapper -> this.trackPlayer(wrapper.user(), wrapper.get(Types.INT, 0));
   }

   public void trackPlayer(UserConnection connection, int entityId) {
      EntityTracker tracker = this.tracker(connection);
      tracker.setClientEntityId(entityId);
      tracker.addEntity(entityId, tracker.playerType());
   }

   public void trackWorld(UserConnection connection, String world) {
      EntityTracker tracker = this.tracker(connection);
      if (tracker.currentWorld() != null && !tracker.currentWorld().equals(world)) {
         tracker.clearEntities();
      }

      tracker.setCurrentWorld(world);
   }

   public PacketHandler worldDataTrackerHandler(int nbtIndex) {
      return wrapper -> {
         EntityTracker tracker = this.tracker(wrapper.user());
         CompoundTag registryData = wrapper.get(Types.NAMED_COMPOUND_TAG, nbtIndex);
         NumberTag height = registryData.getNumberTag("height");
         if (height != null) {
            int blockHeight = height.asInt();
            tracker.setCurrentWorldSectionHeight(blockHeight >> 4);
         } else {
            this.protocol.getLogger().warning("Height missing in dimension data: " + registryData);
         }

         NumberTag minY = registryData.getNumberTag("min_y");
         if (minY != null) {
            tracker.setCurrentMinY(minY.asInt());
         } else {
            this.protocol.getLogger().warning("Min Y missing in dimension data: " + registryData);
         }

         String world = wrapper.get(Types.STRING, 0);
         this.trackWorld(wrapper.user(), world);
      };
   }

   public PacketHandler worldDataTrackerHandlerByKey() {
      return wrapper -> {
         EntityTracker tracker = this.tracker(wrapper.user());
         String dimensionKey = wrapper.get(Types.STRING, 0);
         DimensionData dimensionData = tracker.dimensionData(dimensionKey);
         if (dimensionData == null) {
            this.protocol.getLogger().severe("Dimension data missing for dimension: " + dimensionKey + ", falling back to overworld");
            dimensionData = tracker.dimensionData("minecraft:overworld");
            Preconditions.checkNotNull(dimensionData, "Overworld data missing");
         }

         tracker.setCurrentWorldSectionHeight(dimensionData.height() >> 4);
         tracker.setCurrentMinY(dimensionData.minY());
         String world = wrapper.get(Types.STRING, 1);
         this.trackWorld(wrapper.user(), world);
      };
   }

   public PacketHandler worldDataTrackerHandlerByKey1_20_5(int dimensionIdIndex) {
      return wrapper -> {
         int dimensionId = wrapper.get(Types.VAR_INT, dimensionIdIndex);
         String world = wrapper.get(Types.STRING, 0);
         this.trackWorldDataByKey1_20_5(wrapper.user(), dimensionId, world);
      };
   }

   public void trackWorldDataByKey1_20_5(UserConnection connection, int dimensionId, String world) {
      EntityTracker tracker = this.tracker(connection);
      DimensionData dimensionData = tracker.dimensionData(dimensionId);
      if (dimensionData == null) {
         this.protocol.getLogger().severe("Dimension data missing for dimension: " + dimensionId + ", falling back to overworld");
         dimensionData = tracker.dimensionData("overworld");
         Preconditions.checkNotNull(dimensionData, "Overworld data missing");
      }

      tracker.setCurrentDimensionId(dimensionId);
      tracker.setCurrentWorldSectionHeight(dimensionData.height() >> 4);
      tracker.setCurrentMinY(dimensionData.minY());
      this.trackWorld(connection, world);
   }

   public PacketHandler biomeSizeTracker() {
      return wrapper -> this.trackBiomeSize(wrapper.user(), wrapper.get(Types.NAMED_COMPOUND_TAG, 0));
   }

   public PacketHandler configurationBiomeSizeTracker() {
      return wrapper -> this.trackBiomeSize(wrapper.user(), wrapper.get(Types.COMPOUND_TAG, 0));
   }

   public void trackBiomeSize(UserConnection connection, CompoundTag registry) {
      ListTag<?> biomes = TagUtil.getRegistryEntries(registry, "worldgen/biome");
      this.<EntityTracker>tracker(connection).setBiomesSent(biomes.size());
   }

   public PacketHandler dimensionDataHandler() {
      return wrapper -> this.cacheDimensionData(wrapper.user(), wrapper.get(Types.NAMED_COMPOUND_TAG, 0));
   }

   public PacketHandler configurationDimensionDataHandler() {
      return wrapper -> this.cacheDimensionData(wrapper.user(), wrapper.get(Types.COMPOUND_TAG, 0));
   }

   public void cacheDimensionData(UserConnection connection, CompoundTag registry) {
      ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(registry, "dimension_type");
      Map<String, DimensionData> dimensionDataMap = new HashMap<>(dimensions.size());

      for (CompoundTag dimension : dimensions) {
         NumberTag idTag = dimension.getNumberTag("id");
         CompoundTag element = dimension.getCompoundTag("element");
         String name = dimension.getStringTag("name").getValue();
         dimensionDataMap.put(Key.stripMinecraftNamespace(name), new DimensionDataImpl(idTag.asInt(), element));
      }

      this.<EntityTracker>tracker(connection).setDimensions(dimensionDataMap);
   }

   public TrackedEntity trackAndRewrite(PacketWrapper wrapper, int typeId, int entityId) {
      int mappedTypeId = this.newEntityId(typeId);
      if (mappedTypeId != typeId) {
         wrapper.set(Types.VAR_INT, 1, mappedTypeId);
      }

      EntityType entityType = this.typeFromId(this.trackMappedType ? mappedTypeId : typeId);
      return entityType == null ? null : this.<EntityTracker>tracker(wrapper.user()).addEntity(entityId, entityType);
   }

   public PacketHandler trackerAndRewriterHandler(@Nullable Type<List<EntityData>> dataType) {
      return wrapper -> {
         int entityId = wrapper.get(Types.VAR_INT, 0);
         int type = wrapper.get(Types.VAR_INT, 1);
         this.trackAndRewrite(wrapper, type, entityId);
         if (dataType != null) {
            this.handleEntityData(entityId, wrapper.get(dataType, 0), wrapper.user());
         }
      };
   }

   public PacketHandler trackerAndRewriterHandler(@Nullable Type<List<EntityData>> dataType, EntityType entityType) {
      return wrapper -> {
         int entityId = wrapper.get(Types.VAR_INT, 0);
         this.<EntityTracker>tracker(wrapper.user()).addEntity(entityId, entityType);
         if (dataType != null) {
            this.handleEntityData(entityId, wrapper.get(dataType, 0), wrapper.user());
         }
      };
   }

   public PacketHandler objectTrackerHandler() {
      return wrapper -> {
         int entityId = wrapper.get(Types.VAR_INT, 0);
         byte type = wrapper.get(Types.BYTE, 0);
         int data = wrapper.get(Types.INT, 0);
         EntityType entType = this.objectTypeFromId(type, data);
         if (entType != null) {
            this.<EntityTracker>tracker(wrapper.user()).addEntity(entityId, entType);
         }
      };
   }

   protected EntityType fallingBlockType() {
      if (this.fallingBlockType == null) {
         this.fallingBlockType = this.typeFromId("falling_block");
      }

      return this.fallingBlockType;
   }

   private void logException(Exception e, @Nullable EntityType type, List<EntityData> entityDataList, EntityData entityData) {
      if (Via.getConfig().logEntityDataErrors()) {
         this.protocol
            .getLogger()
            .severe(
               "An error occurred in entity data handler "
                  + this.getClass().getSimpleName()
                  + " for "
                  + (type != null ? type.name() : "untracked")
                  + " entity type: "
                  + entityData
            );
         this.protocol
            .getLogger()
            .severe(
               entityDataList.stream()
                  .sorted(Comparator.comparingInt(EntityData::id))
                  .map(EntityData::toString)
                  .collect(Collectors.joining("\n", "Full entity data: ", ""))
            );
         this.protocol.getLogger().log(Level.SEVERE, "Error: ", e);
      }
   }
}
