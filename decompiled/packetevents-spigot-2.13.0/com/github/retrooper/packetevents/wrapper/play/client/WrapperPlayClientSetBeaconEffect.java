package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientSetBeaconEffect extends PacketWrapper<WrapperPlayClientSetBeaconEffect> {
   private int primaryEffect;
   private int secondaryEffect;

   public WrapperPlayClientSetBeaconEffect(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientSetBeaconEffect(int primaryEffect, int secondaryEffect) {
      super(PacketType.Play.Client.SET_BEACON_EFFECT);
      this.primaryEffect = primaryEffect;
      this.secondaryEffect = secondaryEffect;
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         this.primaryEffect = this.readOptionalEffect();
         this.secondaryEffect = this.readOptionalEffect();
      } else {
         this.primaryEffect = this.readVarInt();
         this.secondaryEffect = this.readVarInt();
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         this.writeOptionalEffect(this.primaryEffect);
         this.writeOptionalEffect(this.secondaryEffect);
      } else {
         this.writeVarInt(this.primaryEffect);
         this.writeVarInt(this.secondaryEffect);
      }
   }

   public void copy(WrapperPlayClientSetBeaconEffect wrapper) {
      this.primaryEffect = wrapper.primaryEffect;
      this.secondaryEffect = wrapper.secondaryEffect;
   }

   public int getPrimaryEffect() {
      return this.primaryEffect;
   }

   public void setPrimaryEffect(int primaryEffect) {
      this.primaryEffect = primaryEffect;
   }

   public int getSecondaryEffect() {
      return this.secondaryEffect;
   }

   public void setSecondaryEffect(int secondaryEffect) {
      this.secondaryEffect = secondaryEffect;
   }

   private int readOptionalEffect() {
      return this.readBoolean() ? this.readVarInt() : -1;
   }

   private void writeOptionalEffect(int effect) {
      this.writeBoolean(effect != -1);
      if (effect != -1) {
         this.writeVarInt(effect);
      }
   }
}
