package org.jdbi.v3.core.statement;

import org.jdbi.v3.core.cache.JdbiCache;
import org.jdbi.v3.core.cache.JdbiCacheBuilder;
import org.jdbi.v3.core.cache.internal.DefaultJdbiCacheBuilder;
import org.jdbi.v3.meta.Beta;

public abstract class CachingSqlParser implements SqlParser {
   public static final int PARSED_SQL_CACHE_SIZE = 1000;
   private final JdbiCache<String, ParsedSql> parsedSqlCache;

   CachingSqlParser() {
      this(DefaultJdbiCacheBuilder.builder().maxSize(1000));
   }

   CachingSqlParser(JdbiCacheBuilder cacheBuilder) {
      this.parsedSqlCache = cacheBuilder.buildWithLoader(this::internalParse);
   }

   @Override
   public ParsedSql parse(String sql, StatementContext ctx) {
      try {
         return this.parsedSqlCache.get(sql);
      } catch (IllegalArgumentException var4) {
         throw new UnableToCreateStatementException("Exception parsing for named parameter replacement", var4, ctx);
      }
   }

   @Beta
   public <T> T cacheStats() {
      return this.parsedSqlCache.getStats();
   }

   abstract ParsedSql internalParse(String sql);
}
