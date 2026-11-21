package ac.grim.grimac.events.packets.worldreader.multiblockchange;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import io.netty.buffer.ByteBuf;

public interface VersionedMultiBlockChangeHandler {
   int RANGE = 16;
   long TRANSACTION_COOLDOWN_MS = 2L;

   void handleMultiBlockChange(GrimPlayer var1, PacketSendEvent var2);

   default long readVarLong(ByteBuf buf) {
      long value = 0L;
      int size = 0;

      int b;
      while (((b = buf.readByte()) & 128) == 128) {
         value |= (long)(b & 127) << size++ * 7;
      }

      return value | (long)(b & 127) << size * 7;
   }
}
