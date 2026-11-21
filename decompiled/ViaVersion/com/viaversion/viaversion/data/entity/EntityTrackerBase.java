package com.viaversion.viaversion.data.entity;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EntityTrackerBase implements EntityTracker, ClientEntityIdChangeListener {
   protected final Int2ObjectMap<TrackedEntity> entities = new Int2ObjectOpenHashMap<>();
   private final Map<String, KeyMappings> registryKeyMappings = new HashMap<>();
   private final UserConnection connection;
   private final EntityType playerType;
   private Integer clientEntityId;
   private int currentWorldSectionHeight = -1;
   private int currentMinY;
   private String currentWorld;
   private int currentDimensionId = -1;
   private int biomesSent = -1;
   private Map<String, DimensionData> dimensions = Collections.emptyMap();
   private boolean instaBuild;
   private Type<Chunk> chunkType;
   private Type<Chunk> mappedChunkType;

   public EntityTrackerBase(UserConnection connection, @Nullable EntityType playerType) {
      this.connection = connection;
      this.playerType = playerType;
   }

   @Override
   public UserConnection user() {
      return this.connection;
   }

   @Override
   public TrackedEntity addEntity(int id, EntityType type) {
      TrackedEntityImpl entity = new TrackedEntityImpl(type);
      this.entities.put(id, entity);
      return entity;
   }

   @Override
   public boolean hasEntity(int id) {
      return this.entities.containsKey(id);
   }

   @Nullable
   @Override
   public TrackedEntity entity(int entityId) {
      return this.entities.get(entityId);
   }

   @Nullable
   @Override
   public EntityType entityType(int id) {
      TrackedEntity entity = this.entities.get(id);
      return entity != null ? entity.entityType() : null;
   }

   @Override
   public TrackedEntity removeEntity(int id) {
      return this.entities.remove(id);
   }

   @Override
   public void clearEntities() {
      for (int id : this.entities.keySet().toIntArray()) {
         this.removeEntity(id);
      }

      if (this.clientEntityId != null) {
         this.entities.put(this.clientEntityId.intValue(), new TrackedEntityImpl(this.playerType));
      }
   }

   @Override
   public void clear() {
      for (int id : this.entities.keySet().toIntArray()) {
         this.removeEntity(id);
      }

      this.clientEntityId = null;
      this.registryKeyMappings.clear();
   }

   @Override
   public boolean hasClientEntityId() {
      return this.clientEntityId != null;
   }

   @Override
   public int clientEntityId() throws IllegalStateException {
      if (this.clientEntityId == null) {
         throw new IllegalStateException("Client entity id not set");
      } else {
         return this.clientEntityId;
      }
   }

   @Override
   public void setClientEntityId(int clientEntityId) {
      Preconditions.checkNotNull(this.playerType);
      TrackedEntity oldEntity;
      if (this.clientEntityId != null && (oldEntity = this.entities.remove(this.clientEntityId.intValue())) != null) {
         this.entities.put(clientEntityId, oldEntity);
      } else {
         this.entities.put(clientEntityId, new TrackedEntityImpl(this.playerType));
      }

      this.clientEntityId = clientEntityId;
   }

   @Override
   public boolean canInstaBuild() {
      return this.instaBuild;
   }

   @Override
   public void setInstaBuild(boolean instaBuild) {
      this.instaBuild = instaBuild;
   }

   @Override
   public int currentWorldSectionHeight() {
      return this.currentWorldSectionHeight;
   }

   @Override
   public void setCurrentWorldSectionHeight(int currentWorldSectionHeight) {
      if (this.currentWorldSectionHeight != currentWorldSectionHeight) {
         this.chunkType = null;
         this.mappedChunkType = null;
      }

      this.currentWorldSectionHeight = currentWorldSectionHeight;
   }

   @Override
   public int currentMinY() {
      return this.currentMinY;
   }

   @Override
   public void setCurrentMinY(int currentMinY) {
      this.currentMinY = currentMinY;
   }

   @Nullable
   @Override
   public String currentWorld() {
      return this.currentWorld;
   }

   @Override
   public void setCurrentWorld(String currentWorld) {
      this.currentWorld = currentWorld;
   }

   @Override
   public int currentDimensionId() {
      return this.currentDimensionId;
   }

   @Override
   public void setCurrentDimensionId(int currentDimensionId) {
      this.currentDimensionId = currentDimensionId;
   }

   @Override
   public int biomesSent() {
      return this.biomesSent;
   }

   @Override
   public void setBiomesSent(int biomesSent) {
      if (this.biomesSent != biomesSent) {
         this.chunkType = null;
         this.mappedChunkType = null;
      }

      this.biomesSent = biomesSent;
   }

   @Nullable
   @Override
   public Type<Chunk> chunkType(boolean mapped) {
      return mapped ? this.mappedChunkType : this.chunkType;
   }

   @Override
   public void setChunkType(boolean mapped, @Nullable Type<Chunk> chunkType) {
      if (mapped) {
         this.mappedChunkType = chunkType;
      } else {
         this.chunkType = chunkType;
      }
   }

   @Override
   public EntityType playerType() {
      return this.playerType;
   }

   @Nullable
   @Override
   public DimensionData dimensionData(String dimension) {
      return this.dimensions.get(Key.stripMinecraftNamespace(dimension));
   }

   @Nullable
   @Override
   public DimensionData dimensionData(int dimensionId) {
      return this.dimensions.values().stream().filter(data -> data.id() == dimensionId).findFirst().orElse(null);
   }

   @Override
   public void setDimensions(Map<String, DimensionData> dimensions) {
      this.dimensions = dimensions;
   }

   @Nullable
   @Override
   public KeyMappings registryKeys(String registryKey) {
      return this.registryKeyMappings.get(Key.stripMinecraftNamespace(registryKey));
   }

   @Override
   public void addRegistryKeys(String registryKey, KeyMappings mappings) {
      this.registryKeyMappings.put(Key.stripMinecraftNamespace(registryKey), mappings);
   }
}
