package org.jdbi.v3.core.statement;

import jakarta.annotation.Nullable;
import java.io.Closeable;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import org.jdbi.v3.core.CloseException;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.array.SqlArrayArgumentStrategy;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.array.SqlArrayTypes;
import org.jdbi.v3.core.collector.JdbiCollectors;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.internal.exceptions.ThrowableSuppressor;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.Mappers;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.meta.Alpha;
import org.jdbi.v3.meta.Beta;

public class StatementContext implements Closeable {
   private final ConfigRegistry config;
   private final ExtensionMethod extensionMethod;
   private final Type jdbiStatementType;
   private final Set<Cleanable> cleanables = new LinkedHashSet<>();
   private String rawSql;
   private String renderedSql;
   private ParsedSql parsedSql;
   private PreparedStatement statement;
   private Connection connection;
   private Binding binding = new Binding(this);
   private volatile boolean returningGeneratedKeys = false;
   private String[] generatedKeysColumnNames = new String[0];
   private volatile boolean concurrentUpdatable = false;
   private Instant executionMoment;
   private Instant completionMoment;
   private Instant exceptionMoment;
   private volatile long mappedRows;
   private String traceId;

   static StatementContext create(final ConfigRegistry config, final ExtensionMethod extensionMethod, final Type jdbiStatementType) {
      StatementContext context = new StatementContext(config, extensionMethod, jdbiStatementType);
      context.notifyContextCreated();
      return context;
   }

   private StatementContext(final ConfigRegistry config, final ExtensionMethod extensionMethod, final Type jdbiStatementType) {
      this.config = Objects.requireNonNull(config);
      this.extensionMethod = extensionMethod;
      this.jdbiStatementType = jdbiStatementType;
   }

   @Beta
   public Type getJdbiStatementType() {
      return this.jdbiStatementType;
   }

   @Beta
   public String describeJdbiStatementType() {
      return this.jdbiStatementType instanceof Class<?> clazz ? clazz.getSimpleName() : this.jdbiStatementType.getTypeName();
   }

   public <C extends JdbiConfig<C>> C getConfig(Class<C> configClass) {
      return this.config.get(configClass);
   }

   public ConfigRegistry getConfig() {
      return this.config;
   }

   public Map<String, Object> getAttributes() {
      return this.getConfig(SqlStatements.class).getAttributes();
   }

   public Object getAttribute(String key) {
      return this.getConfig(SqlStatements.class).getAttribute(key);
   }

   public void define(String key, Object value) {
      this.getConfig(SqlStatements.class).define(key, value);
   }

   public Optional<Argument> findArgumentFor(Type type, Object value) {
      return this.getConfig(Arguments.class).findFor(type, value);
   }

   public Optional<Argument> findArgumentFor(QualifiedType<?> type, Object value) {
      return this.getConfig(Arguments.class).findFor(type, value);
   }

   public SqlArrayArgumentStrategy getSqlArrayArgumentStrategy() {
      return this.getConfig(SqlArrayTypes.class).getArgumentStrategy();
   }

   public Optional<SqlArrayType<?>> findSqlArrayTypeFor(Type elementType) {
      return this.getConfig(SqlArrayTypes.class).findFor(elementType);
   }

   public <T> Optional<RowMapper<T>> findMapperFor(Class<T> type) {
      return this.getConfig(Mappers.class).findFor(type);
   }

   public <T> Optional<RowMapper<T>> findMapperFor(GenericType<T> type) {
      return this.getConfig(Mappers.class).findFor(type);
   }

   public Optional<RowMapper<?>> findMapperFor(Type type) {
      return this.getConfig(Mappers.class).findFor(type);
   }

   public <T> Optional<RowMapper<T>> findMapperFor(QualifiedType<T> type) {
      return this.getConfig(Mappers.class).findFor(type);
   }

   public <T> Optional<ColumnMapper<T>> findColumnMapperFor(Class<T> type) {
      return this.getConfig(ColumnMappers.class).findFor(type);
   }

   public <T> Optional<ColumnMapper<T>> findColumnMapperFor(GenericType<T> type) {
      return this.getConfig(ColumnMappers.class).findFor(type);
   }

   public Optional<ColumnMapper<?>> findColumnMapperFor(Type type) {
      return this.getConfig(ColumnMappers.class).findFor(type);
   }

   public <T> Optional<ColumnMapper<T>> findColumnMapperFor(QualifiedType<T> type) {
      return this.getConfig(ColumnMappers.class).findFor(type);
   }

   public Optional<RowMapper<?>> findRowMapperFor(Type type) {
      return this.getConfig(RowMappers.class).findFor(type);
   }

   public <T> Optional<RowMapper<T>> findRowMapperFor(Class<T> type) {
      return this.getConfig(RowMappers.class).findFor(type);
   }

   public <T> Optional<RowMapper<T>> findRowMapperFor(GenericType<T> type) {
      return this.getConfig(RowMappers.class).findFor(type);
   }

   public Optional<Collector<?, ?, ?>> findCollectorFor(Type containerType) {
      return this.getConfig(JdbiCollectors.class).findFor(containerType);
   }

   public Optional<Type> findElementTypeFor(Type containerType) {
      return this.getConfig(JdbiCollectors.class).findElementTypeFor(containerType);
   }

   StatementContext setRawSql(String rawSql) {
      this.rawSql = rawSql;
      return this;
   }

   public String getRawSql() {
      return this.rawSql;
   }

   void setRenderedSql(String renderedSql) {
      this.renderedSql = renderedSql;
   }

   public String getRenderedSql() {
      return this.renderedSql;
   }

   void setParsedSql(ParsedSql parsedSql) {
      this.parsedSql = parsedSql;
   }

   public ParsedSql getParsedSql() {
      return this.parsedSql;
   }

   void setStatement(PreparedStatement stmt) {
      this.statement = stmt;
   }

   public PreparedStatement getStatement() {
      return this.statement;
   }

   StatementContext setConnection(Connection connection) {
      this.connection = connection;
      return this;
   }

   public Connection getConnection() {
      return this.connection;
   }

   StatementContext setBinding(Binding b) {
      this.binding = b;
      return this;
   }

   public Binding getBinding() {
      return this.binding;
   }

   public void setReturningGeneratedKeys(boolean returningGeneratedKeys) {
      if (this.isConcurrentUpdatable() && returningGeneratedKeys) {
         throw new IllegalArgumentException("Cannot create a result set that is concurrent updatable and is returning generated keys.");
      } else {
         this.returningGeneratedKeys = returningGeneratedKeys;
      }
   }

   public boolean isReturningGeneratedKeys() {
      return this.returningGeneratedKeys || this.generatedKeysColumnNames.length > 0;
   }

   public String[] getGeneratedKeysColumnNames() {
      return Arrays.copyOf(this.generatedKeysColumnNames, this.generatedKeysColumnNames.length);
   }

   public void setGeneratedKeysColumnNames(String[] generatedKeysColumnNames) {
      this.generatedKeysColumnNames = Arrays.copyOf(generatedKeysColumnNames, generatedKeysColumnNames.length);
   }

   public boolean isConcurrentUpdatable() {
      return this.concurrentUpdatable;
   }

   public void setConcurrentUpdatable(final boolean concurrentUpdatable) {
      if (concurrentUpdatable && this.isReturningGeneratedKeys()) {
         throw new IllegalArgumentException("Cannot create a result set that is concurrent updatable and is returning generated keys.");
      } else {
         this.concurrentUpdatable = concurrentUpdatable;
      }
   }

   @Nullable
   public Instant getExecutionMoment() {
      return this.executionMoment;
   }

   public void setExecutionMoment(Instant executionMoment) {
      this.executionMoment = executionMoment;
   }

   @Nullable
   public Instant getCompletionMoment() {
      return this.completionMoment;
   }

   public void setCompletionMoment(Instant completionMoment) {
      this.completionMoment = completionMoment;
   }

   @Nullable
   public Instant getExceptionMoment() {
      return this.exceptionMoment;
   }

   public void setExceptionMoment(Instant exceptionMoment) {
      this.exceptionMoment = exceptionMoment;
   }

   @Alpha
   public long getMappedRows() {
      return this.mappedRows;
   }

   @Alpha
   public void setMappedRows(final long mappedRows) {
      this.mappedRows = mappedRows;
   }

   @Alpha
   public void setTraceId(String traceId) {
      this.traceId = traceId;
   }

   @Alpha
   public String getTraceId() {
      return this.traceId;
   }

   public long getElapsedTime(ChronoUnit unit) {
      return unit.between(this.executionMoment, this.completionMoment == null ? this.exceptionMoment : this.completionMoment);
   }

   public void addCleanable(Cleanable cleanable) {
      synchronized (this.cleanables) {
         this.cleanables.add(cleanable);
      }

      this.notifyCleanableAdded(cleanable);
   }

   @Override
   public void close() {
      try {
         List<Cleanable> cleanablesCopy;
         synchronized (this.cleanables) {
            if (this.cleanables.isEmpty()) {
               return;
            }

            cleanablesCopy = new ArrayList<>(this.cleanables);
            this.cleanables.clear();
         }

         Collections.reverse(cleanablesCopy);
         cleanablesCopy.forEach(this::notifyCleanableRemoved);
         ThrowableSuppressor throwableSuppressor = new ThrowableSuppressor();

         for (Cleanable cleanable : cleanablesCopy) {
            throwableSuppressor.suppressAppend(cleanable::close);
         }

         throwableSuppressor.throwIfNecessary(t -> new CloseException("Exception thrown while cleaning StatementContext", t));
      } finally {
         this.notifyContextCleaned();
      }
   }

   public ExtensionMethod getExtensionMethod() {
      return this.extensionMethod;
   }

   boolean isClean() {
      synchronized (this.cleanables) {
         return this.cleanables.isEmpty();
      }
   }

   private Collection<StatementContextListener> getListeners() {
      return this.getConfig(SqlStatements.class).getContextListeners();
   }

   private void notifyContextCreated() {
      Collection<StatementContextListener> listeners = this.getListeners();
      listeners.forEach(customizer -> customizer.contextCreated(this));
   }

   private void notifyContextCleaned() {
      Collection<StatementContextListener> listeners = this.getListeners();
      listeners.forEach(customizer -> customizer.contextCleaned(this));
   }

   private void notifyCleanableRemoved(Cleanable cleanable) {
      Collection<StatementContextListener> listeners = this.getListeners();
      listeners.forEach(customizer -> customizer.cleanableRemoved(this, cleanable));
   }

   private void notifyCleanableAdded(Cleanable cleanable) {
      Collection<StatementContextListener> listeners = this.getListeners();
      listeners.forEach(customizer -> customizer.cleanableAdded(this, cleanable));
   }
}
