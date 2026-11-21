package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_ArmadilloData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor STATE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.armadillo.Armadillo", "DATA_STATE"
   );
   public static final String STATE_IDLE = "IDLE";
   public static final String STATE_ROLLING = "ROLLING";
   public static final String STATE_SCARED = "SCARED";
   public static final String STATE_UNROLLING = "UNROLLING";
}
