package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.constructor;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.nodes.Node;

public interface Construct {
   Object construct(Node var1);

   void construct2ndStep(Node var1, Object var2);
}
