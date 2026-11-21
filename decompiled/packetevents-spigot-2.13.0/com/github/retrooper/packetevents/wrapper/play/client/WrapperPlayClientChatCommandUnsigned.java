package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientChatCommandUnsigned extends PacketWrapper<WrapperPlayClientChatCommandUnsigned> {
   private String command;

   public WrapperPlayClientChatCommandUnsigned(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientChatCommandUnsigned(String command) {
      super(PacketType.Play.Client.CHAT_COMMAND_UNSIGNED);
      this.command = command;
   }

   @Override
   public void read() {
      this.command = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5) ? this.readString() : this.readString(256);
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
         this.writeString(this.command);
      } else {
         this.writeString(this.command, 256);
      }
   }

   public void copy(WrapperPlayClientChatCommandUnsigned wrapper) {
      this.command = wrapper.command;
   }

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String command) {
      this.command = command;
   }
}
