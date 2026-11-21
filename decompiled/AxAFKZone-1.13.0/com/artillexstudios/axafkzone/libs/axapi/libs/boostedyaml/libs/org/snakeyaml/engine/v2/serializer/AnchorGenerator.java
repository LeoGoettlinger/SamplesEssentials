package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.serializer;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;

public interface AnchorGenerator {
   Anchor nextAnchor(Node var1);
}
