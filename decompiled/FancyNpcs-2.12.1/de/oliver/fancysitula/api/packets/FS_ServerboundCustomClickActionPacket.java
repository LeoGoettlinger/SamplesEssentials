package de.oliver.fancysitula.api.packets;

import java.util.Map;
import net.kyori.adventure.key.Key;

public class FS_ServerboundCustomClickActionPacket extends FS_ServerboundPacket {
   private final Key id;
   private final Map<String, String> payload;

   public FS_ServerboundCustomClickActionPacket(FS_ServerboundPacket.Type type, Key id, Map<String, String> payload) {
      super(type);
      this.id = id;
      this.payload = payload;
   }

   public Key getId() {
      return this.id;
   }

   public Map<String, String> getPayload() {
      return this.payload;
   }

   @Override
   public FS_ServerboundPacket.Type getType() {
      return super.getType();
   }
}
