package com.github.retrooper.packetevents.protocol.entity.tropicalfish;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;

public interface TropicalFishPattern extends MappedEntity {
   TropicalFishPattern.Base getBase();

   public static enum Base {
      SMALL,
      LARGE;
   }
}
