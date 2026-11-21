package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.api;

import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;

public interface ConstructNode {
   Object construct(Node var1);

   default void constructRecursive(Node node, Object object) {
      if (node.isRecursive()) {
         throw new IllegalStateException("Not implemented in " + this.getClass().getName());
      } else {
         throw new YamlEngineException("Unexpected recursive structure for Node: " + node);
      }
   }
}
