package org.skriptlang.skript.lang.properties;

import java.util.HashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Experimental
public class PropertyMap<Handler extends PropertyHandler<?>> extends HashMap<Class<?>, Property.PropertyInfo<Handler>> {
   @Nullable
   public Handler getHandler(Class<?> inputClass) {
      Property.PropertyInfo<Handler> propertyInfo = this.get(inputClass);
      return propertyInfo == null ? null : propertyInfo.handler();
   }

   public Property.PropertyInfo<Handler> get(Class<?> actualClass) {
      if (super.containsKey(actualClass)) {
         return (Property.PropertyInfo<Handler>)super.get(actualClass);
      } else {
         Class<?> closestClass = null;

         for (Class<?> candidateClass : this.keySet()) {
            if (candidateClass.isAssignableFrom(actualClass) && (closestClass == null || closestClass.isAssignableFrom(candidateClass))) {
               closestClass = candidateClass;
            }
         }

         Property.PropertyInfo<Handler> propertyInfo = (Property.PropertyInfo<Handler>)super.get(closestClass);
         this.put(actualClass, propertyInfo);
         return propertyInfo;
      }
   }
}
