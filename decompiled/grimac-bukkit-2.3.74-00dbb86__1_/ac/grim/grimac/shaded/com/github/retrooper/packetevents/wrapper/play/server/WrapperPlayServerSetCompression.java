package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerSetCompression extends PacketWrapper<WrapperPlayServerSetCompression> {
   private int threshold;

   public WrapperPlayServerSetCompression(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerSetCompression(int threshold) {
      super(PacketType.Play.Server.SET_COMPRESSION);
      this.threshold = threshold;
   }

   @Override
   public void read() {
      this.threshold = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.threshold);
   }

   public void copy(WrapperPlayServerSetCompression wrapper) {
      this.threshold = wrapper.threshold;
   }

   public int getThreshold() {
      return this.threshold;
   }

   public void setThreshold(int threshold) {
      this.threshold = threshold;
   }
}
