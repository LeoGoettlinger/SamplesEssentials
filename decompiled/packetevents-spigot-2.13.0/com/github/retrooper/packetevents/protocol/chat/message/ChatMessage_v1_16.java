package com.github.retrooper.packetevents.protocol.chat.message;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class ChatMessage_v1_16 extends ChatMessage {
   private UUID senderUUID;

   public ChatMessage_v1_16(Component chatContent, ChatType type, UUID senderUUID) {
      super(chatContent, type);
      this.senderUUID = senderUUID;
   }

   public UUID getSenderUUID() {
      return this.senderUUID;
   }

   public void setSenderUUID(UUID senderUUID) {
      this.senderUUID = senderUUID;
   }
}
