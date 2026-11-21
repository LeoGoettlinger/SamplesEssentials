package com.github.retrooper.packetevents.event;

public abstract class PacketListenerAbstract extends PacketListenerCommon {
   public PacketListenerAbstract(PacketListenerPriority priority) {
      super(priority);
   }

   public PacketListenerAbstract() {
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
   }
}
