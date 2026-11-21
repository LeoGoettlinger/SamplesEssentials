package ch.jalu.configme.beanmapper.leafvaluehandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumberLeafValueHandler extends AbstractLeafValueHandler {
   private static final Map<Class<?>, Function<Number, Number>> NUMBER_CLASSES_TO_CONVERSION = createMapOfTypeToTransformFunction();

   @Nullable
   @Override
   public Object convert(@Nullable Class<?> clazz, @Nullable Object value) {
      if (value instanceof Number) {
         Function<Number, Number> numberFunction = NUMBER_CLASSES_TO_CONVERSION.get(clazz);
         return numberFunction == null ? null : numberFunction.apply((Number)value);
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Object toExportValue(@Nullable Object value) {
      Class<?> clazz = value == null ? null : value.getClass();
      return NUMBER_CLASSES_TO_CONVERSION.containsKey(clazz) ? value : null;
   }

   @NotNull
   private static Map<Class<?>, Function<Number, Number>> createMapOfTypeToTransformFunction() {
      Map<Class<?>, Function<Number, Number>> map = new HashMap<>();
      map.put(byte.class, Number::byteValue);
      map.put(Byte.class, Number::byteValue);
      map.put(short.class, Number::shortValue);
      map.put(Short.class, Number::shortValue);
      map.put(int.class, Number::intValue);
      map.put(Integer.class, Number::intValue);
      map.put(long.class, Number::longValue);
      map.put(Long.class, Number::longValue);
      map.put(float.class, Number::floatValue);
      map.put(Float.class, Number::floatValue);
      map.put(double.class, Number::doubleValue);
      map.put(Double.class, Number::doubleValue);
      return Collections.unmodifiableMap(map);
   }
}
