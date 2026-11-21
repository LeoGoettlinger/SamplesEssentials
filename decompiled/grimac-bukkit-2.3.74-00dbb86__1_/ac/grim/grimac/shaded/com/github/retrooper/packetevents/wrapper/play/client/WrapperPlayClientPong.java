package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientPong extends PacketWrapper<WrapperPlayClientPong> {
   private int id;

   public WrapperPlayClientPong(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientPong(int id) {
      super(PacketType.Play.Client.PONG);
      this.id = id;
   }

   @Override
   public void read() {
      this.id = this.readInt();
   }

   @Override
   public void write() {
      this.writeInt(this.id);
   }

   public void copy(WrapperPlayClientPong wrapper) {
      this.id = wrapper.id;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }
}
