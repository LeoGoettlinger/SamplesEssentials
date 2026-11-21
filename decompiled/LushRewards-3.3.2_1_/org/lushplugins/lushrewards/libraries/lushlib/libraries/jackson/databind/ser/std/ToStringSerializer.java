package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;

@JacksonStdImpl
public class ToStringSerializer extends ToStringSerializerBase {
   public static final ToStringSerializer instance = new ToStringSerializer();

   public ToStringSerializer() {
      super(Object.class);
   }

   public ToStringSerializer(Class<?> handledType) {
      super(handledType);
   }

   @Override
   public final String valueToString(Object value) {
      return value.toString();
   }
}
