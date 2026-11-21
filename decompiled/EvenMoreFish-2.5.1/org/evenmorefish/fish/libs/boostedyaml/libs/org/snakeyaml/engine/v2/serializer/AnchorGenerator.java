package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.serializer;

import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;

public interface AnchorGenerator {
   Anchor nextAnchor(Node var1);
}
