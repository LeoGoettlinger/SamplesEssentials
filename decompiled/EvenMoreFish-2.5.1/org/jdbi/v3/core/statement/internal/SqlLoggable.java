package org.jdbi.v3.core.statement.internal;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlLoggable<T> {
   T invoke() throws SQLException;
}
