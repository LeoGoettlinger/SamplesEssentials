package org.jdbi.v3.core.statement;

import java.sql.CallableStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface CallableStatementMapper {
   Object map(int position, CallableStatement stmt) throws SQLException;
}
