package com.github.retrooper.packetevents.protocol.chat.message;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import net.kyori.adventure.text.Component;

public class ChatMessageLegacy extends ChatMessage {
   public ChatMessageLegacy(Component chatContent, ChatType type) {
      super(chatContent, type);
   }
}
