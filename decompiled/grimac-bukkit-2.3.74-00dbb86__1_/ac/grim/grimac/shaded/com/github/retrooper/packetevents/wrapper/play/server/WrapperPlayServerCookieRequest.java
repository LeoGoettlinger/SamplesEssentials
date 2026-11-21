package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerCookieRequest extends PacketWrapper<WrapperPlayServerCookieRequest> {
   private ResourceLocation key;

   public WrapperPlayServerCookieRequest(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerCookieRequest(ResourceLocation key) {
      super(PacketType.Play.Server.COOKIE_REQUEST);
      this.key = key;
   }

   @Override
   public void read() {
      this.key = this.readIdentifier();
   }

   @Override
   public void write() {
      this.writeIdentifier(this.key);
   }

   public void copy(WrapperPlayServerCookieRequest wrapper) {
      this.key = wrapper.key;
   }

   public ResourceLocation getKey() {
      return this.key;
   }

   public void setKey(ResourceLocation key) {
      this.key = key;
   }
}
