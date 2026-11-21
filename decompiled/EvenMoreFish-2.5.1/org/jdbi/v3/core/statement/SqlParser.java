package org.jdbi.v3.core.statement;

public interface SqlParser {
   ParsedSql parse(String sql, StatementContext ctx);

   String nameParameter(String rawName, StatementContext ctx);
}
