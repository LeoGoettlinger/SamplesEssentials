package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor.json;

import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlJsonBool extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String val = this.constructScalar(node);
      return BOOL_VALUES.get(val);
   }
}
