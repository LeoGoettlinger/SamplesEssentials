package org.jdbi.v3.core.mapper.reflect.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class NullDelegatingMapper<T> implements RowMapper<T> {
   private final int index;
   private final RowMapper<T> delegate;
   private final T nullResult;

   public NullDelegatingMapper(int index, RowMapper<T> delegate) {
      this(index, delegate, null);
   }

   public NullDelegatingMapper(int index, RowMapper<T> delegate, T nullResult) {
      this.index = index;
      this.delegate = delegate;
      this.nullResult = nullResult;
   }

   @Override
   public void init(ConfigRegistry registry) {
      this.delegate.init(registry);
   }

   @Override
   public T map(ResultSet rs, StatementContext ctx) throws SQLException {
      rs.getObject(this.index);
      return rs.wasNull() ? this.nullResult : this.delegate.map(rs, ctx);
   }

   @Override
   public RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
      return new NullDelegatingMapper<>(this.index, this.delegate.specialize(rs, ctx), this.nullResult);
   }

   @Override
   public String toString() {
      return new StringJoiner(", ", NullDelegatingMapper.class.getSimpleName() + "[", "]")
         .add("index=" + this.index)
         .add("delegate=" + this.delegate)
         .add("nullResult=" + this.nullResult)
         .toString();
   }
}
