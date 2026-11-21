package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerPlayerRotation extends PacketWrapper<WrapperPlayServerPlayerRotation> {
   private float yaw;
   private boolean relativeYaw;
   private float pitch;
   private boolean relativePitch;

   public WrapperPlayServerPlayerRotation(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerPlayerRotation(float yaw, float pitch) {
      this(yaw, false, pitch, false);
   }

   public WrapperPlayServerPlayerRotation(float yaw, boolean relativeYaw, float pitch, boolean relativePitch) {
      super(PacketType.Play.Server.PLAYER_ROTATION);
      this.yaw = yaw;
      this.relativeYaw = relativeYaw;
      this.pitch = pitch;
      this.relativePitch = relativePitch;
   }

   @Override
   public void read() {
      this.yaw = this.readFloat();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9)) {
         this.relativeYaw = this.readBoolean();
      }

      this.pitch = this.readFloat();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9)) {
         this.relativePitch = this.readBoolean();
      }
   }

   @Override
   public void write() {
      this.writeFloat(this.yaw);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9)) {
         this.writeBoolean(this.relativeYaw);
      }

      this.writeFloat(this.pitch);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9)) {
         this.writeBoolean(this.relativePitch);
      }
   }

   public void copy(WrapperPlayServerPlayerRotation wrapper) {
      this.yaw = wrapper.yaw;
      this.relativeYaw = wrapper.relativeYaw;
      this.pitch = wrapper.pitch;
      this.relativePitch = wrapper.relativePitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public boolean isRelativeYaw() {
      return this.relativeYaw;
   }

   public void setRelativeYaw(boolean relativeYaw) {
      this.relativeYaw = relativeYaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public boolean isRelativePitch() {
      return this.relativePitch;
   }

   public void setRelativePitch(boolean relativePitch) {
      this.relativePitch = relativePitch;
   }
}
