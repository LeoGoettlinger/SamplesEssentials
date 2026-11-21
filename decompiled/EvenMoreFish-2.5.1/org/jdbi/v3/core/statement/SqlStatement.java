package org.jdbi.v3.core.statement;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.BeanPropertyArguments;
import org.jdbi.v3.core.argument.CharacterStreamArgument;
import org.jdbi.v3.core.argument.InputStreamArgument;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.argument.ObjectArgument;
import org.jdbi.v3.core.argument.ObjectFieldArguments;
import org.jdbi.v3.core.argument.ObjectMethodArguments;
import org.jdbi.v3.core.argument.internal.NamedArgumentFinderFactory;
import org.jdbi.v3.core.argument.internal.PojoPropertyArguments;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.internal.IterableLike;
import org.jdbi.v3.core.mapper.Mappers;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.qualifier.NVarchar;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.statement.internal.JfrSupport;
import org.jdbi.v3.core.statement.internal.OptionalEvent;
import org.jdbi.v3.meta.Beta;

public abstract class SqlStatement<This extends SqlStatement<This>> extends BaseStatement<This> {
   private final String sql;
   PreparedStatement stmt;

   SqlStatement(Handle handle, CharSequence sql) {
      super(handle);
      this.sql = sql.toString();
      this.getContext().setConnection(handle.getConnection()).setRawSql(this.sql);
   }

   protected Binding getBinding() {
      return this.getContext().getBinding();
   }

   protected String getSql() {
      return this.sql;
   }

   public This setQueryTimeout(final int seconds) {
      return this.addCustomizer(StatementCustomizers.statementTimeout(seconds));
   }

   public This cleanupHandleCommit() {
      return this.cleanupHandle(Handle::commit);
   }

   public This cleanupHandleRollback() {
      return this.cleanupHandle(Handle::rollback);
   }

   private This cleanupHandle(Consumer<Handle> action) {
      this.getContext().addCleanable(() -> {
         Handle handle = this.getHandle();
         if (handle != null) {
            if (handle.isInTransaction()) {
               action.accept(handle);
            }

            handle.close();
         }
      });
      return this.typedThis;
   }

   public This bind(int position, Argument argument) {
      this.getBinding().addPositional(position, argument);
      return (This)this;
   }

   public This bind(String name, Argument argument) {
      this.getBinding().addNamed(name, argument);
      return this.typedThis;
   }

   public This bindBean(Object bean) {
      return this.bindBean(null, bean);
   }

   public This bindBean(String prefix, Object bean) {
      return bean != null
         ? this.bindNamedArgumentFinder(
            NamedArgumentFinderFactory.BEAN, prefix, bean, bean.getClass(), () -> new BeanPropertyArguments(prefix, bean, this.getConfig())
         )
         : this.typedThis;
   }

   @Beta
   public This bindPojo(Object pojo) {
      return this.bindPojo(null, pojo);
   }

   @Beta
   public This bindPojo(String prefix, Object pojo) {
      return this.bindPojo(prefix, pojo, pojo.getClass());
   }

   @Beta
   public This bindPojo(Object pojo, Type type) {
      return this.bindPojo(null, pojo, type);
   }

   @Beta
   public This bindPojo(String prefix, Object pojo, Type type) {
      return pojo != null
         ? this.bindNamedArgumentFinder(
            NamedArgumentFinderFactory.POJO, prefix, pojo, type, () -> new PojoPropertyArguments(prefix, pojo, type, this.getConfig())
         )
         : this.typedThis;
   }

   @Beta
   public This bindPojo(Object pojo, GenericType<?> type) {
      return this.bindPojo(null, pojo, type.getType());
   }

   @Beta
   public This bindPojo(String prefix, Object pojo, GenericType<?> type) {
      return this.bindPojo(prefix, pojo, type.getType());
   }

   public This bindFields(Object object) {
      return this.bindFields(null, object);
   }

   public This bindFields(String prefix, Object object) {
      return object != null
         ? this.bindNamedArgumentFinder(NamedArgumentFinderFactory.FIELDS, prefix, object, object.getClass(), () -> new ObjectFieldArguments(prefix, object))
         : this.typedThis;
   }

   public This bindMethods(Object object) {
      return this.bindMethods(null, object);
   }

   public This bindMethods(String prefix, Object object) {
      return object != null
         ? this.bindNamedArgumentFinder(NamedArgumentFinderFactory.METHODS, prefix, object, object.getClass(), () -> new ObjectMethodArguments(prefix, object))
         : this.typedThis;
   }

   public This bindMap(Map<String, ?> map) {
      if (map != null) {
         map.forEach(this::bind);
      }

      return this.typedThis;
   }

   public This bindNamedArgumentFinder(final NamedArgumentFinder namedArgumentFinder) {
      if (namedArgumentFinder != null) {
         this.getBinding().addNamedArgumentFinder(namedArgumentFinder);
      }

      return this.typedThis;
   }

   This bindNamedArgumentFinder(NamedArgumentFinderFactory factory, String prefix, Object value, Type type, Supplier<NamedArgumentFinder> namedArgumentFinder) {
      return this.bindNamedArgumentFinder(namedArgumentFinder.get());
   }

   public final This bind(int position, Character value) {
      return this.bind(position, Character.class, value);
   }

   public final This bind(String name, Character value) {
      return this.bind(name, Character.class, value);
   }

   public final This bind(int position, String value) {
      return this.bind(position, String.class, value);
   }

   public final This bind(String name, String value) {
      return this.bind(name, String.class, value);
   }

   public final This bindNVarchar(int position, String value) {
      return this.bindByType(position, value, QualifiedType.of(String.class).with(NVarchar.class));
   }

   public final This bindNVarchar(String name, String value) {
      return this.bindByType(name, value, QualifiedType.of(String.class).with(NVarchar.class));
   }

   public final This bind(int position, int value) {
      return this.bind(position, int.class, Integer.valueOf(value));
   }

   public final This bind(int position, Integer value) {
      return this.bind(position, Integer.class, value);
   }

   public final This bind(String name, int value) {
      return this.bind(name, int.class, Integer.valueOf(value));
   }

   public final This bind(String name, Integer value) {
      return this.bind(name, Integer.class, value);
   }

   public final This bind(int position, char value) {
      return this.bind(position, char.class, Character.valueOf(value));
   }

   public final This bind(String name, char value) {
      return this.bind(name, char.class, Character.valueOf(value));
   }

   public final This bindASCIIStream(int position, InputStream value, int length) {
      return this.bind(position, (Argument)(new InputStreamArgument(value, length, true)));
   }

   public final This bindASCIIStream(String name, InputStream value, int length) {
      return this.bind(name, (Argument)(new InputStreamArgument(value, length, true)));
   }

   public final This bind(int position, BigDecimal value) {
      return this.bind(position, BigDecimal.class, value);
   }

   public final This bind(String name, BigDecimal value) {
      return this.bind(name, BigDecimal.class, value);
   }

   public final This bindBinaryStream(int position, InputStream value, int length) {
      return this.bind(position, (Argument)(new InputStreamArgument(value, length, false)));
   }

   public final This bindBinaryStream(String name, InputStream value, int length) {
      return this.bind(name, (Argument)(new InputStreamArgument(value, length, false)));
   }

   public final This bind(int position, Blob value) {
      return this.bind(position, Blob.class, value);
   }

   public final This bind(String name, Blob value) {
      return this.bind(name, Blob.class, value);
   }

   public final This bind(int position, boolean value) {
      return this.bind(position, boolean.class, value);
   }

   public final This bind(int position, Boolean value) {
      return this.bind(position, Boolean.class, value);
   }

   private This bind(int position, Class<?> type, Object value) {
      return this.bindByType(position, value, type);
   }

   private This bind(String name, Class<?> type, Object value) {
      return this.bindByType(name, value, type);
   }

   public final This bind(String name, boolean value) {
      return this.bindByType(name, value, boolean.class);
   }

   public final This bind(String name, Boolean value) {
      return this.bindByType(name, value, Boolean.class);
   }

   public final This bind(int position, byte value) {
      return this.bindByType(position, value, byte.class);
   }

   public final This bind(int position, Byte value) {
      return this.bindByType(position, value, Byte.class);
   }

   public final This bind(String name, byte value) {
      return this.bindByType(name, value, byte.class);
   }

   public final This bind(String name, Byte value) {
      return this.bindByType(name, value, Byte.class);
   }

   public final This bind(int position, byte[] value) {
      return this.bindByType(position, value, byte[].class);
   }

   public final This bind(String name, byte[] value) {
      return this.bindByType(name, value, byte[].class);
   }

   public final This bind(int position, Reader value, int length) {
      return this.bind(position, (Argument)(new CharacterStreamArgument(value, length)));
   }

   public final This bind(String name, Reader value, int length) {
      return this.bind(name, (Argument)(new CharacterStreamArgument(value, length)));
   }

   public final This bind(int position, Clob value) {
      return this.bindByType(position, value, Clob.class);
   }

   public final This bind(String name, Clob value) {
      return this.bindByType(name, value, Clob.class);
   }

   public final This bind(int position, Date value) {
      return this.bindByType(position, value, Date.class);
   }

   public final This bind(String name, Date value) {
      return this.bindByType(name, value, Date.class);
   }

   public final This bind(int position, java.util.Date value) {
      return this.bindByType(position, value, java.util.Date.class);
   }

   public final This bind(String name, java.util.Date value) {
      return this.bindByType(name, value, java.util.Date.class);
   }

   public final This bind(int position, double value) {
      return this.bindByType(position, value, double.class);
   }

   public final This bind(int position, Double value) {
      return this.bindByType(position, value, Double.class);
   }

   public final This bind(String name, double value) {
      return this.bindByType(name, value, double.class);
   }

   public final This bind(String name, Double value) {
      return this.bindByType(name, value, Double.class);
   }

   public final This bind(int position, float value) {
      return this.bindByType(position, value, float.class);
   }

   public final This bind(int position, Float value) {
      return this.bindByType(position, value, Float.class);
   }

   public final This bind(String name, float value) {
      return this.bindByType(name, value, float.class);
   }

   public final This bind(String name, Float value) {
      return this.bindByType(name, value, Float.class);
   }

   public final This bind(int position, long value) {
      return this.bindByType(position, value, long.class);
   }

   public final This bind(int position, Long value) {
      return this.bindByType(position, value, Long.class);
   }

   public final This bind(String name, long value) {
      return this.bindByType(name, value, long.class);
   }

   public final This bind(String name, Long value) {
      return this.bindByType(name, value, Long.class);
   }

   public final This bind(int position, Short value) {
      return this.bindByType(position, value, Short.class);
   }

   public final This bind(int position, short value) {
      return this.bindByType(position, value, short.class);
   }

   public final This bind(String name, short value) {
      return this.bindByType(name, value, short.class);
   }

   public final This bind(String name, Short value) {
      return this.bindByType(name, value, Short.class);
   }

   public final This bind(int position, Object value) {
      this.getBinding().addPositional(position, value);
      return this.typedThis;
   }

   public final This bind(String name, Object value) {
      this.getBinding().addNamed(name, value);
      return this.typedThis;
   }

   public final This bind(int position, Time value) {
      return this.bindByType(position, value, Time.class);
   }

   public final This bind(String name, Time value) {
      return this.bindByType(name, value, Time.class);
   }

   public final This bind(int position, Timestamp value) {
      return this.bindByType(position, value, Timestamp.class);
   }

   public final This bind(String name, Timestamp value) {
      return this.bindByType(name, value, Timestamp.class);
   }

   public final This bind(int position, URL value) {
      return this.bindByType(position, value, URL.class);
   }

   public final This bind(String name, URL value) {
      return this.bindByType(name, value, URL.class);
   }

   public final This bind(int position, URI value) {
      return this.bindByType(position, value, URI.class);
   }

   public final This bind(String name, URI value) {
      return this.bindByType(name, value, URI.class);
   }

   public final This bind(int position, UUID value) {
      return this.bindByType(position, value, UUID.class);
   }

   public final This bind(String name, UUID value) {
      return this.bindByType(name, value, UUID.class);
   }

   public final This bindByType(int position, Object value, Type argumentType) {
      return this.bindByType(position, value, QualifiedType.of(argumentType));
   }

   public final This bindByType(int position, Object value, GenericType<?> argumentType) {
      return this.bindByType(position, value, argumentType.getType());
   }

   public final This bindByType(int position, Object value, QualifiedType<?> argumentType) {
      this.getBinding().addPositional(position, value, argumentType);
      return this.typedThis;
   }

   public final This bindByType(String name, Object value, Type argumentType) {
      return this.bindByType(name, value, QualifiedType.of(argumentType));
   }

   public final This bindByType(String name, Object value, GenericType<?> argumentType) {
      return this.bindByType(name, value, argumentType.getType());
   }

   public final This bindByType(String name, Object value, QualifiedType<?> argumentType) {
      this.getBinding().addNamed(name, value, argumentType);
      return this.typedThis;
   }

   @SafeVarargs
   public final <T> This bindArray(String name, T... array) {
      return this.bindArray(name, array.getClass().getComponentType(), array);
   }

   @SafeVarargs
   public final <T> This bindArray(int pos, T... array) {
      return this.bindArray(pos, array.getClass().getComponentType(), array);
   }

   public final This bindArray(String name, Type elementType, Object... array) {
      return this.bindByType(name, array, GenericTypes.arrayType(elementType));
   }

   public final This bindArray(int pos, Type elementType, Object... array) {
      return this.bindByType(pos, array, GenericTypes.arrayType(elementType));
   }

   public final This bindArray(String name, Type elementType, Iterable<?> iterable) {
      return this.bindByType(name, iterable, GenericTypes.parameterizeClass(Iterable.class, elementType));
   }

   public final This bindArray(int pos, Type elementType, Iterable<?> iterable) {
      return this.bindByType(pos, iterable, GenericTypes.parameterizeClass(Iterable.class, elementType));
   }

   public final This bindArray(String name, Type elementType, Iterator<?> iterator) {
      return this.bindByType(name, iterator, GenericTypes.parameterizeClass(Iterator.class, elementType));
   }

   public final This bindArray(int pos, Type elementType, Iterator<?> iterator) {
      return this.bindByType(pos, iterator, GenericTypes.parameterizeClass(Iterator.class, elementType));
   }

   public final This bindNull(String name, int sqlType) {
      return this.bind(name, (Argument)(new NullArgument(sqlType)));
   }

   public final This bindNull(int position, int sqlType) {
      return this.bind(position, (Argument)(new NullArgument(sqlType)));
   }

   public final This bindBySqlType(String name, Object value, int sqlType) {
      return this.bind(name, ObjectArgument.of(value, sqlType));
   }

   public final This bindBySqlType(int position, Object value, int sqlType) {
      return this.bind(position, ObjectArgument.of(value, sqlType));
   }

   public final This bindList(String key, Object... values) {
      return this.bindList(EmptyHandling.THROW, key, values);
   }

   public final This bindList(BiConsumer<SqlStatement, String> onEmpty, String key, Object... values) {
      return this.bindList(onEmpty, key, values == null ? null : Arrays.asList(values));
   }

   public final This bindList(String key, Iterable<?> values) {
      return this.bindList(EmptyHandling.THROW, key, values);
   }

   public final This bindList(BiConsumer<SqlStatement, String> onEmpty, String key, Iterable<?> values) {
      return this.bindList(onEmpty, key, values == null ? null : IterableLike.toList(values));
   }

   public final This bindList(String key, Iterator<?> values) {
      return this.bindList(EmptyHandling.THROW, key, values);
   }

   public final This bindList(BiConsumer<SqlStatement, String> onEmpty, String key, Iterator<?> values) {
      return this.bindList(onEmpty, key, values == null ? null : IterableLike.toList(values));
   }

   public final This bindList(BiConsumer<SqlStatement, String> onEmpty, String key, List<?> values) {
      if (values != null && !values.isEmpty()) {
         SqlStatements sqlStatements = this.getConfig(SqlStatements.class);
         BindListStyle style = sqlStatements.getBindListStyle();
         StringBuilder names = new StringBuilder();

         for (int i = 0; i < values.size(); i++) {
            String name = "__" + key + "_" + i;
            if (i > 0) {
               names.append(',');
            }

            style.appendElement(names, sqlStatements.getSqlParser().nameParameter(name, this.getContext()));
            this.bind(name, values.get(i));
         }

         return this.define(key, names.toString());
      } else {
         onEmpty.accept(this, key);
         return this.typedThis;
      }
   }

   public final This bindBeanList(String key, List<?> values, List<String> propertyNames) {
      if (values.isEmpty()) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".bindBeanList was called with no values.");
      } else if (propertyNames.isEmpty()) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".bindBeanList was called with no properties.");
      } else {
         StringBuilder names = new StringBuilder();
         StatementContext ctx = this.getContext();

         for (int valueIndex = 0; valueIndex < values.size(); valueIndex++) {
            if (valueIndex > 0) {
               names.append(',');
            }

            Object bean = values.get(valueIndex);
            BeanPropertyArguments beanProperties = new BeanPropertyArguments(null, bean, this.getConfig());
            names.append('(');

            for (int propertyIndex = 0; propertyIndex < propertyNames.size(); propertyIndex++) {
               if (propertyIndex > 0) {
                  names.append(',');
               }

               String propertyName = propertyNames.get(propertyIndex);
               String name = "__" + key + "_" + valueIndex + "_" + propertyName;
               names.append(':').append(name);
               Argument argument = beanProperties.find(propertyName, ctx)
                  .orElseThrow(() -> new UnableToCreateStatementException("Unable to get " + propertyName + " argument for " + bean, ctx));
               this.bind(name, argument);
            }

            names.append(')');
         }

         return this.define(key, names.toString());
      }
   }

   public final This bindMethodsList(String key, Iterable<?> values, List<String> methodNames) {
      Iterator<?> valueIter = values.iterator();
      if (!valueIter.hasNext()) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".bindMethodsList was called with no values.");
      } else if (methodNames.isEmpty()) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".bindMethodsList was called with no values.");
      } else {
         StringBuilder names = new StringBuilder();
         StatementContext ctx = this.getContext();

         for (int valueIndex = 0; valueIter.hasNext(); valueIndex++) {
            if (valueIndex > 0) {
               names.append(',');
            }

            Object bean = valueIter.next();
            ObjectMethodArguments beanMethods = new ObjectMethodArguments(null, bean);
            names.append('(');

            for (int methodIndex = 0; methodIndex < methodNames.size(); methodIndex++) {
               if (methodIndex > 0) {
                  names.append(',');
               }

               String methodName = methodNames.get(methodIndex);
               String name = key + valueIndex + "_" + methodName;
               names.append(':').append(name);
               Argument argument = beanMethods.find(methodName, ctx)
                  .orElseThrow(() -> new UnableToCreateStatementException("Unable to get " + methodName + " argument for " + bean, ctx));
               this.bind(name, argument);
            }

            names.append(')');
         }

         return this.define(key, names.toString());
      }
   }

   public final This defineList(String key, Object... values) {
      if (values.length == 0) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".defineList was called with no vararg values.");
      } else if (Stream.of(values).anyMatch(Objects::isNull)) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".defineList was called with a vararg array containing null values.");
      } else {
         return this.defineList(key, Arrays.asList(values));
      }
   }

   public final This defineList(String key, List<?> values) {
      if (values.isEmpty()) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".defineList was called with an empty list.");
      } else if (values.stream().anyMatch(Objects::isNull)) {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + ".defineList was called with a list containing null values.");
      } else {
         String value = values.stream().map(Object::toString).collect(Collectors.joining(", "));
         return this.define(key, value);
      }
   }

   @Beta
   public This defineNamedBindings() {
      return this.addCustomizer(new DefineNamedBindingsStatementCustomizer());
   }

   @Override
   public String toString() {
      return String.format("%s[sql=%s, bindings=%s]", this.getClass().getSimpleName(), this.sql, this.getContext().getBinding());
   }

   PreparedStatement internalExecute() {
      StatementContext ctx = this.getContext();
      OptionalEvent evt = JfrSupport.newStatementEvent();
      evt.begin();
      this.beforeTemplating();
      ParsedSql parsedSql = this.parseSql();
      SqlStatements stmtConfig = this.getConfig(SqlStatements.class);

      try {
         try {
            this.stmt = this.createStatement(parsedSql.getSql());
            this.getContext().addCleanable(() -> this.cleanupStatement(this.stmt));
            stmtConfig.customize(this.stmt);
         } catch (SQLException var9) {
            throw new UnableToCreateStatementException(var9, ctx);
         }

         ctx.setStatement(this.stmt);
         this.beforeBinding();
         new ArgumentBinder(this.stmt, ctx, parsedSql.getParameters()).bind(this.getBinding());
         this.beforeExecution();
         this.attachJfrEvent(evt, ctx);

         try {
            SqlLoggerUtil.wrap(this.stmt::execute, ctx, stmtConfig.getSqlLogger());
         } catch (SQLException var8) {
            throw stmtConfig.handleException(var8, ctx);
         }

         this.afterExecution();
         return this.stmt;
      } catch (Exception var10) {
         try {
            this.close();
         } catch (Exception var7) {
            var10.addSuppressed(var7);
         }

         throw var10;
      }
   }

   PreparedStatement createStatement(final String parsedSql) throws SQLException {
      return this.getHandle().getStatementBuilder().create(this.getHandle().getConnection(), parsedSql, this.getContext());
   }

   void cleanupStatement(final PreparedStatement statement) throws SQLException {
      this.getHandle().getStatementBuilder().close(this.getHandle().getConnection(), this.sql, statement);
   }

   ParsedSql parseSql() {
      StatementContext ctx = this.getContext();
      SqlStatements statements = this.getConfig(SqlStatements.class);
      String renderedSql = statements.preparedRender(this.sql, ctx);
      ctx.setRenderedSql(renderedSql);
      ParsedSql parsedSql = statements.getSqlParser().parse(renderedSql, ctx);
      ctx.setParsedSql(parsedSql);
      return parsedSql;
   }

   <T> RowMapper<T> mapperForType(Class<T> type) {
      return (RowMapper<T>)this.mapperForType(type);
   }

   <T> RowMapper<T> mapperForType(GenericType<T> type) {
      return (RowMapper<T>)this.mapperForType(type.getType());
   }

   RowMapper<?> mapperForType(Type type) {
      return this.getConfig(Mappers.class).findFor(type).orElseThrow(() -> new UnsupportedOperationException("No mapper registered for " + type));
   }

   void beforeTemplating() {
      this.callCustomizers(c -> c.beforeTemplating(this.stmt, this.getContext()));
   }

   void beforeBinding() {
      this.callCustomizers(c -> c.beforeBinding(this.stmt, this.getContext()));
   }

   void beforeExecution() {
      this.callCustomizers(c -> c.beforeExecution(this.stmt, this.getContext()));
   }

   void afterExecution() {
      this.callCustomizers(c -> c.afterExecution(this.stmt, this.getContext()));
   }

   private void attachJfrEvent(OptionalEvent statementEvent, StatementContext ctx) {
      if (statementEvent.shouldCommit()) {
         (new Object() {
            void attach() {
               JdbiStatementEvent evt = (JdbiStatementEvent)statementEvent;
               evt.traceId = ctx.getTraceId();
               evt.type = ctx.describeJdbiStatementType();
               SqlStatements stmtConfig = SqlStatement.this.getConfig(SqlStatements.class);
               String renderedSql = ctx.getRenderedSql();
               if (renderedSql != null) {
                  evt.sql = renderedSql.substring(0, Math.min(renderedSql.length(), stmtConfig.getJfrSqlMaxLength()));
               }

               evt.parameters = SqlStatement.this.getBinding().describe(stmtConfig.getJfrParamMaxLength());
               ctx.addCleanable(() -> {
                  evt.rowsMapped = ctx.getMappedRows();
                  evt.commit();
               });
            }
         }).attach();
      }
   }
}
