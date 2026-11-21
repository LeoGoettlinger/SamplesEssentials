package org.jdbi.v3.core.argument.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementBinder<T> {
   void bind(PreparedStatement p, int index, T value) throws SQLException;
}
