package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.debug.DebugSubscription;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperPlayServerDebugEntityValue extends PacketWrapper<WrapperPlayServerDebugEntityValue> {
   private int entityId;
   private DebugSubscription.Update<?> update;

   public WrapperPlayServerDebugEntityValue(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerDebugEntityValue(int entityId, DebugSubscription.Update<?> update) {
      super(PacketType.Play.Server.DEBUG_ENTITY_VALUE);
      this.entityId = entityId;
      this.update = update;
   }

   @Override
   public void read() {
      this.entityId = this.readVarInt();
      this.update = DebugSubscription.Update.read(this);
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityId);
      DebugSubscription.Update.write(this, this.update);
   }

   public void copy(WrapperPlayServerDebugEntityValue wrapper) {
      this.entityId = wrapper.entityId;
      this.update = wrapper.update;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public DebugSubscription.Update<?> getUpdate() {
      return this.update;
   }

   public void setUpdate(DebugSubscription.Update<?> update) {
      this.update = update;
   }
}
