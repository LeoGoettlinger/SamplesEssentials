package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import java.util.UUID;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructUuidClass extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String uuidValue = this.constructScalar(node);
      return UUID.fromString(uuidValue);
   }
}
