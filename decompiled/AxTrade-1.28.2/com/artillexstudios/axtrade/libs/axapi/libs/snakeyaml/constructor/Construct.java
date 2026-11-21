package com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.constructor;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.nodes.Node;

public interface Construct {
   Object construct(Node var1);

   void construct2ndStep(Node var1, Object var2);
}
