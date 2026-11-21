package org.skriptlang.skript.lang.properties.handlers.base;

import ch.njol.skript.classes.Changer;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface ExpressionPropertyHandler<Type, ReturnType> extends PropertyHandler<Type> {
   @Nullable
   default ReturnType convert(Event event, Type propertyHolder) {
      return this.convert(propertyHolder);
   }

   @Nullable
   ReturnType convert(Type var1);

   @Nullable
   default Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return null;
   }

   default void change(Type propertyHolder, @Nullable Object[] delta, Changer.ChangeMode mode) {
      throw new UnsupportedOperationException("Changing is not supported for this property.");
   }

   default boolean requiresSourceExprChange() {
      return false;
   }

   @NotNull
   Class<ReturnType> returnType();

   @NotNull
   default Class<?>[] possibleReturnTypes() {
      return new Class[]{this.returnType()};
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   @NotNull
   static <Type, ReturnType> ExpressionPropertyHandler<Type, ReturnType> of(
      final Function<Type, ReturnType> converter, @NotNull final Class<ReturnType> returnType
   ) {
      return new ExpressionPropertyHandler<Type, ReturnType>() {
         @Nullable
         @Override
         public ReturnType convert(Type propertyHolder) {
            return converter.apply(propertyHolder);
         }

         @NotNull
         @Override
         public Class<ReturnType> returnType() {
            return returnType;
         }
      };
   }
}
