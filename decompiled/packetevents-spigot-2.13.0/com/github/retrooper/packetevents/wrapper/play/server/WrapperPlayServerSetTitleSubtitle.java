package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public class WrapperPlayServerSetTitleSubtitle extends PacketWrapper<WrapperPlayServerSetTitleSubtitle> {
   @Deprecated
   public static boolean HANDLE_JSON = true;
   private Component subtitle;

   public WrapperPlayServerSetTitleSubtitle(PacketSendEvent event) {
      super(event);
   }

   @Deprecated
   public WrapperPlayServerSetTitleSubtitle(String subtitleJson) {
      super(PacketType.Play.Server.SET_TITLE_SUBTITLE);
      this.subtitle = this.getSerializers().fromJson(subtitleJson);
   }

   public WrapperPlayServerSetTitleSubtitle(Component subtitle) {
      super(PacketType.Play.Server.SET_TITLE_SUBTITLE);
      this.subtitle = subtitle;
   }

   @Override
   public void read() {
      this.subtitle = this.readComponent();
   }

   @Override
   public void write() {
      this.writeComponent(this.subtitle);
   }

   public void copy(WrapperPlayServerSetTitleSubtitle wrapper) {
      this.subtitle = wrapper.subtitle;
   }

   public Component getSubtitle() {
      return this.subtitle;
   }

   public void setSubtitle(Component subtitle) {
      this.subtitle = subtitle;
   }

   @Deprecated
   public String getSubtitleJson() {
      return this.getSerializers().asJson(this.getSubtitle());
   }

   @Deprecated
   public void setSubtitleJson(String subtitleJson) {
      this.setSubtitle(this.getSerializers().fromJson(subtitleJson));
   }
}
