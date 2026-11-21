package org.skriptlang.skript.lang.properties.handlers.base;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface PropertyHandler<Type> {
   default PropertyHandler<Type> newInstance() {
      return this;
   }

   default boolean init(Expression<?> parentExpression, ParserInstance parser) {
      return true;
   }
}
