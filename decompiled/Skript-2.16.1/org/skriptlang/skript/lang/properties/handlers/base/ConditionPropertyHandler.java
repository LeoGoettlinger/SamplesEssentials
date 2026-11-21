package org.skriptlang.skript.lang.properties.handlers.base;

import java.util.function.Predicate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface ConditionPropertyHandler<Type> extends PropertyHandler<Type> {
   boolean check(Type var1);

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   static <Type> ConditionPropertyHandler<Type> of(Predicate<Type> predicate) {
      return predicate::test;
   }
}
