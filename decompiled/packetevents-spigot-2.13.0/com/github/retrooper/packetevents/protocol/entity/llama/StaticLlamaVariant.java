package com.github.retrooper.packetevents.protocol.entity.llama;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticLlamaVariant extends AbstractMappedEntity implements LlamaVariant {
   @Internal
   public StaticLlamaVariant(@Nullable TypesBuilderData data) {
      super(data);
   }
}
