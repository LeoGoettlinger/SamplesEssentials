package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientAnimation extends PacketWrapper<WrapperPlayClientAnimation> {
   private InteractionHand interactionHand;

   public WrapperPlayClientAnimation(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientAnimation(InteractionHand interactionHand) {
      super(PacketType.Play.Client.ANIMATION);
      this.interactionHand = interactionHand;
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.interactionHand = InteractionHand.getById(this.readVarInt());
      } else {
         this.interactionHand = InteractionHand.MAIN_HAND;
      }
   }

   public void copy(WrapperPlayClientAnimation wrapper) {
      this.interactionHand = wrapper.interactionHand;
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.writeVarInt(this.interactionHand.getId());
      }
   }

   public InteractionHand getHand() {
      return this.interactionHand;
   }

   public void setHand(InteractionHand interactionHand) {
      this.interactionHand = interactionHand;
   }
}
