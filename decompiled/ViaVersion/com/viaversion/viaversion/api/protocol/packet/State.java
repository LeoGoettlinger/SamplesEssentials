package com.viaversion.viaversion.api.protocol.packet;

public enum State {
   HANDSHAKE,
   STATUS,
   LOGIN,
   CONFIGURATION,
   PLAY;

   private static final int STATES_COUNT = values().length;

   public static int statesCount() {
      return STATES_COUNT;
   }
}
