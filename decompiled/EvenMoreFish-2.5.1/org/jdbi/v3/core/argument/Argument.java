package org.jdbi.v3.core.argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface Argument {
   void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException;
}
