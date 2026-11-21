package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlJsonFloat extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String value = this.constructScalar(node);
      if (".inf".equals(value)) {
         return Double.POSITIVE_INFINITY;
      } else if ("-.inf".equals(value)) {
         return Double.NEGATIVE_INFINITY;
      } else {
         return ".nan".equals(value) ? Double.NaN : this.constructFromString(value);
      }
   }

   protected Object constructFromString(String value) {
      int sign = 1;
      char first = value.charAt(0);
      if (first == '-') {
         sign = -1;
         value = value.substring(1);
      } else if (first == '+') {
         value = value.substring(1);
      }

      double d = Double.valueOf(value);
      return d * sign;
   }
}
