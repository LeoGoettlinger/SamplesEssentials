package org.skriptlang.skript.lang.properties.handlers;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Experimental
public interface ContainsHandler<Container, Element> extends PropertyHandler<Container> {
   boolean contains(Container var1, Element var2);

   Class<? extends Element>[] elementTypes();

   default boolean canContain(Class<?> type) {
      for (Class<? extends Element> elementType : this.elementTypes()) {
         if (elementType.isAssignableFrom(type)) {
            return true;
         }
      }

      return false;
   }
}
