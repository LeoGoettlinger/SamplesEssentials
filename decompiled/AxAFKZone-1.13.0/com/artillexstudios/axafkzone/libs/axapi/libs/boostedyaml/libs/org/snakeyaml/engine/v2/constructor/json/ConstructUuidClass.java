package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import java.util.UUID;

public class ConstructUuidClass extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String uuidValue = this.constructScalar(node);
      return UUID.fromString(uuidValue);
   }
}
