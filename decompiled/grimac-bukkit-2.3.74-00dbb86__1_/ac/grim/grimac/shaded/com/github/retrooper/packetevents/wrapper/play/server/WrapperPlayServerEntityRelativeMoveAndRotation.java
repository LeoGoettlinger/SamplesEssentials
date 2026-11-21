package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerEntityRelativeMoveAndRotation extends PacketWrapper<WrapperPlayServerEntityRelativeMoveAndRotation> {
   private static final float ROTATION_FACTOR = 0.7111111F;
   private static final double MODERN_DELTA_DIVISOR = 4096.0;
   private static final double LEGACY_DELTA_DIVISOR = 32.0;
   private int entityID;
   private double deltaX;
   private double deltaY;
   private double deltaZ;
   private float yaw;
   private float pitch;
   private boolean onGround;

   public WrapperPlayServerEntityRelativeMoveAndRotation(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerEntityRelativeMoveAndRotation(int entityID, double deltaX, double deltaY, double deltaZ, float yaw, float pitch, boolean onGround) {
      super(PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION);
      this.entityID = entityID;
      this.deltaX = deltaX;
      this.deltaY = deltaY;
      this.deltaZ = deltaZ;
      this.yaw = yaw;
      this.pitch = pitch;
      this.onGround = onGround;
   }

   @Override
   public void read() {
      this.entityID = this.readVarInt();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.deltaX = this.readShort() / 4096.0;
         this.deltaY = this.readShort() / 4096.0;
         this.deltaZ = this.readShort() / 4096.0;
      } else {
         this.deltaX = this.readByte() / 32.0;
         this.deltaY = this.readByte() / 32.0;
         this.deltaZ = this.readByte() / 32.0;
      }

      this.yaw = this.readByte() / 0.7111111F;
      this.pitch = this.readByte() / 0.7111111F;
      this.onGround = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityID);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.writeShort((short)(this.deltaX * 4096.0));
         this.writeShort((short)(this.deltaY * 4096.0));
         this.writeShort((short)(this.deltaZ * 4096.0));
      } else {
         this.writeByte((byte)(this.deltaX * 32.0));
         this.writeByte((byte)(this.deltaY * 32.0));
         this.writeByte((byte)(this.deltaZ * 32.0));
      }

      this.writeByte((int)(this.yaw * 0.7111111F));
      this.writeByte((int)(this.pitch * 0.7111111F));
      this.writeBoolean(this.onGround);
   }

   public void copy(WrapperPlayServerEntityRelativeMoveAndRotation wrapper) {
      this.entityID = wrapper.entityID;
      this.deltaX = wrapper.deltaX;
      this.deltaY = wrapper.deltaY;
      this.deltaZ = wrapper.deltaZ;
      this.yaw = wrapper.yaw;
      this.pitch = wrapper.pitch;
      this.onGround = wrapper.onGround;
   }

   public int getEntityId() {
      return this.entityID;
   }

   public void setEntityId(int entityID) {
      this.entityID = entityID;
   }

   public double getDeltaX() {
      return this.deltaX;
   }

   public void setDeltaX(double deltaX) {
      this.deltaX = deltaX;
   }

   public double getDeltaY() {
      return this.deltaY;
   }

   public void setDeltaY(double deltaY) {
      this.deltaY = deltaY;
   }

   public double getDeltaZ() {
      return this.deltaZ;
   }

   public void setDeltaZ(double deltaZ) {
      this.deltaZ = deltaZ;
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

   public boolean isOnGround() {
      return this.onGround;
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }
}
