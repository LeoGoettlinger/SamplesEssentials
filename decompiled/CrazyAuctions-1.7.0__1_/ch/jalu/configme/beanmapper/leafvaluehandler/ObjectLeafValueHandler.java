package ch.jalu.configme.beanmapper.leafvaluehandler;

import org.jetbrains.annotations.Nullable;

public class ObjectLeafValueHandler extends AbstractLeafValueHandler {
   @Nullable
   @Override
   public Object convert(@Nullable Class<?> clazz, @Nullable Object value) {
      return clazz == Object.class ? value : null;
   }

   @Nullable
   @Override
   public Object toExportValue(@Nullable Object value) {
      return null;
   }
}
