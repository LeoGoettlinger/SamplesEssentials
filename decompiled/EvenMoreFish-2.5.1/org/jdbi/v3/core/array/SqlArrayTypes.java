package org.jdbi.v3.core.array;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.enums.internal.EnumSqlArrayTypeFactory;
import org.jdbi.v3.core.interceptor.JdbiInterceptionChainHolder;
import org.jdbi.v3.meta.Alpha;

public class SqlArrayTypes implements JdbiConfig<SqlArrayTypes> {
   private final JdbiInterceptionChainHolder<SqlArrayType<?>, SqlArrayTypeFactory> inferenceInterceptors;
   private final List<SqlArrayTypeFactory> factories;
   private SqlArrayArgumentStrategy argumentStrategy;
   private ConfigRegistry registry;

   public SqlArrayTypes() {
      this.inferenceInterceptors = new JdbiInterceptionChainHolder<>(InferredSqlArrayTypeFactory::new);
      this.factories = new CopyOnWriteArrayList<>();
      this.argumentStrategy = SqlArrayArgumentStrategy.SQL_ARRAY;
      this.register(boolean.class, "boolean");
      this.register(Boolean.class, "boolean");
      this.register(short.class, "smallint");
      this.register(Short.class, "smallint");
      this.register(int.class, "integer");
      this.register(Integer.class, "integer");
      this.register(long.class, "bigint");
      this.register(Long.class, "bigint");
      this.register(float.class, "float4");
      this.register(Float.class, "float4");
      this.register(double.class, "float8");
      this.register(Double.class, "float8");
      this.register(String.class, "varchar");
      this.register(UUID.class, "uuid");
      this.register(LocalDate.class, "date");
      this.register(LocalTime.class, "time");
      this.register(LocalDateTime.class, "timestamp");
      this.register(OffsetDateTime.class, "timestamp with time zone");
      this.register(OffsetTime.class, "time with time zone");
      this.register(SqlArrayTypeFactory.of(Instant.class, "timestamp with time zone", v -> v == null ? null : OffsetDateTime.ofInstant(v, ZoneOffset.UTC)));
      this.register(SqlArrayTypeFactory.of(ZonedDateTime.class, "timestamp with time zone", v -> v == null ? null : v.toOffsetDateTime()));
      this.register(new EnumSqlArrayTypeFactory());
   }

   private SqlArrayTypes(SqlArrayTypes that) {
      this.factories = new CopyOnWriteArrayList<>(that.factories);
      this.argumentStrategy = that.argumentStrategy;
      this.inferenceInterceptors = new JdbiInterceptionChainHolder<>(that.inferenceInterceptors);
   }

   public SqlArrayArgumentStrategy getArgumentStrategy() {
      return this.argumentStrategy;
   }

   public SqlArrayTypes setArgumentStrategy(SqlArrayArgumentStrategy argumentStrategy) {
      this.argumentStrategy = argumentStrategy;
      return this;
   }

   public SqlArrayTypes register(Class<?> elementType, String sqlTypeName) {
      return this.register(SqlArrayTypeFactory.of(elementType, sqlTypeName, Function.identity()));
   }

   public SqlArrayTypes register(SqlArrayType<?> arrayType) {
      SqlArrayTypeFactory factory = this.inferenceInterceptors.process(arrayType);
      return this.register(factory);
   }

   public SqlArrayTypes register(SqlArrayTypeFactory factory) {
      this.factories.add(0, factory);
      return this;
   }

   public Optional<SqlArrayType<?>> findFor(Type elementType) {
      return this.factories.stream().flatMap(factory -> factory.build(elementType, this.registry).stream()).findFirst();
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   @Alpha
   public JdbiInterceptionChainHolder<SqlArrayType<?>, SqlArrayTypeFactory> getInferenceInterceptors() {
      return this.inferenceInterceptors;
   }

   public SqlArrayTypes createCopy() {
      return new SqlArrayTypes(this);
   }
}
