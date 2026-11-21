package org.jdbi.v3.core.result;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface ResultProducer<R> {
   R produce(Supplier<PreparedStatement> statementSupplier, StatementContext ctx) throws SQLException;
}
