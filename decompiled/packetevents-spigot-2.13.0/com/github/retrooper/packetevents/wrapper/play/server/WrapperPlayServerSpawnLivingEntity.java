package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityMetadataProvider;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.MathUtil;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WrapperPlayServerSpawnLivingEntity extends PacketWrapper<WrapperPlayServerSpawnLivingEntity> {
   private static final double POSITION_FACTOR = 32.0;
   private static final float ROTATION_FACTOR = 0.7111111F;
   private static final double VELOCITY_FACTOR = 8000.0;
   private int entityID;
   private UUID entityUUID;
   private EntityType entityType;
   private Vector3d position;
   private float yaw;
   private float pitch;
   private float headPitch;
   private Vector3d velocity;
   private List<EntityData<?>> entityMetadata;

   public WrapperPlayServerSpawnLivingEntity(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerSpawnLivingEntity(
      int entityID,
      UUID entityUUID,
      EntityType entityType,
      Vector3d position,
      float yaw,
      float pitch,
      float headPitch,
      Vector3d velocity,
      List<EntityData<?>> entityMetadata
   ) {
      super(PacketType.Play.Server.SPAWN_LIVING_ENTITY);
      this.entityID = entityID;
      this.entityUUID = entityUUID;
      this.entityType = entityType;
      this.position = position;
      this.yaw = yaw;
      this.pitch = pitch;
      this.headPitch = headPitch;
      this.velocity = velocity;
      this.entityMetadata = entityMetadata;
   }

   public WrapperPlayServerSpawnLivingEntity(
      int entityID,
      UUID entityUUID,
      EntityType entityType,
      Vector3d position,
      float yaw,
      float pitch,
      float headPitch,
      Vector3d velocity,
      EntityMetadataProvider metadata
   ) {
      this(
         entityID,
         entityUUID,
         entityType,
         position,
         yaw,
         pitch,
         headPitch,
         velocity,
         metadata.entityData(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion())
      );
   }

   public WrapperPlayServerSpawnLivingEntity(
      int entityId, UUID entityUUID, EntityType entityType, Location location, float headPitch, Vector3d velocity, List<EntityData<?>> entityMetadata
   ) {
      this(entityId, entityUUID, entityType, location.getPosition(), location.getYaw(), location.getPitch(), headPitch, velocity, entityMetadata);
   }

   public WrapperPlayServerSpawnLivingEntity(
      int entityId, UUID entityUUID, EntityType entityType, Location location, float headPitch, Vector3d velocity, EntityMetadataProvider metadata
   ) {
      this(
         entityId,
         entityUUID,
         entityType,
         location.getPosition(),
         location.getYaw(),
         location.getPitch(),
         headPitch,
         velocity,
         metadata.entityData(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion())
      );
   }

   @Override
   public void read() {
      this.entityID = this.readVarInt();
      if (this.serverVersion.isOlderThan(ServerVersion.V_1_9)) {
         this.entityUUID = new UUID(0L, 0L);
         int entityTypeID = this.readByte() & 255;
         this.entityType = EntityTypes.getById(this.serverVersion.toClientVersion(), entityTypeID);
         this.position = new Vector3d(this.readInt() / 32.0, this.readInt() / 32.0, this.readInt() / 32.0);
      } else {
         this.entityUUID = this.readUUID();
         int entityTypeID;
         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_11)) {
            entityTypeID = this.readVarInt();
         } else {
            entityTypeID = this.readUnsignedByte();
         }

         this.entityType = EntityTypes.getById(this.serverVersion.toClientVersion(), entityTypeID);
         this.position = new Vector3d(this.readDouble(), this.readDouble(), this.readDouble());
      }

      this.yaw = this.readByte() / 0.7111111F;
      this.pitch = this.readByte() / 0.7111111F;
      this.headPitch = this.readByte() / 0.7111111F;
      double velX = this.readShort() / 8000.0;
      double velY = this.readShort() / 8000.0;
      double velZ = this.readShort() / 8000.0;
      this.velocity = new Vector3d(velX, velY, velZ);
      if (this.serverVersion.isOlderThan(ServerVersion.V_1_15)) {
         this.entityMetadata = this.readEntityMetadata();
      } else {
         this.entityMetadata = new ArrayList<>();
      }
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityID);
      if (this.serverVersion.isOlderThan(ServerVersion.V_1_9)) {
         this.writeByte(this.entityType.getId(this.serverVersion.toClientVersion()) & 0xFF);
         this.writeInt(MathUtil.floor(this.position.x * 32.0));
         this.writeInt(MathUtil.floor(this.position.y * 32.0));
         this.writeInt(MathUtil.floor(this.position.z * 32.0));
      } else {
         this.writeUUID(this.entityUUID);
         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_11)) {
            this.writeVarInt(this.entityType.getId(this.serverVersion.toClientVersion()));
         } else {
            this.writeByte(this.entityType.getId(this.serverVersion.toClientVersion()) & 0xFF);
         }

         this.writeDouble(this.position.x);
         this.writeDouble(this.position.y);
         this.writeDouble(this.position.z);
      }

      this.writeByte((int)(this.yaw * 0.7111111F));
      this.writeByte((int)(this.pitch * 0.7111111F));
      this.writeByte((int)(this.headPitch * 0.7111111F));
      this.writeShort((int)(this.velocity.x * 8000.0));
      this.writeShort((int)(this.velocity.y * 8000.0));
      this.writeShort((int)(this.velocity.z * 8000.0));
      if (this.serverVersion.isOlderThan(ServerVersion.V_1_15)) {
         this.writeEntityMetadata(this.entityMetadata);
      }
   }

   public void copy(WrapperPlayServerSpawnLivingEntity wrapper) {
      this.entityID = wrapper.entityID;
      this.entityUUID = wrapper.entityUUID;
      this.entityType = wrapper.entityType;
      this.position = wrapper.position;
      this.yaw = wrapper.yaw;
      this.pitch = wrapper.pitch;
      this.headPitch = wrapper.headPitch;
      this.velocity = wrapper.velocity;
      this.entityMetadata = wrapper.entityMetadata;
   }

   public int getEntityId() {
      return this.entityID;
   }

   public void setEntityId(int entityID) {
      this.entityID = entityID;
   }

   public UUID getEntityUUID() {
      return this.entityUUID;
   }

   public void setEntityUUID(UUID entityUUID) {
      this.entityUUID = entityUUID;
   }

   public EntityType getEntityType() {
      return this.entityType;
   }

   public void setEntityType(EntityType entityType) {
      this.entityType = entityType;
   }

   public Vector3d getPosition() {
      return this.position;
   }

   public void setPosition(Vector3d position) {
      this.position = position;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public float getHeadPitch() {
      return this.headPitch;
   }

   public void setHeadPitch(float headPitch) {
      this.headPitch = headPitch;
   }

   public Vector3d getVelocity() {
      return this.velocity;
   }

   public void setVelocity(Vector3d velocity) {
      this.velocity = velocity;
   }

   public List<EntityData<?>> getEntityMetadata() {
      return this.entityMetadata;
   }

   public void setEntityMetadata(List<EntityData<?>> entityMetadata) {
      this.entityMetadata = entityMetadata;
   }

   public void setEntityMetadata(EntityMetadataProvider metadata) {
      this.entityMetadata = metadata.entityData(this.serverVersion.toClientVersion());
   }
}
