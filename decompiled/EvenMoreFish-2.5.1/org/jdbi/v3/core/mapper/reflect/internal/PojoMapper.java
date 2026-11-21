package org.jdbi.v3.core.mapper.reflect.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jdbi.v3.core.annotation.internal.JdbiAnnotations;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.PrefixedRowMapper;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.SingleColumnMapper;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.ColumnNameMatcher;
import org.jdbi.v3.core.mapper.reflect.ReflectionMapperUtil;
import org.jdbi.v3.core.mapper.reflect.ReflectionMappers;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import org.jdbi.v3.core.statement.StatementContext;

public class PojoMapper<T> implements PrefixedRowMapper<T> {
   protected boolean strictColumnTypeMapping = true;
   protected final Type type;
   protected final String prefix;
   private final Map<PojoProperties.PojoProperty<T>, PojoMapper<?>> nestedMappers = new ConcurrentHashMap<>();

   public PojoMapper(Type type, String prefix) {
      this.type = type;
      this.prefix = prefix;
   }

   @Override
   public T map(ResultSet rs, StatementContext ctx) throws SQLException {
      return this.specialize(rs, ctx).map(rs, ctx);
   }

   @Override
   public RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
      UnaryOperator<String> caseChange = ctx.getConfig(ReflectionMappers.class).getCaseChange();
      List<String> columnNames = ReflectionMapperUtil.getColumnNames(rs, caseChange);
      List<ColumnNameMatcher> columnNameMatchers = ctx.getConfig(ReflectionMappers.class).getColumnNameMatchers();
      List<String> unmatchedColumns = new ArrayList<>(columnNames);
      RowMapper<T> result = this.createSpecializedRowMapper(ctx, columnNames, columnNameMatchers, unmatchedColumns, Function.identity())
         .orElseThrow(
            () -> new IllegalArgumentException(
               String.format(
                  "Mapping bean %s didn't find any matching columns in result set.%s", this.type, UnmatchedColumnsHint.forColumns(this.prefix, columnNames)
               )
            )
         );
      if (ctx.getConfig(ReflectionMappers.class).isStrictMatching()
         && ReflectionMapperUtil.anyColumnsStartWithPrefix(unmatchedColumns, this.prefix, columnNameMatchers)) {
         throw new IllegalArgumentException(String.format("Mapping bean %s could not match properties for columns: %s", this.type, unmatchedColumns));
      } else {
         return result;
      }
   }

   private <R> Optional<RowMapper<R>> createSpecializedRowMapper(
      StatementContext ctx, List<String> columnNames, List<ColumnNameMatcher> columnNameMatchers, List<String> unmatchedColumns, Function<T, R> postProcessor
   ) {
      List<PojoMapper.PropertyData<T>> propList = new ArrayList<>();

      for (PojoProperties.PojoProperty<T> property : this.getProperties(ctx.getConfig()).getProperties().values()) {
         Nested nested = property.getAnnotation(Nested.class).orElse(null);
         if (JdbiAnnotations.isMapped(property)) {
            if (nested == null) {
               String paramName = ReflectionMapperUtil.addPropertyNamePrefix(this.prefix, this.getName(property));
               ReflectionMapperUtil.findColumnIndex(paramName, columnNames, columnNameMatchers, () -> this.debugName(property))
                  .ifPresent(
                     index -> {
                        ColumnMapper<?> mapper = ctx.findColumnMapperFor(property.getQualifiedType().mapType(GenericTypes::box))
                           .orElseGet(
                              () -> {
                                 if (this.strictColumnTypeMapping) {
                                    throw new NoSuchMapperException(
                                       String.format(
                                          "Couldn't find mapper for property '%s' of type '%s' from %s",
                                          property.getName(),
                                          property.getQualifiedType(),
                                          this.type
                                       )
                                    );
                                 } else {
                                    return ColumnMapper.getDefaultColumnMapper();
                                 }
                              }
                           );
                        propList.add(new PojoMapper.PropertyData<>(property, new SingleColumnMapper<>(mapper, index + 1)));
                        unmatchedColumns.remove(columnNames.get(index));
                     }
                  );
            } else {
               String nestedPrefix = ReflectionMapperUtil.addPropertyNamePrefix(this.prefix, nested.value());
               if (ReflectionMapperUtil.anyColumnsStartWithPrefix(columnNames, nestedPrefix, columnNameMatchers)) {
                  Type propertyType = property.getQualifiedType().getType();
                  Optional<? extends RowMapper<?>> nestedMapper;
                  if (propertyType instanceof ParameterizedType pt && pt.getRawType().equals(Optional.class)) {
                     Class<?> rawType = GenericTypes.findGenericParameter(propertyType, Optional.class)
                        .map(GenericTypes::getErasedType)
                        .orElseThrow(
                           () -> new IllegalArgumentException(String.format("Could not determine the type of Optional property %s", property.getName()))
                        );
                     nestedMapper = this.nestedMappers
                        .computeIfAbsent(property, d -> this.createNestedMapper(ctx, rawType, nestedPrefix))
                        .createSpecializedRowMapper(ctx, columnNames, columnNameMatchers, unmatchedColumns, Optional::ofNullable);
                  } else {
                     nestedMapper = this.nestedMappers
                        .computeIfAbsent(property, d -> this.createNestedMapper(ctx, GenericTypes.getErasedType(propertyType), nestedPrefix))
                        .createSpecializedRowMapper(ctx, columnNames, columnNameMatchers, unmatchedColumns, Function.identity());
                  }

                  nestedMapper.ifPresent(mapper -> propList.add(new PojoMapper.PropertyData<>(property, (RowMapper<?>)mapper)));
               }
            }
         }
      }

      if (propList.isEmpty() && !columnNames.isEmpty()) {
         return Optional.empty();
      } else {
         propList.sort(Comparator.comparing(p -> p.propagateNull ? 1 : 0));
         RowMapper<R> boundMapper = new PojoMapper.BoundPojoMapper<>(propList, postProcessor);
         OptionalInt propagateNullColumnIndex = this.locatePropagateNullColumnIndex(columnNames, columnNameMatchers);
         return propagateNullColumnIndex.isPresent()
            ? Optional.of(new NullDelegatingMapper<>(propagateNullColumnIndex.getAsInt() + 1, boundMapper, postProcessor.apply(null)))
            : Optional.of(boundMapper);
      }
   }

   private OptionalInt locatePropagateNullColumnIndex(List<String> columnNames, List<ColumnNameMatcher> columnNameMatchers) {
      Optional<String> propagateNullColumn = Optional.ofNullable(GenericTypes.getErasedType(this.type).getAnnotation(PropagateNull.class))
         .map(PropagateNull::value)
         .map(name -> ReflectionMapperUtil.addPropertyNamePrefix(this.prefix, name));
      return !propagateNullColumn.isPresent()
         ? OptionalInt.empty()
         : ReflectionMapperUtil.findColumnIndex(propagateNullColumn.get(), columnNames, columnNameMatchers, propagateNullColumn::get);
   }

   protected PojoProperties<T> getProperties(ConfigRegistry config) {
      return (PojoProperties<T>)config.get(PojoTypes.class)
         .findFor(this.type)
         .orElseThrow(() -> new UnableToProduceResultException("Couldn't find properties for " + this.type));
   }

   protected PojoMapper<?> createNestedMapper(StatementContext ctx, Class<?> rawType, String nestedPrefix) {
      return new PojoMapper(rawType, nestedPrefix);
   }

   @Override
   public String getPrefix() {
      return this.prefix;
   }

   private String getName(PojoProperties.PojoProperty<T> property) {
      return property.getAnnotation(ColumnName.class).map(ColumnName::value).orElseGet(property::getName);
   }

   private String debugName(PojoProperties.PojoProperty<T> p) {
      return String.format("%s.%s", this.type, p.getName());
   }

   class BoundPojoMapper<R> implements RowMapper<R> {
      private final List<PojoMapper.PropertyData<T>> propList;
      private final Function<T, R> postProcessor;

      BoundPojoMapper(List<PojoMapper.PropertyData<T>> propList, Function<T, R> postProcessor) {
         this.propList = propList;
         this.postProcessor = postProcessor;
      }

      @Override
      public R map(ResultSet rs, StatementContext ctx) throws SQLException {
         PojoProperties.PojoBuilder<T> pojo = PojoMapper.this.getProperties(ctx.getConfig()).create();

         for (PojoMapper.PropertyData<T> p : this.propList) {
            Object value = p.mapper.map(rs, ctx);
            boolean wasNull = value == null || p.isPrimitive && rs.wasNull();
            if (p.propagateNull && wasNull) {
               return this.postProcessor.apply(null);
            }

            if (value != null) {
               pojo.set(p.property, value);
            }
         }

         return this.postProcessor.apply(pojo.build());
      }

      @Override
      public String toString() {
         return new StringJoiner(", ", PojoMapper.BoundPojoMapper.class.getSimpleName() + "[", "]")
            .add("type=" + PojoMapper.this.type.getTypeName())
            .add("prefix=" + PojoMapper.this.prefix)
            .toString();
      }
   }

   private static class PropertyData<T> {
      final PojoProperties.PojoProperty<T> property;
      final RowMapper<?> mapper;
      final boolean propagateNull;
      final boolean isPrimitive;

      PropertyData(PojoProperties.PojoProperty<T> property, RowMapper<?> mapper) {
         this.property = property;
         this.mapper = mapper;
         this.propagateNull = checkPropagateNullAnnotation(property);
         this.isPrimitive = GenericTypes.getErasedType(property.getQualifiedType().getType()).isPrimitive();
      }

      private static boolean checkPropagateNullAnnotation(PojoProperties.PojoProperty<?> property) {
         Optional<String> propagateNullValue = property.getAnnotation(PropagateNull.class).map(PropagateNull::value);
         propagateNullValue.ifPresent(v -> {
            if (!v.isEmpty()) {
               throw new IllegalArgumentException(String.format("@PropagateNull does not support a value (%s) on a property (%s)", v, property.getName()));
            }
         });
         return propagateNullValue.isPresent();
      }
   }
}
