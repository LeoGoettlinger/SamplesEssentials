package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperConfigServerCodeOfConduct extends PacketWrapper<WrapperConfigServerCodeOfConduct> {
   private String codeOfConduct;

   public WrapperConfigServerCodeOfConduct(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerCodeOfConduct(String codeOfConduct) {
      super(PacketType.Configuration.Server.CODE_OF_CONDUCT);
      this.codeOfConduct = codeOfConduct;
   }

   @Override
   public void read() {
      this.codeOfConduct = this.readString();
   }

   @Override
   public void write() {
      this.writeString(this.codeOfConduct);
   }

   public void copy(WrapperConfigServerCodeOfConduct wrapper) {
      this.codeOfConduct = wrapper.codeOfConduct;
   }

   public String getCodeOfConduct() {
      return this.codeOfConduct;
   }

   public void setCodeOfConduct(String codeOfConduct) {
      this.codeOfConduct = codeOfConduct;
   }
}
