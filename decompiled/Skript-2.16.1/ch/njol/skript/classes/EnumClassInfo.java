package ch.njol.skript.classes;

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.util.coll.iterator.ArrayIterator;
import java.util.function.Consumer;

public class EnumClassInfo<T extends Enum<T>> extends ClassInfo<T> {
   public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode) {
      this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass));
   }

   public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, Consumer<T> parseCallback) {
      this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass), parseCallback);
   }

   public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression) {
      this(enumClass, codeName, languageNode, defaultExpression, ignored -> {});
   }

   public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression, Consumer<T> parseCallback) {
      super(enumClass, codeName);
      EnumParser<T> enumParser = new EnumParser<>(enumClass, languageNode, parseCallback);
      this.usage(new String[]{enumParser.getCombinedPatterns()})
         .serializer(new EnumSerializer<>(enumClass))
         .defaultExpression(defaultExpression)
         .supplier(() -> new ArrayIterator<>(enumClass.getEnumConstants()))
         .parser(enumParser);
   }

   @Deprecated(
      since = "2.16",
      forRemoval = true
   )
   public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, boolean registerComparator) {
      this(enumClass, codeName, languageNode, new EventValueExpression<>(enumClass));
   }

   @Deprecated(
      since = "2.16",
      forRemoval = true
   )
   public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, DefaultExpression<T> defaultExpression, boolean registerComparator) {
      this(enumClass, codeName, languageNode, defaultExpression);
   }
}
