package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public class WrapperPlayServerDisguisedChat extends PacketWrapper<WrapperPlayServerDisguisedChat> {
   private Component message;
   private ChatType.Bound chatFormatting;

   public WrapperPlayServerDisguisedChat(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerDisguisedChat(Component message, ChatType.Bound chatType) {
      super(PacketType.Play.Server.DISGUISED_CHAT);
      this.message = message;
      this.chatFormatting = chatType;
   }

   @Override
   public void read() {
      this.message = this.readComponent();
      this.chatFormatting = this.readChatTypeBoundNetwork();
   }

   @Override
   public void write() {
      this.writeComponent(this.message);
      this.writeChatTypeBoundNetwork(this.chatFormatting);
   }

   public void copy(WrapperPlayServerDisguisedChat wrapper) {
      this.message = wrapper.message;
      this.chatFormatting = wrapper.chatFormatting;
   }

   public Component getMessage() {
      return this.message;
   }

   public void setMessage(Component message) {
      this.message = message;
   }

   public ChatType.Bound getChatFormatting() {
      return this.chatFormatting;
   }

   public void setChatFormatting(ChatType.Bound chatFormatting) {
      this.chatFormatting = chatFormatting;
   }

   @Deprecated
   public ChatType.Bound getChatType() {
      return this.chatFormatting;
   }

   @Deprecated
   public void setChatType(ChatType.Bound chatFormatting) {
      this.chatFormatting = chatFormatting;
   }
}
