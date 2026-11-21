package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_PandaData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor UNHAPPY_COUNTER = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.panda.Panda", "UNHAPPY_COUNTER"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SNEEZE_COUNTER = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.panda.Panda", "SNEEZE_COUNTER"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor EAT_COUNTER = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.panda.Panda", "EAT_COUNTER"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor MAIN_GENE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.panda.Panda", "MAIN_GENE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HIDDEN_GENE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.panda.Panda", "HIDDEN_GENE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.panda.Panda", "DATA_ID_FLAGS"
   );
   public static final byte GENE_NORMAL = 0;
   public static final byte GENE_LAZY = 1;
   public static final byte GENE_WORRIED = 2;
   public static final byte GENE_PLAYFUL = 3;
   public static final byte GENE_BROWN = 4;
   public static final byte GENE_WEAK = 5;
   public static final byte GENE_AGGRESSIVE = 6;
   public static final byte FLAG_SNEEZING = 2;
   public static final byte FLAG_ROLLING = 4;
   public static final byte FLAG_SITTING = 8;
   public static final byte FLAG_ON_BACK = 16;
}
