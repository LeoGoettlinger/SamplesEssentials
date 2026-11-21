package org.jdbi.v3.core.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface ResultSetAccumulator<T> {
   T apply(T previous, ResultSet rs, StatementContext ctx) throws SQLException;
}
