package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.schema;

import java.util.Map;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.ConstructNode;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver.ScalarResolver;

public interface Schema {
   ScalarResolver getScalarResolver();

   Map<Tag, ConstructNode> getSchemaTagConstructors();
}
