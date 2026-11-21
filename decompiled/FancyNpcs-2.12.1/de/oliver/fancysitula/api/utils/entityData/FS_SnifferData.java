package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_SnifferData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor STATE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.sniffer.Sniffer", "DATA_STATE"
   );
   public static final String STATE_IDLING = "IDLING";
   public static final String STATE_FEELING_HAPPY = "FEELING_HAPPY";
   public static final String STATE_SCENTING = "SCENTING";
   public static final String STATE_SNIFFING = "SNIFFING";
   public static final String STATE_SEARCHING = "SEARCHING";
   public static final String STATE_DIGGING = "DIGGING";
   public static final String STATE_RISING = "RISING";
}
