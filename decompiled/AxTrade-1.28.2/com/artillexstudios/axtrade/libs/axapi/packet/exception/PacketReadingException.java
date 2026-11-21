package com.artillexstudios.axtrade.libs.axapi.packet.exception;

public class PacketReadingException extends RuntimeException {
   public static final PacketReadingException INSTANCE = new PacketReadingException();

   private PacketReadingException() {
   }
}
