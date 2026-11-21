package com.github.retrooper.packetevents.protocol.world.clock;

import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldClock extends MappedEntity, CopyableEntity<WorldClock>, DeepComparableEntity {
   NbtCodec<WorldClock> CODEC = NbtCodecs.forRegistry(WorldClocks.getRegistry());
   NbtCodec<WorldClock> DIRECT_CODEC = NbtCodecs.forUnit(StaticWorldClock::new).codec();

   static WorldClock read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(WorldClocks.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, WorldClock clock) {
      wrapper.writeMappedEntity(clock);
   }
}
