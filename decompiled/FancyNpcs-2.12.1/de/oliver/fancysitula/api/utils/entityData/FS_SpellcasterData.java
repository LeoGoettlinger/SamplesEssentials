package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_SpellcasterData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SPELL_CASTING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.SpellcasterIllager", "DATA_SPELL_CASTING_ID"
   );
   public static final byte SPELL_NONE = 0;
   public static final byte SPELL_SUMMON_VEX = 1;
   public static final byte SPELL_FANGS = 2;
   public static final byte SPELL_WOLOLO = 3;
   public static final byte SPELL_DISAPPEAR = 4;
   public static final byte SPELL_BLINDNESS = 5;
}
