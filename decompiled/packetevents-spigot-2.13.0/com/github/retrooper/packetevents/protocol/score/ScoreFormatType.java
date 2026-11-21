package com.github.retrooper.packetevents.protocol.score;

import com.github.retrooper.packetevents.protocol.mapper.StaticMappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public interface ScoreFormatType<T extends ScoreFormat> extends StaticMappedEntity {
   @Deprecated
   @Override
   int getId();

   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T format);
}
