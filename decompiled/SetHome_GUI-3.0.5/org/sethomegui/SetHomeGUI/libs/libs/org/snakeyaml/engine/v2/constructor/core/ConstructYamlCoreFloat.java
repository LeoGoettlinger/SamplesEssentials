package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor.core;

import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.ScalarNode;

public class ConstructYamlCoreFloat extends ConstructYamlJsonFloat {
   @Override
   protected String constructScalar(Node node) {
      return ((ScalarNode)node).getValue().toLowerCase();
   }
}
