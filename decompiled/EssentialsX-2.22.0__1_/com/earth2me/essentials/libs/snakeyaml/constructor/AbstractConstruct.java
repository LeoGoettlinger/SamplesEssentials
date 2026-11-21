package com.earth2me.essentials.libs.snakeyaml.constructor;

import com.earth2me.essentials.libs.snakeyaml.error.YAMLException;
import com.earth2me.essentials.libs.snakeyaml.nodes.Node;

public abstract class AbstractConstruct implements Construct {
   @Override
   public void construct2ndStep(Node node, Object data) {
      if (node.isTwoStepsConstruction()) {
         throw new IllegalStateException("Not Implemented in " + this.getClass().getName());
      } else {
         throw new YAMLException("Unexpected recursive structure for Node: " + node);
      }
   }
}
