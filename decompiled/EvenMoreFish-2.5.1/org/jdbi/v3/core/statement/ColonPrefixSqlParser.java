package org.jdbi.v3.core.statement;

import org.jdbi.v3.core.cache.JdbiCacheBuilder;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$CharStreams;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$Token;
import org.jdbi.v3.core.internal.lexer.ColonStatementLexer;
import org.jdbi.v3.core.statement.internal.ErrorListener;
import org.jdbi.v3.meta.Beta;

public class ColonPrefixSqlParser extends CachingSqlParser {
   public ColonPrefixSqlParser() {
   }

   @Beta
   public ColonPrefixSqlParser(JdbiCacheBuilder cacheBuilder) {
      super(cacheBuilder);
   }

   @Override
   public String nameParameter(String rawName, StatementContext ctx) {
      return ":" + rawName;
   }

   @Override
   ParsedSql internalParse(String sql) {
      ParsedSql.Builder parsedSql = ParsedSql.builder();
      ColonStatementLexer lexer = new ColonStatementLexer($CharStreams.fromString(sql));
      lexer.addErrorListener(new ErrorListener());

      for ($Token t = lexer.nextToken(); t.getType() != -1; t = lexer.nextToken()) {
         switch (t.getType()) {
            case 1:
            case 2:
            case 3:
            case 7:
               parsedSql.append(t.getText());
               break;
            case 4:
               parsedSql.append(t.getText().substring(1));
               break;
            case 5:
               parsedSql.appendNamedParameter(t.getText().substring(1));
               break;
            case 6:
               parsedSql.appendPositionalParameter();
         }
      }

      return parsedSql.build();
   }
}
