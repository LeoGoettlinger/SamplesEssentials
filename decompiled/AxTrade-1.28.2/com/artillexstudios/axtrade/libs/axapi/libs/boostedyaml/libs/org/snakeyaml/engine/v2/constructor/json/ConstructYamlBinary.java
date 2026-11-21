package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import java.util.Base64;

public class ConstructYamlBinary extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String noWhiteSpaces = this.constructScalar(node).replaceAll("\\s", "");
      return Base64.getDecoder().decode(noWhiteSpaces);
   }
}
