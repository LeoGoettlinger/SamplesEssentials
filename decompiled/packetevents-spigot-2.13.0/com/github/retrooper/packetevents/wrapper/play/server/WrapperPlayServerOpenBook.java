package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerOpenBook extends PacketWrapper<WrapperPlayServerOpenBook> {
   private InteractionHand hand;

   public WrapperPlayServerOpenBook(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerOpenBook(InteractionHand hand) {
      super(PacketType.Play.Server.OPEN_BOOK);
      this.hand = hand;
   }

   @Override
   public void read() {
      this.hand = InteractionHand.values()[this.readVarInt()];
   }

   @Override
   public void write() {
      this.writeVarInt(this.hand.ordinal());
   }

   public void copy(WrapperPlayServerOpenBook wrapper) {
      this.hand = wrapper.hand;
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public void setHand(InteractionHand hand) {
      this.hand = hand;
   }
}
