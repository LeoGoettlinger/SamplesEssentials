package com.github.retrooper.packetevents.protocol.chat.message.reader.impl;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.chat.message.reader.ChatMessageProcessor;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatMessageProcessor_v1_16 implements ChatMessageProcessor {
   @Override
   public ChatMessage readChatMessage(@NotNull PacketWrapper<?> wrapper) {
      Component chatContent = wrapper.readComponent();
      ChatType type = wrapper.readMappedEntity(ChatTypes.getRegistry());
      UUID senderUUID = wrapper.readUUID();
      return new ChatMessage_v1_16(chatContent, type, senderUUID);
   }

   @Override
   public void writeChatMessage(@NotNull PacketWrapper<?> wrapper, @NotNull ChatMessage data) {
      wrapper.writeComponent(data.getChatContent());
      wrapper.writeMappedEntity(data.getType());
      wrapper.writeUUID(((ChatMessage_v1_16)data).getSenderUUID());
   }
}
