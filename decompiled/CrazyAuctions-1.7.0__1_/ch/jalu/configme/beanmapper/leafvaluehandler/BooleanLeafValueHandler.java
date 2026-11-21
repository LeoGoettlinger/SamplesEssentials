package ch.jalu.configme.beanmapper.leafvaluehandler;

import org.jetbrains.annotations.Nullable;

public class BooleanLeafValueHandler extends AbstractLeafValueHandler {
   @Nullable
   @Override
   public Object convert(@Nullable Class<?> clazz, @Nullable Object value) {
      return (clazz == boolean.class || clazz == Boolean.class) && value instanceof Boolean ? value : null;
   }

   @Nullable
   @Override
   public Object toExportValue(@Nullable Object value) {
      return value instanceof Boolean ? value : null;
   }
}
