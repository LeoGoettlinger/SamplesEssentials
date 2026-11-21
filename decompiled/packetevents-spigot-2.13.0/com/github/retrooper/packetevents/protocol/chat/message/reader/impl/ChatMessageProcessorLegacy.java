package com.github.retrooper.packetevents.protocol.chat.message.reader.impl;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.reader.ChatMessageProcessor;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatMessageProcessorLegacy implements ChatMessageProcessor {
   @Override
   public ChatMessage readChatMessage(@NotNull PacketWrapper<?> wrapper) {
      Component chatContent = wrapper.readComponent();
      ChatType type = wrapper.readMappedEntity(ChatTypes.getRegistry());
      return new ChatMessageLegacy(chatContent, type);
   }

   @Override
   public void writeChatMessage(@NotNull PacketWrapper<?> wrapper, @NotNull ChatMessage data) {
      wrapper.writeComponent(data.getChatContent());
      wrapper.writeMappedEntity(data.getType());
   }
}
