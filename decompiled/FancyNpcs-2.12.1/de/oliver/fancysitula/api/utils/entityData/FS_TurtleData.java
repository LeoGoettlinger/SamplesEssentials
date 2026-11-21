package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_TurtleData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LAYING_EGG = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.turtle.Turtle", "LAYING_EGG"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LAYING_EGG_LEGACY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.Turtle", "LAYING_EGG"
   );
}
