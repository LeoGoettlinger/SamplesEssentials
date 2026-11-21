package de.oliver.fancysitula.api.packets;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;

public abstract class FS_ClientboundBundlePacket extends FS_ClientboundPacket {
   protected final List<FS_ClientboundPacket> packets;

   public FS_ClientboundBundlePacket(List<FS_ClientboundPacket> packets) {
      this.packets = new ArrayList<>(packets);
   }

   public List<FS_ClientboundPacket> getPackets() {
      return this.packets;
   }

   @ApiStatus.Internal
   protected List<Object> createRawPackets() {
      List<Object> rawPackets = new ArrayList<>();

      for (FS_ClientboundPacket packet : this.packets) {
         Object raw = packet.createPacket();
         if (raw != null) {
            rawPackets.add(raw);
         }
      }

      return rawPackets;
   }
}
