package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientSelectTrade extends PacketWrapper<WrapperPlayClientSelectTrade> {
   private int slot;

   public WrapperPlayClientSelectTrade(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientSelectTrade(int slot) {
      super(PacketType.Play.Client.SELECT_TRADE);
      this.slot = slot;
   }

   @Override
   public void read() {
      this.slot = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.slot);
   }

   public void copy(WrapperPlayClientSelectTrade wrapper) {
      this.slot = wrapper.slot;
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }
}
