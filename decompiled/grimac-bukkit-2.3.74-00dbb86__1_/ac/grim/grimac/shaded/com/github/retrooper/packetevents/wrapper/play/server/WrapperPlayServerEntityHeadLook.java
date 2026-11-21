package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerEntityHeadLook extends PacketWrapper<WrapperPlayServerEntityHeadLook> {
   private static final float ROTATION_FACTOR = 0.7111111F;
   private int entityID;
   private float headYaw;

   public WrapperPlayServerEntityHeadLook(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerEntityHeadLook(int entityID, float headYaw) {
      super(PacketType.Play.Server.ENTITY_HEAD_LOOK);
      this.entityID = entityID;
      this.headYaw = headYaw;
   }

   @Override
   public void read() {
      this.entityID = this.readVarInt();
      this.headYaw = this.readByte() / 0.7111111F;
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityID);
      this.writeByte((int)(this.headYaw * 0.7111111F));
   }

   public void copy(WrapperPlayServerEntityHeadLook wrapper) {
      this.entityID = wrapper.entityID;
      this.headYaw = wrapper.headYaw;
   }

   public int getEntityId() {
      return this.entityID;
   }

   public void setEntityId(int entityID) {
      this.entityID = entityID;
   }

   public float getHeadYaw() {
      return this.headYaw;
   }

   public void setHeadYaw(float headYaw) {
      this.headYaw = headYaw;
   }
}
