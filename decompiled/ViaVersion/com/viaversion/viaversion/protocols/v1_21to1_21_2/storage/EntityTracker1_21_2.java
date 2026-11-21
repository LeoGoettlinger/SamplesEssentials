package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityTracker1_21_2 extends EntityTrackerBase {
   private double playerMaxHealthAttributeValue = 20.0;

   public EntityTracker1_21_2(UserConnection connection) {
      super(connection, EntityTypes1_21_2.PLAYER);
   }

   public EntityTracker1_21_2.BoatEntity trackBoatEntity(int entityId, UUID uuid, int data) {
      EntityTracker1_21_2.BoatEntity entity = new EntityTracker1_21_2.BoatEntity(uuid, data);
      this.entity(entityId).put(entity);
      return entity;
   }

   @Nullable
   public EntityTracker1_21_2.BoatEntity trackedBoatEntity(int entityId) {
      TrackedEntity entity = this.entity(entityId);
      return entity != null ? entity.get(EntityTracker1_21_2.BoatEntity.class) : null;
   }

   public void updateBoatType(int entityId, EntityType type) {
      EntityTracker1_21_2.BoatEntity boatEntity = this.trackedBoatEntity(entityId);
      this.removeEntity(entityId);
      TrackedEntity newEntity = this.addEntity(entityId, type);
      newEntity.put(boatEntity);
   }

   public double playerMaxHealthAttributeValue() {
      return this.playerMaxHealthAttributeValue;
   }

   public void setPlayerMaxHealthAttributeValue(double playerMaxHealthAttributeValue) {
      this.playerMaxHealthAttributeValue = playerMaxHealthAttributeValue;
   }

   public static final class BoatEntity implements StorableObject {
      private final List<EntityData> entityData = new ArrayList<>();
      private final UUID uuid;
      private final int data;
      private double x;
      private double y;
      private double z;
      private float yaw;
      private float pitch;
      private int[] passengers;

      public BoatEntity(UUID uuid, int data) {
         this.uuid = uuid;
         this.data = data;
      }

      public void setPosition(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public void setRotation(float yaw, float pitch) {
         this.yaw = yaw;
         this.pitch = pitch;
      }

      public void setPassengers(int[] passengers) {
         this.passengers = passengers;
      }

      public UUID uuid() {
         return this.uuid;
      }

      public int data() {
         return this.data;
      }

      public double x() {
         return this.x;
      }

      public double y() {
         return this.y;
      }

      public double z() {
         return this.z;
      }

      public float yaw() {
         return this.yaw;
      }

      public float pitch() {
         return this.pitch;
      }

      public List<EntityData> entityData() {
         return this.entityData;
      }

      @Nullable
      public int[] passengers() {
         return this.passengers;
      }
   }
}
