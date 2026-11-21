package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprSubnodeValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

@Deprecated(
   since = "2.13",
   forRemoval = true
)
public interface AnyValued<Type> extends AnyProvider {
   @UnknownNullability
   Type value();

   default <Converted> Converted convertedValue(ClassInfo<Converted> expected) {
      Type value = this.value();
      return value == null ? null : ExprSubnodeValue.convertedValue(value, expected);
   }

   default boolean supportsValueChange() {
      return false;
   }

   default void changeValue(Type value) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
   }

   Class<Type> valueType();

   default void resetValue() throws UnsupportedOperationException {
      this.changeValueSafely(null);
   }

   default void changeValueSafely(Object value) throws UnsupportedOperationException {
      Class<Type> typeClass = this.valueType();
      ClassInfo<? super Type> classInfo = Classes.getSuperClassInfo(typeClass);
      if (value == null) {
         this.changeValue(null);
      } else if (typeClass == String.class) {
         this.changeValue(typeClass.cast(Classes.toString(value, StringMode.MESSAGE)));
      } else if (value instanceof String string && classInfo.getParser() != null && classInfo.getParser().canParse(ParseContext.CONFIG)) {
         Type convert = (Type)classInfo.getParser().parse(string, ParseContext.CONFIG);
         this.changeValue(convert);
      } else {
         Type convert = Converters.convert(value, typeClass);
         this.changeValue(convert);
      }
   }
}
