package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerDestroyEntities extends PacketWrapper<WrapperPlayServerDestroyEntities> {
   private int[] entityIDs;

   public WrapperPlayServerDestroyEntities(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerDestroyEntities(int... entityIDs) {
      super(PacketType.Play.Server.DESTROY_ENTITIES);
      this.entityIDs = entityIDs;
   }

   public WrapperPlayServerDestroyEntities(int entityID) {
      super(PacketType.Play.Server.DESTROY_ENTITIES);
      this.entityIDs = new int[]{entityID};
   }

   @Override
   public void read() {
      if (this.serverVersion == ServerVersion.V_1_17) {
         this.entityIDs = new int[]{this.readVarInt()};
      } else if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         int entityIDCount = this.readUnsignedByte();
         this.entityIDs = new int[entityIDCount];

         for (int i = 0; i < entityIDCount; i++) {
            this.entityIDs[i] = this.readInt();
         }
      } else {
         int entityIDCount = this.readVarInt();
         this.entityIDs = new int[entityIDCount];

         for (int i = 0; i < entityIDCount; i++) {
            this.entityIDs[i] = this.readVarInt();
         }
      }
   }

   @Override
   public void write() {
      if (this.serverVersion == ServerVersion.V_1_17) {
         this.writeVarInt(this.entityIDs[0]);
      } else if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         this.writeByte(this.entityIDs.length);

         for (int entityID : this.entityIDs) {
            this.writeInt(entityID);
         }
      } else {
         this.writeVarInt(this.entityIDs.length);

         for (int entityID : this.entityIDs) {
            this.writeVarInt(entityID);
         }
      }
   }

   public void copy(WrapperPlayServerDestroyEntities wrapper) {
      this.entityIDs = wrapper.entityIDs;
   }

   public int[] getEntityIds() {
      return this.entityIDs;
   }

   public void setEntityIds(int[] entityIDs) {
      this.entityIDs = entityIDs;
   }
}
