package com.viaversion.viaversion.data.entity;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.libs.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TrackedEntityImpl implements TrackedEntity {
   private final EntityType entityType;
   private Map<Class<?>, StorableObject> storedObjects;
   private boolean sentEntityData;

   public TrackedEntityImpl(EntityType entityType) {
      this.entityType = entityType;
   }

   @Override
   public EntityType entityType() {
      return this.entityType;
   }

   @Override
   public boolean has(Class<? extends StorableObject> objectClass) {
      return this.storedObjects != null && this.storedObjects.containsKey(objectClass);
   }

   @Nullable
   @Override
   public <T extends StorableObject> T get(Class<T> objectClass) {
      return (T)(this.storedObjects != null ? this.storedObjects.get(objectClass) : null);
   }

   @Override
   public void put(StorableObject object) {
      this.storedObjects().put(object.getClass(), object);
   }

   @Nullable
   @Override
   public <T extends StorableObject> T remove(Class<T> objectClass) {
      return (T)(this.storedObjects != null ? this.storedObjects.remove(objectClass) : null);
   }

   private Map<Class<?>, StorableObject> storedObjects() {
      if (this.storedObjects == null) {
         this.storedObjects = new Reference2ObjectArrayMap<>();
      }

      return this.storedObjects;
   }

   @Override
   public boolean hasSentEntityData() {
      return this.sentEntityData;
   }

   @Override
   public void sentEntityData(boolean sentEntityData) {
      this.sentEntityData = sentEntityData;
   }

   @Override
   public String toString() {
      return "TrackedEntityImpl{entityType=" + this.entityType + ", storedObjects=" + this.storedObjects + ", sentEntityData=" + this.sentEntityData + "}";
   }
}
