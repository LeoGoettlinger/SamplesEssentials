package com.viaversion.viaversion.api.data.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.KeyMappings;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EntityTracker {
   @Deprecated
   UserConnection user();

   TrackedEntity addEntity(int var1, EntityType var2);

   boolean hasEntity(int var1);

   @Nullable
   TrackedEntity entity(int var1);

   @Nullable
   EntityType entityType(int var1);

   @Nullable
   TrackedEntity removeEntity(int var1);

   void clearEntities();

   void clear();

   boolean hasClientEntityId();

   int clientEntityId() throws IllegalStateException;

   void setClientEntityId(int var1);

   boolean canInstaBuild();

   void setInstaBuild(boolean var1);

   int currentWorldSectionHeight();

   void setCurrentWorldSectionHeight(int var1);

   int currentMinY();

   void setCurrentMinY(int var1);

   @Nullable
   String currentWorld();

   void setCurrentWorld(String var1);

   int currentDimensionId();

   void setCurrentDimensionId(int var1);

   int biomesSent();

   void setBiomesSent(int var1);

   @Nullable
   Type<Chunk> chunkType(boolean var1);

   void setChunkType(boolean var1, @Nullable Type<Chunk> var2);

   EntityType playerType();

   @Nullable
   DimensionData dimensionData(String var1);

   @Nullable
   DimensionData dimensionData(int var1);

   void setDimensions(Map<String, DimensionData> var1);

   @Nullable
   KeyMappings registryKeys(String var1);

   void addRegistryKeys(String var1, KeyMappings var2);
}
