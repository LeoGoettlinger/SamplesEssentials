package com.github.retrooper.packetevents.protocol.recipe;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.recipe.data.RecipeData;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticRecipeSerializer<T extends RecipeData> extends AbstractMappedEntity implements RecipeSerializer<T> {
   private final PacketWrapper.Reader<T> reader;
   private final PacketWrapper.Writer<T> writer;
   @Nullable
   private final RecipeType legacyType;

   @Internal
   public StaticRecipeSerializer(
      @Nullable TypesBuilderData data, PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer, @Nullable RecipeType legacyType
   ) {
      super(data);
      this.reader = reader;
      this.writer = writer;
      this.legacyType = legacyType;
   }

   @Override
   public RecipeType getLegacyType() {
      return this.legacyType;
   }

   @Override
   public T read(PacketWrapper<?> wrapper) {
      return this.reader.apply(wrapper);
   }

   @Override
   public void write(PacketWrapper<?> wrapper, T data) {
      this.writer.accept(wrapper, data);
   }
}
