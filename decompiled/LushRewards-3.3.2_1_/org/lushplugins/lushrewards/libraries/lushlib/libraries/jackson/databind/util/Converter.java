package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeFactory;

public interface Converter<IN, OUT> {
   OUT convert(IN var1);

   JavaType getInputType(TypeFactory var1);

   JavaType getOutputType(TypeFactory var1);

   public abstract static class None implements Converter<Object, Object> {
   }
}
