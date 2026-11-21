package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlJsonBool extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String val = this.constructScalar(node);
      return BOOL_VALUES.get(val);
   }
}
