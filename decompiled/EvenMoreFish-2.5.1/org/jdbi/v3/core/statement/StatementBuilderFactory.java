package org.jdbi.v3.core.statement;

import java.sql.Connection;

@FunctionalInterface
public interface StatementBuilderFactory {
   StatementBuilder createStatementBuilder(Connection conn);
}
