package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.status.client;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperStatusClientRequest extends PacketWrapper<WrapperStatusClientRequest> {
   public WrapperStatusClientRequest(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperStatusClientRequest() {
      super(PacketType.Status.Client.REQUEST);
   }

   @Override
   public void read() {
   }

   @Override
   public void write() {
   }

   public void copy(WrapperStatusClientRequest wrapper) {
   }
}
