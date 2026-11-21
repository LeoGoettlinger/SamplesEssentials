package org.jdbi.v3.core.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface ResultSetScanner<T> {
   T scanResultSet(Supplier<ResultSet> resultSetSupplier, StatementContext ctx) throws SQLException;
}
