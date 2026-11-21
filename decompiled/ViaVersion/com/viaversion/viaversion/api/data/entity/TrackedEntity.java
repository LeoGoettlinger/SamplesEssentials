package com.viaversion.viaversion.api.data.entity;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TrackedEntity {
   EntityType entityType();

   boolean has(Class<? extends StorableObject> var1);

   @Nullable
   <T extends StorableObject> T get(Class<T> var1);

   @Nullable
   <T extends StorableObject> T remove(Class<T> var1);

   void put(StorableObject var1);

   boolean hasSentEntityData();

   void sentEntityData(boolean var1);
}
