package ac.grim.grimac.events.packets.worldreader.multiblockchange;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import io.netty.buffer.ByteBuf;

public final class V1160MultiBlockChangeBitRepackHandler implements VersionedMultiBlockChangeHandler {
   private static final int SHIFT_STATE = 17;
   private static final int MASK_STATE = 32767;
   static final int MASK_LOCAL = 4095;
   private static final boolean HAS_TRUST_EDGES = PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_19_4);

   @Override
   public void handleMultiBlockChange(GrimPlayer player, PacketSendEvent event) {
      ByteBuf buf = (ByteBuf)event.getByteBuf();
      long sectionEncodedPosition = ByteBufHelper.readLong(buf);
      if (HAS_TRUST_EDGES) {
         buf.skipBytes(1);
      }

      int recordCount = ByteBufHelper.readVarInt(buf);
      int[] packed = new int[recordCount];
      int secX = (int)(sectionEncodedPosition >> 42);
      int secZ = (int)(sectionEncodedPosition << 22 >> 42);
      int secY = (int)(sectionEncodedPosition << 44 >> 44);
      int baseX = secX << 4;
      int baseY = secY << 4;
      int baseZ = secZ << 4;
      boolean sendTx = false;
      long now = System.currentTimeMillis();

      for (int i = 0; i < recordCount; i++) {
         long data = this.readVarLong(buf);
         int local = (int)(data & 4095L);
         packed[i] = this.repackFromLong(data);
         if (!sendTx) {
            int lx = local >>> 8 & 15;
            int lz = local >>> 4 & 15;
            int ly = local & 15;
            int wx = baseX + lx;
            int wy = baseY + ly;
            int wz = baseZ + lz;
            if (Math.abs(wx - player.x) < 16.0 && Math.abs(wy - player.y) < 16.0 && Math.abs(wz - player.z) < 16.0 && player.lastTransSent + 2L < now) {
               sendTx = true;
            }
         }
      }

      if (sendTx) {
         player.sendTransaction();
      }

      player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
         int sX = (int)(sectionEncodedPosition >> 42);
         int sY = (int)(sectionEncodedPosition << 44 >> 44);
         int sZ = (int)(sectionEncodedPosition << 22 >> 42);
         int bx = sX << 4;
         int by = sY << 4;
         int bz = sZ << 4;

         for (int rec : packed) {
            int stateId = rec >>> 17 & 32767;
            int lxx = rec >>> 8 & 15;
            int lzx = rec >>> 4 & 15;
            int lyx = rec & 15;
            int wxx = bx + lxx;
            int wyx = by + lyx;
            int wzx = bz + lzx;
            player.compensatedWorld.updateBlock(wxx, wyx, wzx, stateId);
         }
      });
   }

   public int repackFromLong(long data) {
      int blockState = (int)(data >>> 12 & 32767L);
      int local = (int)(data & 4095L);
      return blockState << 17 | local;
   }
}
