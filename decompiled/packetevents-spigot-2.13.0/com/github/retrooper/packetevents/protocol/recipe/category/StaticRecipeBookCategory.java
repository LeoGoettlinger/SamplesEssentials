package com.github.retrooper.packetevents.protocol.recipe.category;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticRecipeBookCategory extends AbstractMappedEntity implements RecipeBookCategory {
   @Internal
   public StaticRecipeBookCategory(@Nullable TypesBuilderData data) {
      super(data);
   }
}
