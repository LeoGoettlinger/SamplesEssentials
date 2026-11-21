package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.VersionRange;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TypesBuilderData {
   protected final ResourceLocation name;
   protected final int[] ids;
   protected final TypesBuilder typesBuilder;
   protected final VersionRange versions;

   @Deprecated
   public TypesBuilderData(ResourceLocation name, int[] ids) {
      this(name, ids, new TypesBuilder("", true), VersionRange.ALL_VERSIONS);
   }

   @Internal
   public TypesBuilderData(ResourceLocation name, int[] ids, TypesBuilder typesBuilder, VersionRange versions) {
      this.name = name;
      this.ids = ids;
      this.typesBuilder = typesBuilder;
      this.versions = versions;
   }

   public int getId(ClientVersion version) {
      return this.ids[this.typesBuilder.getDataIndex(version)];
   }

   public ResourceLocation getName() {
      return this.name;
   }

   @Deprecated
   public int[] getData() {
      return this.ids;
   }

   public VersionRange getVersions() {
      return this.versions;
   }
}
