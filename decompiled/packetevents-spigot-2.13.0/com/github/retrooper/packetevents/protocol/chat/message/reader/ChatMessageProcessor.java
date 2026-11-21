package com.github.retrooper.packetevents.protocol.chat.message.reader;

import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.NotNull;

public interface ChatMessageProcessor {
   ChatMessage readChatMessage(@NotNull PacketWrapper<?> wrapper);

   void writeChatMessage(@NotNull PacketWrapper<?> wrapper, @NotNull ChatMessage data);
}
