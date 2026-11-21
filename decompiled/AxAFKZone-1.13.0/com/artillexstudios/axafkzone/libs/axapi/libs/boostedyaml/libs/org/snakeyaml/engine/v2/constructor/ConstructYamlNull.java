package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlNull extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      return null;
   }
}
