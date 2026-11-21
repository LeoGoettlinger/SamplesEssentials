package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperPlayClientAttack extends PacketWrapper<WrapperPlayClientAttack> {
   private int entityId;

   public WrapperPlayClientAttack(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientAttack(int entityId) {
      super(PacketType.Play.Client.ATTACK);
      this.entityId = entityId;
   }

   @Override
   public void read() {
      this.entityId = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityId);
   }

   public void copy(WrapperPlayClientAttack wrapper) {
      this.entityId = wrapper.entityId;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }
}
