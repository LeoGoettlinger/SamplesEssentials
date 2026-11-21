package com.github.retrooper.packetevents.protocol;

public enum PacketSide {
   CLIENT,
   SERVER;

   public PacketSide getOpposite() {
      return this == CLIENT ? SERVER : CLIENT;
   }
}
