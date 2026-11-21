package com.github.retrooper.packetevents.protocol.debug.struct;

import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DebugRaidsInfo {
   private final List<Vector3i> positions;

   public DebugRaidsInfo(List<Vector3i> positions) {
      this.positions = positions;
   }

   public static DebugRaidsInfo read(PacketWrapper<?> wrapper) {
      List<Vector3i> positions = wrapper.readList(PacketWrapper::readBlockPosition);
      return new DebugRaidsInfo(positions);
   }

   public static void write(PacketWrapper<?> wrapper, DebugRaidsInfo info) {
      wrapper.writeList(info.positions, PacketWrapper::writeBlockPosition);
   }

   public List<Vector3i> getPositions() {
      return this.positions;
   }
}
