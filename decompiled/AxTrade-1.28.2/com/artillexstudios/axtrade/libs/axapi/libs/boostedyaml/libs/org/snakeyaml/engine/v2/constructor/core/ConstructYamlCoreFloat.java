package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.core;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.ScalarNode;

public class ConstructYamlCoreFloat extends ConstructYamlJsonFloat {
   @Override
   protected String constructScalar(Node node) {
      return ((ScalarNode)node).getValue().toLowerCase();
   }
}
