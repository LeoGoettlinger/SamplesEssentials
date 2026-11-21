package ch.jalu.configme.beanmapper.leafvaluehandler;

import org.jetbrains.annotations.Nullable;

public class EnumLeafValueHandler extends AbstractLeafValueHandler {
   @Nullable
   @Override
   public Object convert(@Nullable Class<?> clazz, @Nullable Object value) {
      if (value instanceof String && Enum.class.isAssignableFrom(clazz)) {
         String givenText = (String)value;

         for (Enum e : (Enum[])clazz.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(givenText)) {
               return e;
            }
         }
      }

      return null;
   }

   @Nullable
   @Override
   public Object toExportValue(@Nullable Object value) {
      return value instanceof Enum ? ((Enum)value).name() : null;
   }
}
