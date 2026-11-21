package org.skriptlang.skript.lang.properties.handlers;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprSubnodeValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Experimental
public interface TypedValueHandler<Type, ValueType> extends ExpressionPropertyHandler<Type, ValueType> {
   @Nullable
   @Override
   ValueType convert(Type var1);

   default <Converted> Converted convert(Type propertyHolder, ClassInfo<Converted> expected) {
      ValueType value = this.convert(propertyHolder);
      return value == null ? null : ExprSubnodeValue.convertedValue(value, expected);
   }

   default ValueType convertChangeValue(Object value) throws UnsupportedOperationException {
      Class<ValueType> typeClass = this.returnType();
      ClassInfo<? super ValueType> classInfo = Classes.getSuperClassInfo(typeClass);
      if (value == null) {
         return null;
      } else if (typeClass == String.class) {
         return typeClass.cast(Classes.toString(value, StringMode.MESSAGE));
      } else {
         return (ValueType)(value instanceof String string && classInfo.getParser() != null && classInfo.getParser().canParse(ParseContext.CONFIG)
            ? classInfo.getParser().parse(string, ParseContext.CONFIG)
            : Converters.convert(value, typeClass));
      }
   }
}
