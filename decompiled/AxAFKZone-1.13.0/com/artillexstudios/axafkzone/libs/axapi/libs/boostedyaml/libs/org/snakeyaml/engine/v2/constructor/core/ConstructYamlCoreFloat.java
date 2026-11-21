package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.core;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.ScalarNode;

public class ConstructYamlCoreFloat extends ConstructYamlJsonFloat {
   @Override
   protected String constructScalar(Node node) {
      return ((ScalarNode)node).getValue().toLowerCase();
   }
}
