package org.lushplugins.lushrewards.libraries.lushlib.serializer;

import org.lushplugins.lushrewards.libraries.lushlib.utils.IntRange;

public class IntRangeDeserializer extends TextDeserializer<IntRange> {
   public IntRange deserialize(String text) {
      return IntRange.parseIntRange(text);
   }
}
