package com.artillexstudios.axafkzone.libs.axapi.packet.data;

public enum SignTextSlot {
   BACK,
   FRONT;

   public static SignTextSlot get(boolean front) {
      return front ? FRONT : BACK;
   }
}
