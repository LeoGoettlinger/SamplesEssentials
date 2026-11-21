package org.jdbi.v3.core.config;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.QualifiedArgumentFactory;
import org.jdbi.v3.core.array.SqlArrayArgumentStrategy;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.array.SqlArrayTypeFactory;
import org.jdbi.v3.core.array.SqlArrayTypes;
import org.jdbi.v3.core.codec.CodecFactory;
import org.jdbi.v3.core.collector.CollectorFactory;
import org.jdbi.v3.core.collector.JdbiCollectors;
import org.jdbi.v3.core.extension.ExtensionFactory;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.MapEntryMappers;
import org.jdbi.v3.core.mapper.QualifiedColumnMapperFactory;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.SqlParser;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.core.statement.StatementCustomizer;
import org.jdbi.v3.core.statement.TemplateEngine;
import org.jdbi.v3.core.statement.TimingCollector;
import org.jdbi.v3.meta.Beta;

public interface Configurable<This> {
   ConfigRegistry getConfig();

   default <C extends JdbiConfig<C>> C getConfig(Class<C> configClass) {
      return this.getConfig().get(configClass);
   }

   default <C extends JdbiConfig<C>> This configure(Class<C> configClass, Consumer<C> configurer) {
      configurer.accept(this.getConfig(configClass));
      return (This)this;
   }

   default This setTemplateEngine(TemplateEngine templateEngine) {
      return this.configure(SqlStatements.class, c -> c.setTemplateEngine(templateEngine));
   }

   default This setSqlParser(SqlParser parser) {
      return this.configure(SqlStatements.class, c -> c.setSqlParser(parser));
   }

   @Deprecated(
      since = "3.2.0",
      forRemoval = true
   )
   default This setTimingCollector(TimingCollector collector) {
      return this.configure(SqlStatements.class, c -> c.setTimingCollector(collector));
   }

   default This setSqlLogger(SqlLogger sqlLogger) {
      return this.configure(SqlStatements.class, c -> c.setSqlLogger(sqlLogger));
   }

   default This addCustomizer(StatementCustomizer customizer) {
      return this.configure(SqlStatements.class, c -> c.addCustomizer(customizer));
   }

   default This define(String key, Object value) {
      return this.configure(SqlStatements.class, c -> c.define(key, value));
   }

   default This registerArgument(ArgumentFactory factory) {
      return this.configure(Arguments.class, c -> c.register(factory));
   }

   default This registerArgument(QualifiedArgumentFactory factory) {
      return this.configure(Arguments.class, c -> c.register(factory));
   }

   default This setSqlArrayArgumentStrategy(SqlArrayArgumentStrategy strategy) {
      return this.configure(SqlArrayTypes.class, c -> c.setArgumentStrategy(strategy));
   }

   default This setMapKeyColumn(String keyColumn) {
      return this.configure(MapEntryMappers.class, c -> c.setKeyColumn(keyColumn));
   }

   default This setMapValueColumn(String valueColumn) {
      return this.configure(MapEntryMappers.class, c -> c.setValueColumn(valueColumn));
   }

   default This registerArrayType(Class<?> elementType, String sqlTypeName) {
      return this.configure(SqlArrayTypes.class, c -> c.register(elementType, sqlTypeName));
   }

   default <T> This registerArrayType(Class<T> elementType, String sqlTypeName, Function<T, ?> conversion) {
      return this.registerArrayType(SqlArrayTypeFactory.of(elementType, sqlTypeName, conversion));
   }

   default This registerArrayType(SqlArrayType<?> arrayType) {
      return this.configure(SqlArrayTypes.class, c -> c.register(arrayType));
   }

   default This registerArrayType(SqlArrayTypeFactory factory) {
      return this.configure(SqlArrayTypes.class, c -> c.register(factory));
   }

   default This registerCollector(Type collectionType, Collector<?, ?, ?> collector) {
      return this.configure(JdbiCollectors.class, c -> c.registerCollector(collectionType, collector));
   }

   default This registerCollector(CollectorFactory factory) {
      return this.configure(JdbiCollectors.class, c -> c.register(factory));
   }

   default This registerColumnMapper(ColumnMapper<?> mapper) {
      return this.configure(ColumnMappers.class, c -> c.register(mapper));
   }

   default <T> This registerColumnMapper(GenericType<T> type, ColumnMapper<T> mapper) {
      return this.configure(ColumnMappers.class, c -> c.register(type, mapper));
   }

   default This registerColumnMapper(Type type, ColumnMapper<?> mapper) {
      return this.configure(ColumnMappers.class, c -> c.register(type, mapper));
   }

   default <T> This registerColumnMapper(QualifiedType<T> type, ColumnMapper<T> mapper) {
      return this.configure(ColumnMappers.class, c -> c.register(type, mapper));
   }

   default This registerColumnMapper(ColumnMapperFactory factory) {
      return this.configure(ColumnMappers.class, c -> c.register(factory));
   }

   default This registerColumnMapper(QualifiedColumnMapperFactory factory) {
      return this.configure(ColumnMappers.class, c -> c.register(factory));
   }

   default This registerExtension(ExtensionFactory factory) {
      return this.configure(Extensions.class, c -> c.register(factory));
   }

   default This registerRowMapper(RowMapper<?> mapper) {
      return this.configure(RowMappers.class, c -> c.register(mapper));
   }

   default <T> This registerRowMapper(GenericType<T> type, RowMapper<T> mapper) {
      return this.configure(RowMappers.class, c -> c.register(type, mapper));
   }

   default This registerRowMapper(Type type, RowMapper<?> mapper) {
      return this.configure(RowMappers.class, c -> c.register(type, mapper));
   }

   default This registerRowMapper(RowMapperFactory factory) {
      return this.configure(RowMappers.class, c -> c.register(factory));
   }

   @Beta
   default This registerCodecFactory(CodecFactory codecFactory) {
      this.registerColumnMapper(codecFactory);
      return this.registerArgument(codecFactory);
   }
}
