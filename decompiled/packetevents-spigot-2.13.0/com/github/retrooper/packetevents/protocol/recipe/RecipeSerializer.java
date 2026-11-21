package com.github.retrooper.packetevents.protocol.recipe;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.recipe.data.RecipeData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Obsolete
public interface RecipeSerializer<T extends RecipeData> extends MappedEntity {
   @Deprecated
   RecipeType getLegacyType();

   T read(PacketWrapper<?> wrapper);

   void write(PacketWrapper<?> wrapper, T data);
}
