package org.snakeyaml.engine.v2.parser;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.snakeyaml.engine.v2.common.SpecVersion;

class VersionTagsTuple {
   private final Optional<SpecVersion> specVersion;
   private final Map<String, String> tags;

   public VersionTagsTuple(Optional<SpecVersion> specVersion, Map<String, String> tags) {
      Objects.requireNonNull(specVersion);
      this.specVersion = specVersion;
      this.tags = tags;
   }

   public Optional<SpecVersion> getSpecVersion() {
      return this.specVersion;
   }

   public Map<String, String> getTags() {
      return this.tags;
   }

   @Override
   public String toString() {
      return String.format("VersionTagsTuple<%s, %s>", this.specVersion, this.tags);
   }
}
