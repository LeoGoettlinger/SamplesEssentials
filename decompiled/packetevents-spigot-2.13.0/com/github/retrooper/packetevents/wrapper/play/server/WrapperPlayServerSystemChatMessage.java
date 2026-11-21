package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerSystemChatMessage extends PacketWrapper<WrapperPlayServerSystemChatMessage> {
   @Deprecated
   public static boolean HANDLE_JSON = true;
   @Nullable
   private ChatType type;
   private boolean overlay;
   private Component message;

   public WrapperPlayServerSystemChatMessage(PacketSendEvent event) {
      super(event);
   }

   @Deprecated
   public WrapperPlayServerSystemChatMessage(@NotNull ChatType type, Component message) {
      super(PacketType.Play.Server.SYSTEM_CHAT_MESSAGE);
      this.type = type;
      if (type == ChatTypes.GAME_INFO) {
         this.overlay = true;
      }

      this.message = message;
   }

   @Deprecated
   public WrapperPlayServerSystemChatMessage(@NotNull ChatType type, String messageJson) {
      super(PacketType.Play.Server.SYSTEM_CHAT_MESSAGE);
      this.message = this.getSerializers().fromJson(messageJson);
      this.type = type;
      if (type == ChatTypes.GAME_INFO) {
         this.overlay = true;
      }
   }

   public WrapperPlayServerSystemChatMessage(boolean overlay, Component message) {
      super(PacketType.Play.Server.SYSTEM_CHAT_MESSAGE);
      this.message = message;
      this.overlay = overlay;
      this.type = overlay ? ChatTypes.GAME_INFO : ChatTypes.SYSTEM;
   }

   @Deprecated
   public WrapperPlayServerSystemChatMessage(boolean overlay, String messageJson) {
      super(PacketType.Play.Server.SYSTEM_CHAT_MESSAGE);
      this.message = this.getSerializers().fromJson(messageJson);
      this.overlay = overlay;
      this.type = overlay ? ChatTypes.GAME_INFO : ChatTypes.SYSTEM;
   }

   @Override
   public void read() {
      this.message = this.readComponent();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_1)) {
         this.overlay = this.readBoolean();
      } else {
         this.type = this.readMappedEntity(ChatTypes.getRegistry());
      }
   }

   @Override
   public void write() {
      this.writeComponent(this.message);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_1)) {
         this.writeBoolean(this.overlay);
      } else if (this.type == null) {
         if (this.overlay) {
            this.writeMappedEntity(ChatTypes.GAME_INFO);
         } else {
            this.writeMappedEntity(ChatTypes.SYSTEM);
         }
      } else {
         this.writeMappedEntity(this.type);
      }
   }

   public void copy(WrapperPlayServerSystemChatMessage wrapper) {
      this.type = wrapper.type;
      this.overlay = wrapper.overlay;
      this.message = wrapper.message;
   }

   @Deprecated
   @Nullable
   public ChatType getType() {
      return this.type;
   }

   @Deprecated
   public void setType(@Nullable ChatType type) {
      this.type = type;
   }

   @Deprecated
   public String getMessageJson() {
      return this.getSerializers().asJson(this.getMessage());
   }

   @Deprecated
   public void setMessageJson(String messageJson) {
      this.setMessage(this.getSerializers().fromJson(messageJson));
   }

   public Component getMessage() {
      return this.message;
   }

   public void setMessage(Component message) {
      this.message = message;
   }

   public boolean isOverlay() {
      return this.overlay;
   }

   public void setOverlay(boolean overlay) {
      this.overlay = overlay;
   }
}
