package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor;

import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlNull extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      return null;
   }
}
