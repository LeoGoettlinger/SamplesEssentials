package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.serializer;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.nodes.Node;

public interface AnchorGenerator {
   String nextAnchor(Node var1);
}
