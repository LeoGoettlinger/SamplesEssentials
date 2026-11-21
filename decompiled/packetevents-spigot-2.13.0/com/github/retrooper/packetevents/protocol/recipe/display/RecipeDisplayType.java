package com.github.retrooper.packetevents.protocol.recipe.display;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public interface RecipeDisplayType<T extends RecipeDisplay<?>> extends MappedEntity {
   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T display);
}
