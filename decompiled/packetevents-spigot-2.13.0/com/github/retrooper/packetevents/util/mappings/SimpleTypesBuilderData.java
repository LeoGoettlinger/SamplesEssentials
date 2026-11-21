package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SimpleTypesBuilderData extends TypesBuilderData {
   public SimpleTypesBuilderData(ResourceLocation name, int id) {
      super(name, new int[]{id});
   }

   @Override
   public int getId(ClientVersion version) {
      return this.ids[0];
   }
}
