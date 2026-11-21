package com.github.retrooper.packetevents.protocol.chat.message.reader.impl;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.LastSeenMessages;
import com.github.retrooper.packetevents.protocol.chat.filter.FilterMask;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.reader.ChatMessageProcessor;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatMessageProcessor_v1_19_1 implements ChatMessageProcessor {
   @Override
   public ChatMessage readChatMessage(@NotNull PacketWrapper<?> wrapper) {
      byte[] previousSignature = wrapper.readOptional(PacketWrapper::readByteArray);
      UUID senderUUID = wrapper.readUUID();
      byte[] signature = wrapper.readByteArray();
      String plainContent = wrapper.readString(256);
      Component chatContent = wrapper.readOptional(PacketWrapper::readComponent);
      if (chatContent == null && plainContent.isEmpty()) {
         chatContent = Component.empty();
      } else if (chatContent == null) {
         chatContent = Component.text(plainContent);
      }

      Instant timestamp = wrapper.readTimestamp();
      long salt = wrapper.readLong();
      LastSeenMessages lastSeenMessages = wrapper.readLastSeenMessages();
      Component unsignedChatContent = wrapper.readOptional(PacketWrapper::readComponent);
      FilterMask filterMask = wrapper.readFilterMask();
      ChatType.Bound chatFormatting = wrapper.readChatTypeBoundNetwork();
      return new ChatMessage_v1_19_1(
         plainContent,
         chatContent,
         unsignedChatContent,
         senderUUID,
         chatFormatting,
         previousSignature,
         signature,
         timestamp,
         salt,
         lastSeenMessages,
         filterMask
      );
   }

   @Override
   public void writeChatMessage(@NotNull PacketWrapper<?> wrapper, @NotNull ChatMessage data) {
      ChatMessage_v1_19_1 newData = (ChatMessage_v1_19_1)data;
      wrapper.writeOptional(newData.getPreviousSignature(), PacketWrapper::writeByteArray);
      wrapper.writeUUID(newData.getSenderUUID());
      wrapper.writeByteArray(newData.getSignature());
      wrapper.writeString(newData.getPlainContent(), 256);
      wrapper.writeOptional(newData.getChatContent(), PacketWrapper::writeComponent);
      wrapper.writeTimestamp(newData.getTimestamp());
      wrapper.writeLong(newData.getSalt());
      wrapper.writeLastSeenMessages(newData.getLastSeenMessages());
      wrapper.writeOptional(newData.getUnsignedChatContent(), PacketWrapper::writeComponent);
      wrapper.writeFilterMask(newData.getFilterMask());
      wrapper.writeChatTypeBoundNetwork(newData.getChatFormatting());
   }
}
