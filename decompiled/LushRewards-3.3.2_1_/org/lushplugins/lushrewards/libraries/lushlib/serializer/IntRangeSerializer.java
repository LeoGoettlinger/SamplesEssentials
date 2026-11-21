package org.lushplugins.lushrewards.libraries.lushlib.serializer;

import org.lushplugins.lushrewards.libraries.lushlib.utils.IntRange;

public class IntRangeSerializer extends TextSerializer<IntRange> {
   public String serialize(IntRange intRange) {
      return intRange.toString();
   }
}
