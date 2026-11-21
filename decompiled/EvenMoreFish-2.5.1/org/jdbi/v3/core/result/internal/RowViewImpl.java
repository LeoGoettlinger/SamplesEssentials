package org.jdbi.v3.core.result.internal;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jdbi.v3.core.internal.PrefixedMapperKey;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.MappingException;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.StatementContext;

public class RowViewImpl extends RowView {
   private final StatementContext ctx;
   private final ResultSet rs;
   private final Map<Type, RowMapper<?>> rowMappers = new ConcurrentHashMap<>();
   private final Map<PrefixedMapperKey, RowMapper<?>> prefixedRowMappers = new ConcurrentHashMap<>();
   private final Map<QualifiedType<?>, ColumnMapper<?>> columnMappers = new ConcurrentHashMap<>();
   private final Map<RowMapper<?>, RowMapper<?>> specializedMappers = new ConcurrentHashMap<>();

   public RowViewImpl(ResultSet rs, StatementContext ctx) {
      this.rs = rs;
      this.ctx = ctx;
   }

   @Override
   public Object getRow(Type type) {
      try {
         return this.rowMapperFor(type).map(this.rs, this.ctx);
      } catch (SQLException var3) {
         throw new MappingException(var3);
      }
   }

   @Override
   public Object getRow(Type type, String prefix) {
      Objects.requireNonNull(prefix, "prefix; use getRow(type) to map without a prefix");

      try {
         return this.prefixedRowMapperFor(type, prefix).map(this.rs, this.ctx);
      } catch (SQLException var4) {
         throw new MappingException(var4);
      }
   }

   private RowMapper<?> rowMapperFor(Type type) throws SQLException {
      if (this.rowMappers.containsKey(type)) {
         return this.rowMappers.get(type);
      } else {
         RowMapper<?> mapper = this.ctx
            .findRowMapperFor(type)
            .orElseThrow(() -> new NoSuchMapperException("No row mapper registered for " + type))
            .specialize(this.rs, this.ctx);
         this.rowMappers.put(type, mapper);
         return mapper;
      }
   }

   private RowMapper<?> prefixedRowMapperFor(Type type, String prefix) throws SQLException {
      PrefixedMapperKey key = new PrefixedMapperKey(type, prefix);
      RowMapper<?> cached = this.prefixedRowMappers.get(key);
      if (cached != null) {
         return cached;
      } else {
         RowMapper<?> mapper = this.ctx
            .getConfig(RowMappers.class)
            .findFor(type, prefix)
            .orElseThrow(() -> new NoSuchMapperException("No row mapper registered for " + type + " with prefix \"" + prefix + "\""))
            .specialize(this.rs, this.ctx);
         this.prefixedRowMappers.put(key, mapper);
         return mapper;
      }
   }

   @Override
   public List<String> getColumnNames() {
      try {
         ResultSetMetaData metaData = this.rs.getMetaData();
         List<String> names = new ArrayList<>(metaData.getColumnCount());

         for (int i = 1; i <= metaData.getColumnCount(); i++) {
            names.add(metaData.getColumnLabel(i));
         }

         return Collections.unmodifiableList(names);
      } catch (SQLException var4) {
         throw new MappingException(var4);
      }
   }

   @Override
   public <T> T getRow(RowMapper<T> mapper) {
      try {
         RowMapper<?> specialized = this.specializedMappers.get(mapper);
         if (specialized == null) {
            specialized = mapper.specialize(this.rs, this.ctx);
            this.specializedMappers.put(mapper, specialized);
         }

         return (T)specialized.map(this.rs, this.ctx);
      } catch (SQLException var3) {
         throw new MappingException(var3);
      }
   }

   @Override
   public Object getColumn(String column) {
      try {
         return this.rs.getObject(column);
      } catch (SQLException var3) {
         throw new MappingException(var3);
      }
   }

   @Override
   public Object getColumn(int column) {
      try {
         return this.rs.getObject(column);
      } catch (SQLException var3) {
         throw new MappingException(var3);
      }
   }

   @Override
   public <T> T getColumn(int column, QualifiedType<T> type) {
      try {
         return (T)this.columnMapperFor(type).map(this.rs, column, this.ctx);
      } catch (SQLException var4) {
         throw new MappingException(var4);
      }
   }

   @Override
   public <T> T getColumn(String column, QualifiedType<T> type) {
      try {
         return (T)this.columnMapperFor(type).map(this.rs, column, this.ctx);
      } catch (SQLException var4) {
         throw new MappingException(var4);
      }
   }

   private ColumnMapper<?> columnMapperFor(QualifiedType<?> type) {
      return this.columnMappers
         .computeIfAbsent(
            type, t -> this.ctx.findColumnMapperFor((QualifiedType<?>)t).orElseThrow(() -> new NoSuchMapperException("No column mapper registered for " + t))
         );
   }
}
