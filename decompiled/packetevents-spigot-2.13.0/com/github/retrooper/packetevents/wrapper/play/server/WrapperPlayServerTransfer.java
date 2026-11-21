package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerTransfer extends PacketWrapper<WrapperPlayServerTransfer> {
   private String host;
   private int port;

   public WrapperPlayServerTransfer(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerTransfer(String host, int port) {
      super(PacketType.Play.Server.TRANSFER);
      this.host = host;
      this.port = port;
   }

   @Override
   public void read() {
      this.host = this.readString();
      this.port = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeString(this.host);
      this.writeVarInt(this.port);
   }

   public void copy(WrapperPlayServerTransfer wrapper) {
      this.host = wrapper.host;
      this.port = wrapper.port;
   }

   public String getHost() {
      return this.host;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public int getPort() {
      return this.port;
   }

   public void setPort(int port) {
      this.port = port;
   }
}
