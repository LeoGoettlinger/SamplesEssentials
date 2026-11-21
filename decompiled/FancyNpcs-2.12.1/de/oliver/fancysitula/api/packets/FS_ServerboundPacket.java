package de.oliver.fancysitula.api.packets;

public abstract class FS_ServerboundPacket {
   protected final FS_ServerboundPacket.Type type;

   public FS_ServerboundPacket(FS_ServerboundPacket.Type type) {
      this.type = type;
   }

   public FS_ServerboundPacket.Type getType() {
      return this.type;
   }

   public static enum Type {
      ALL("*"),
      CUSTOM_CLICK_ACTION("ServerboundCustomClickActionPacket");

      private String packetClassName;

      private Type(String packetClassName) {
         this.packetClassName = packetClassName;
      }

      public String getPacketClassName() {
         return this.packetClassName;
      }
   }
}
