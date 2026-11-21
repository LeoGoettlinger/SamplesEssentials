package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.LastSeenMessages;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayClientChatMessage extends PacketWrapper<WrapperPlayClientChatMessage> {
   private static final int LEGACY_MAX_LENGTH = Integer.getInteger("packetevents.legacy-chat-max-length", 100);
   private String message;
   private MessageSignData messageSignData;
   @Nullable
   private LastSeenMessages.Update lastSeenMessages;
   @Nullable
   private LastSeenMessages.LegacyUpdate legacyLastSeenMessages;

   public WrapperPlayClientChatMessage(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientChatMessage(String message, MessageSignData messageSignData, @Nullable LastSeenMessages.LegacyUpdate lastSeenMessages) {
      super(PacketType.Play.Client.CHAT_MESSAGE);
      this.message = message;
      this.messageSignData = messageSignData;
      this.legacyLastSeenMessages = lastSeenMessages;
   }

   public WrapperPlayClientChatMessage(String message, MessageSignData messageSignData, @Nullable LastSeenMessages.Update lastSeenMessages) {
      super(PacketType.Play.Client.CHAT_MESSAGE);
      this.message = message;
      this.messageSignData = messageSignData;
      this.lastSeenMessages = lastSeenMessages;
   }

   @Override
   public void read() {
      int maxMessageLength = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_11) ? 256 : LEGACY_MAX_LENGTH;
      this.message = this.readString(maxMessageLength);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         Instant timestamp = this.readTimestamp();
         this.messageSignData = new MessageSignData(this.readSaltSignature(), timestamp);
         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            this.lastSeenMessages = this.readLastSeenMessagesUpdate();
         } else {
            boolean signedPreview = this.readBoolean();
            this.messageSignData.setSignedPreview(signedPreview);
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_1)) {
               this.legacyLastSeenMessages = this.readLegacyLastSeenMessagesUpdate();
            }
         }
      }
   }

   @Override
   public void write() {
      int maxMessageLength = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_11) ? 256 : LEGACY_MAX_LENGTH;
      this.writeString(this.message, maxMessageLength);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         this.writeTimestamp(this.messageSignData.getTimestamp());
         this.writeSaltSignature(this.messageSignData.getSaltSignature());
         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            if (this.lastSeenMessages != null) {
               this.writeLastSeenMessagesUpdate(this.lastSeenMessages);
            }
         } else {
            this.writeBoolean(this.messageSignData.isSignedPreview());
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_1) && this.legacyLastSeenMessages != null) {
               this.writeLegacyLastSeenMessagesUpdate(this.legacyLastSeenMessages);
            }
         }
      }
   }

   public void copy(WrapperPlayClientChatMessage wrapper) {
      this.message = wrapper.message;
      this.messageSignData = wrapper.messageSignData;
      this.lastSeenMessages = wrapper.lastSeenMessages;
      this.legacyLastSeenMessages = wrapper.legacyLastSeenMessages;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public Optional<MessageSignData> getMessageSignData() {
      return Optional.ofNullable(this.messageSignData);
   }

   public void setMessageSignData(@Nullable MessageSignData messageSignData) {
      this.messageSignData = messageSignData;
   }

   @Nullable
   public LastSeenMessages.Update getLastSeenMessages() {
      return this.lastSeenMessages;
   }

   public void setLastSeenMessages(@Nullable LastSeenMessages.Update lastSeenMessages) {
      this.lastSeenMessages = lastSeenMessages;
   }

   @Nullable
   public LastSeenMessages.LegacyUpdate getLegacyLastSeenMessages() {
      return this.legacyLastSeenMessages;
   }

   public void setLegacyLastSeenMessages(@Nullable LastSeenMessages.LegacyUpdate lastSeenMessages) {
      this.legacyLastSeenMessages = lastSeenMessages;
   }
}
