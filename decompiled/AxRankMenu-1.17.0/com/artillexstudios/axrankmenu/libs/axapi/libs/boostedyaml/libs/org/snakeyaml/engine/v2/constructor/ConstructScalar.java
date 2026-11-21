package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.ConstructNode;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.ScalarNode;
import java.util.HashMap;
import java.util.Map;

public abstract class ConstructScalar implements ConstructNode {
   protected static final Map<String, Boolean> BOOL_VALUES = new HashMap<>();

   protected String constructScalar(Node node) {
      return ((ScalarNode)node).getValue();
   }

   static {
      BOOL_VALUES.put("true", Boolean.TRUE);
      BOOL_VALUES.put("false", Boolean.FALSE);
   }
}
