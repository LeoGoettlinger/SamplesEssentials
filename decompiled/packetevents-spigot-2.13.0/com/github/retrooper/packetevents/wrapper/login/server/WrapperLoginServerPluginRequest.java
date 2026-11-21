package com.github.retrooper.packetevents.wrapper.login.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperLoginServerPluginRequest extends PacketWrapper<WrapperLoginServerPluginRequest> {
   private int messageID;
   private String channelName;
   private byte[] data;

   public WrapperLoginServerPluginRequest(PacketSendEvent event) {
      super(event);
   }

   public WrapperLoginServerPluginRequest(int messageID, String channelName, byte[] data) {
      super(PacketType.Login.Server.LOGIN_PLUGIN_REQUEST);
      this.messageID = messageID;
      this.channelName = channelName;
      this.data = data;
   }

   @Override
   public void read() {
      this.messageID = this.readVarInt();
      this.channelName = this.readString();
      int length = ByteBufHelper.readableBytes(this.buffer);
      this.data = this.readBytes(length);
   }

   @Override
   public void write() {
      this.writeVarInt(this.messageID);
      this.writeString(this.channelName);
      this.writeBytes(this.data);
   }

   public void copy(WrapperLoginServerPluginRequest wrapper) {
      this.messageID = wrapper.messageID;
      this.channelName = wrapper.channelName;
      this.data = wrapper.data;
   }

   public int getMessageId() {
      return this.messageID;
   }

   public void setMessageId(int messageID) {
      this.messageID = messageID;
   }

   public String getChannelName() {
      return this.channelName;
   }

   public void setChannelName(String channelName) {
      this.channelName = channelName;
   }

   public byte[] getData() {
      return this.data;
   }

   public void setData(byte[] data) {
      this.data = data;
   }
}
