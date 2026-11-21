package com.viaversion.viaversion.protocols.v26_2to26_3.packet;

import com.viaversion.viaversion.api.protocol.packet.State;

public enum ClientboundConfigurationPackets26_3 implements ClientboundPacket26_3 {
   COOKIE_REQUEST,
   CUSTOM_PAYLOAD,
   DISCONNECT,
   FINISH_CONFIGURATION,
   KEEP_ALIVE,
   PING,
   RESET_CHAT,
   REGISTRY_DATA,
   RESOURCE_PACK_POP,
   RESOURCE_PACK_PUSH,
   POST_EFFECTS,
   STORE_COOKIE,
   TRANSFER,
   UPDATE_ENABLED_FEATURES,
   UPDATE_TAGS,
   SELECT_KNOWN_PACKS,
   CUSTOM_REPORT_DETAILS,
   SERVER_LINKS,
   CLEAR_DIALOG,
   SHOW_DIALOG,
   CODE_OF_CONDUCT;

   @Override
   public int getId() {
      return this.ordinal();
   }

   @Override
   public String getName() {
      return this.name();
   }

   @Override
   public State state() {
      return State.CONFIGURATION;
   }
}
