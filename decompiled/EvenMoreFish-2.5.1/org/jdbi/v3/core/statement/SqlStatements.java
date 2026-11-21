package org.jdbi.v3.core.statement;

import jakarta.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import org.jdbi.v3.core.cache.JdbiCache;
import org.jdbi.v3.core.cache.JdbiCacheBuilder;
import org.jdbi.v3.core.cache.JdbiCacheLoader;
import org.jdbi.v3.core.cache.internal.DefaultJdbiCacheBuilder;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.meta.Alpha;
import org.jdbi.v3.meta.Beta;

public final class SqlStatements implements JdbiConfig<SqlStatements> {
   public static final int SQL_TEMPLATE_CACHE_SIZE = 1000;
   private final Map<String, Object> attributes;
   private TemplateEngine templateEngine;
   private JdbiCache<SqlStatements.StatementCacheKey, Function<StatementContext, String>> templateCache;
   private SqlParser sqlParser;
   private SqlLogger sqlLogger;
   private Integer queryTimeout;
   private volatile boolean allowUnusedBindings;
   private volatile boolean attachAllStatementsForCleanup;
   private volatile boolean attachCallbackStatementsForCleanup = true;
   private volatile boolean scriptStatementsNeedSemicolon = true;
   private volatile BindListStyle bindListStyle = BindListStyle.PLAIN;
   private final Collection<StatementCustomizer> customizers;
   private final Deque<SqlExceptionHandler> exceptionHandlers;
   private final Collection<StatementContextListener> contextListeners;
   private volatile int jfrSqlMaxLength = 512;
   private volatile int jfrParamMaxLength = 512;
   private volatile boolean includeBindingsInTelemetry = true;

   public SqlStatements() {
      this.attributes = Collections.synchronizedMap(new HashMap<>());
      this.templateEngine = new DefinedAttributeTemplateEngine();
      this.sqlParser = new ColonPrefixSqlParser();
      this.sqlLogger = SqlLogger.NOP_SQL_LOGGER;
      this.queryTimeout = null;
      this.customizers = new CopyOnWriteArrayList<>();
      this.contextListeners = new CopyOnWriteArraySet<>();
      this.templateCache = DefaultJdbiCacheBuilder.builder().maxSize(1000).build();
      this.exceptionHandlers = new ArrayDeque<>(0);
   }

   private SqlStatements(SqlStatements that) {
      this.attributes = Collections.synchronizedMap(that.getAttributes());
      this.templateEngine = that.templateEngine;
      this.sqlParser = that.sqlParser;
      this.sqlLogger = that.sqlLogger;
      this.queryTimeout = that.queryTimeout;
      this.allowUnusedBindings = that.allowUnusedBindings;
      this.attachAllStatementsForCleanup = that.attachAllStatementsForCleanup;
      this.attachCallbackStatementsForCleanup = that.attachCallbackStatementsForCleanup;
      this.scriptStatementsNeedSemicolon = that.scriptStatementsNeedSemicolon;
      this.bindListStyle = that.bindListStyle;
      this.customizers = new CopyOnWriteArrayList<>(that.customizers);
      this.contextListeners = new CopyOnWriteArraySet<>(that.contextListeners);
      this.templateCache = that.templateCache;
      this.jfrSqlMaxLength = that.jfrSqlMaxLength;
      this.jfrParamMaxLength = that.jfrParamMaxLength;
      this.includeBindingsInTelemetry = that.includeBindingsInTelemetry;
      this.exceptionHandlers = new ArrayDeque<>(that.exceptionHandlers);
   }

   public SqlStatements define(String key, Object value) {
      this.attributes.put(key, value);
      return this;
   }

   public SqlStatements defineMap(final Map<String, ?> values) {
      if (values != null) {
         this.attributes.putAll((Map<? extends String, ? extends Object>)values);
      }

      return this;
   }

   public Object getAttribute(String key) {
      return this.attributes.get(key);
   }

   public Map<String, Object> getAttributes() {
      return new HashMap<>(this.attributes);
   }

   public SqlStatements addCustomizer(final StatementCustomizer customizer) {
      this.customizers.add(customizer);
      return this;
   }

   public SqlStatements addContextListener(final StatementContextListener listener) {
      this.contextListeners.add(listener);
      return this;
   }

   public TemplateEngine getTemplateEngine() {
      return this.templateEngine;
   }

   public SqlStatements setTemplateEngine(TemplateEngine templateEngine) {
      this.templateEngine = templateEngine;
      return this;
   }

   @Beta
   public SqlStatements setTemplateCache(JdbiCacheBuilder cacheBuilder) {
      this.templateCache = cacheBuilder.build();
      return this;
   }

   public SqlParser getSqlParser() {
      return this.sqlParser;
   }

   public SqlStatements setSqlParser(SqlParser sqlParser) {
      this.sqlParser = sqlParser;
      return this;
   }

   @Deprecated(
      since = "3.2.0",
      forRemoval = true
   )
   public TimingCollector getTimingCollector() {
      return (elapsed, ctx) -> this.sqlLogger.logAfterExecution(ctx);
   }

   @Deprecated(
      since = "3.2.0",
      forRemoval = true
   )
   public SqlStatements setTimingCollector(TimingCollector timingCollector) {
      this.sqlLogger = timingCollector == null ? SqlLogger.NOP_SQL_LOGGER : new SqlLogger() {
         @Override
         public void logAfterExecution(StatementContext context) {
            timingCollector.collect(context.getElapsedTime(ChronoUnit.NANOS), context);
         }
      };
      return this;
   }

   public SqlLogger getSqlLogger() {
      return this.sqlLogger;
   }

   public SqlStatements setSqlLogger(SqlLogger sqlLogger) {
      this.sqlLogger = sqlLogger == null ? SqlLogger.NOP_SQL_LOGGER : sqlLogger;
      return this;
   }

   @Beta
   public Integer getQueryTimeout() {
      return this.queryTimeout;
   }

   @Beta
   public SqlStatements setQueryTimeout(@Nullable Integer seconds) {
      if (seconds != null && seconds < 0) {
         throw new IllegalArgumentException("queryTimeout must not be < 0");
      } else {
         this.queryTimeout = seconds;
         return this;
      }
   }

   public boolean isUnusedBindingAllowed() {
      return this.allowUnusedBindings;
   }

   public SqlStatements setUnusedBindingAllowed(boolean unusedBindingAllowed) {
      this.allowUnusedBindings = unusedBindingAllowed;
      return this;
   }

   public boolean isAttachAllStatementsForCleanup() {
      return this.attachAllStatementsForCleanup;
   }

   @Beta
   public void setAttachAllStatementsForCleanup(boolean attachAllStatementsForCleanup) {
      this.attachAllStatementsForCleanup = attachAllStatementsForCleanup;
   }

   public boolean isAttachCallbackStatementsForCleanup() {
      return this.attachCallbackStatementsForCleanup;
   }

   public void setScriptStatementsNeedSemicolon(boolean scriptStatementsNeedSemicolon) {
      this.scriptStatementsNeedSemicolon = scriptStatementsNeedSemicolon;
   }

   public boolean isScriptStatementsNeedSemicolon() {
      return this.scriptStatementsNeedSemicolon;
   }

   public void setAttachCallbackStatementsForCleanup(boolean attachCallbackStatementsForCleanup) {
      this.attachCallbackStatementsForCleanup = attachCallbackStatementsForCleanup;
   }

   @Alpha
   public BindListStyle getBindListStyle() {
      return this.bindListStyle;
   }

   @Alpha
   public SqlStatements setBindListStyle(BindListStyle bindListStyle) {
      this.bindListStyle = Objects.requireNonNull(bindListStyle, "bindListStyle");
      return this;
   }

   @Beta
   public SqlStatements setJfrSqlMaxLength(final int jfrSqlMaxLength) {
      this.jfrSqlMaxLength = jfrSqlMaxLength;
      return this;
   }

   @Beta
   public int getJfrSqlMaxLength() {
      return this.jfrSqlMaxLength;
   }

   @Beta
   public boolean getIncludeBindingsInTelemetry() {
      return this.includeBindingsInTelemetry;
   }

   @Beta
   public SqlStatements setJfrParamMaxLength(final int jfrParamMaxLength) {
      this.jfrParamMaxLength = jfrParamMaxLength;
      return this;
   }

   @Beta
   public int getJfrParamMaxLength() {
      return this.jfrParamMaxLength;
   }

   public SqlStatements setIncludeBindingsInTelemetry(boolean includeBindingsInTelemetry) {
      this.includeBindingsInTelemetry = includeBindingsInTelemetry;
      return this;
   }

   @Beta
   public SqlStatements addExceptionHandler(SqlExceptionHandler handler) {
      this.exceptionHandlers.add(handler);
      return this;
   }

   @Beta
   public <T> T cacheStats() {
      return this.templateCache.getStats();
   }

   void customize(Statement statement) throws SQLException {
      if (this.queryTimeout != null) {
         statement.setQueryTimeout(this.queryTimeout);
      }
   }

   public SqlStatements createCopy() {
      return new SqlStatements(this);
   }

   Collection<StatementCustomizer> getCustomizers() {
      return this.customizers;
   }

   Collection<StatementContextListener> getContextListeners() {
      return this.contextListeners;
   }

   String preparedRender(String template, StatementContext ctx) {
      try {
         return Optional.ofNullable(
               this.templateCache.getWithLoader(new SqlStatements.StatementCacheKey(this.templateEngine, template), cacheLoaderFunction(ctx))
            )
            .orElse(cx -> this.templateEngine.render(template, cx))
            .apply(ctx);
      } catch (IllegalArgumentException var4) {
         throw new UnableToCreateStatementException("Exception rendering SQL template", var4, ctx);
      }
   }

   UnableToExecuteStatementException handleException(SQLException e, StatementContext ctx) {
      Iterator<SqlExceptionHandler> handlerIter = this.exceptionHandlers.descendingIterator();

      while (handlerIter.hasNext()) {
         handlerIter.next().handle(e, ctx);
      }

      throw new UnableToExecuteStatementException(e, ctx);
   }

   private static JdbiCacheLoader<SqlStatements.StatementCacheKey, Function<StatementContext, String>> cacheLoaderFunction(StatementContext ctx) {
      return key -> key.getTemplateEngine().parse(key.getTemplate(), ctx.getConfig()).orElse(null);
   }

   private static final class StatementCacheKey {
      private final TemplateEngine templateEngine;
      private final String template;

      StatementCacheKey(TemplateEngine templateEngine, String template) {
         this.templateEngine = templateEngine;
         this.template = template;
      }

      TemplateEngine getTemplateEngine() {
         return this.templateEngine;
      }

      String getTemplate() {
         return this.template;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            SqlStatements.StatementCacheKey that = (SqlStatements.StatementCacheKey)o;
            return this.templateEngine.equals(that.templateEngine) && this.template.equals(that.template);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.templateEngine, this.template);
      }

      @Override
      public String toString() {
         return new StringJoiner(", ", SqlStatements.StatementCacheKey.class.getSimpleName() + "[", "]")
            .add("templateEngine=" + this.templateEngine)
            .add("template='" + this.template + "'")
            .toString();
      }
   }
}
