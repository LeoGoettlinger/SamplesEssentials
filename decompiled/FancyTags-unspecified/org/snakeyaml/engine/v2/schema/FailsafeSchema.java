package org.snakeyaml.engine.v2.schema;

import java.util.HashMap;
import java.util.Map;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.FailsafeScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

public class FailsafeSchema implements Schema {
   @Override
   public ScalarResolver getScalarResolver() {
      return new FailsafeScalarResolver();
   }

   @Override
   public Map<Tag, ConstructNode> getSchemaTagConstructors() {
      return new HashMap<>();
   }
}
