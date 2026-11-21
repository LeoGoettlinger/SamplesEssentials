package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.json;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.constructor.ConstructScalar;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import java.math.BigInteger;

public class ConstructYamlJsonInt extends ConstructScalar {
   @Override
   public Object construct(Node node) {
      String value = this.constructScalar(node);
      return this.createIntNumber(value);
   }

   protected Number createIntNumber(String number) {
      Number result;
      try {
         result = Integer.valueOf(number);
      } catch (NumberFormatException var6) {
         try {
            result = Long.valueOf(number);
         } catch (NumberFormatException var5) {
            result = new BigInteger(number);
         }
      }

      return result;
   }
}
