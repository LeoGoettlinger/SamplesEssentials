package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jdbi.v3.core.config.internal.ConfigCache;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.enums.DatabaseValue;
import org.jdbi.v3.core.internal.exceptions.Unchecked;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import org.jdbi.v3.core.statement.StatementContext;

public abstract class EnumMapper<E extends Enum<E>> implements ColumnMapper<E> {
   EnumMapper() {
   }

   public static <E extends Enum<E>> ColumnMapper<E> byName(Class<E> type) {
      return new EnumMapper.EnumByNameColumnMapper<>(type);
   }

   public static <E extends Enum<E>> ColumnMapper<E> byOrdinal(Class<E> type) {
      return new EnumMapper.EnumByOrdinalColumnMapper<>(type);
   }

   static class EnumByNameColumnMapper<E extends Enum<E>> implements ColumnMapper<E> {
      private static final ConfigCache<Class<? extends Enum<?>>, ConfigCache<String, Enum<?>>> BY_NAME_CACHE = ConfigCaches.declare(
         e -> ConfigCaches.declare(name -> (Enum<?>)e.cast(getValueByName(e, name)))
      );
      private final Class<E> enumClass;

      private EnumByNameColumnMapper(Class<E> enumClass) {
         this.enumClass = enumClass;
      }

      public E map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
         String name = rs.getString(columnNumber);
         return name != null && !name.isEmpty() ? this.enumClass.cast(BY_NAME_CACHE.get(this.enumClass, ctx).get(name, ctx)) : null;
      }

      private static Object getValueByName(Class<? extends Enum<?>> enumClass, String name) {
         return Stream.concat(
               enumConstants(enumClass).filter(enumValueMatches(name, String::equals)),
               enumConstants(enumClass).filter(enumValueMatches(name, String::equalsIgnoreCase))
            )
            .findFirst()
            .orElseThrow(
               () -> new UnableToProduceResultException(String.format("no %s value could be matched to the name %s", enumClass.getSimpleName(), name))
            );
      }

      private static Predicate<Enum<?>> enumValueMatches(String name, BiPredicate<String, String> comparer) {
         return e -> Optional.ofNullable(Unchecked.function(e.getDeclaringClass()::getField).apply(e.name()).getAnnotation(DatabaseValue.class))
            .map(DatabaseValue::value)
            .map(dbName -> comparer.test(dbName, name))
            .orElseGet(() -> comparer.test(e.name(), name));
      }

      private static Stream<Enum<?>> enumConstants(Class<? extends Enum<?>> enumClass) {
         return Arrays.stream((Enum<?>[])enumClass.getEnumConstants());
      }
   }

   static class EnumByOrdinalColumnMapper<E extends Enum<E>> implements ColumnMapper<E> {
      private final Class<E> enumClass;
      private final E[] enumConstants;

      private EnumByOrdinalColumnMapper(Class<E> enumClass) {
         this.enumClass = enumClass;
         this.enumConstants = enumClass.getEnumConstants();
      }

      public E map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
         int ordinal = rs.getInt(columnNumber);

         try {
            return rs.wasNull() ? null : this.enumConstants[ordinal];
         } catch (ArrayIndexOutOfBoundsException var6) {
            throw new UnableToProduceResultException(
               String.format("no %s value could be matched to the ordinal %s", this.enumClass.getSimpleName(), ordinal), var6, ctx
            );
         }
      }
   }
}
