package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;

final class EscapeTagImpl implements EscapeTag {
   public static final EscapeTag INSTANCE = new EscapeTagImpl();

   private EscapeTagImpl() {
   }

   @Override
   public byte getTypeId() {
      return 0;
   }

   @Override
   public void write(NBTOutputStream outputStream) throws IOException {
      outputStream.writeByte(this.getTypeId());
   }

   @Override
   public String toString() {
      return "EscapeTag.INSTANCE";
   }
}
