package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions;

import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.SpecVersion;

public class YamlVersionException extends YamlEngineException {
   private final SpecVersion specVersion;

   public YamlVersionException(SpecVersion specVersion) {
      super(specVersion.toString());
      this.specVersion = specVersion;
   }

   public SpecVersion getSpecVersion() {
      return this.specVersion;
   }
}
