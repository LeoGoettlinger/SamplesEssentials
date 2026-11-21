package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor.json;

import java.util.Base64;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlBinary extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String noWhiteSpaces = this.constructScalar(node).replaceAll("\\s", "");
      return Base64.getDecoder().decode(noWhiteSpaces);
   }
}
