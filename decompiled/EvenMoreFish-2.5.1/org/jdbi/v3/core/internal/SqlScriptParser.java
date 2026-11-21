package org.jdbi.v3.core.internal;

import java.util.ArrayList;
import java.util.List;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$CharStream;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$Token;
import org.jdbi.v3.core.internal.lexer.SqlScriptLexer;
import org.jdbi.v3.core.statement.internal.ErrorListener;

public class SqlScriptParser {
   private final SqlScriptParser.TokenHandler tokenHandler;

   public SqlScriptParser(SqlScriptParser.TokenHandler tokenHandler) {
      this.tokenHandler = tokenHandler;
   }

   public String parse($CharStream charStream) {
      StringBuilder sb = new StringBuilder();
      SqlScriptLexer lexer = new SqlScriptLexer(charStream);
      lexer.addErrorListener(new ErrorListener());
      boolean endOfFile = false;

      while (!endOfFile) {
         $Token t = lexer.nextToken();
         switch (t.getType()) {
            case -1:
               endOfFile = true;
               break;
            case 0:
            default:
               throw new IllegalArgumentException("Unrecognizable token " + t);
            case 1:
            case 2:
               break;
            case 3:
               if (sb.length() > 0) {
                  sb.append('\n');
               }
               break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
               this.tokenHandler.handle(t, sb);
         }
      }

      return sb.toString();
   }

   public static final class ScriptTokenHandler implements SqlScriptParser.TokenHandler {
      private final boolean requireSemicolon;
      private final List<String> statements = new ArrayList<>();
      private $Token lastToken = null;
      private int blockLevel = 0;

      public ScriptTokenHandler(boolean requireSemicolon) {
         this.requireSemicolon = requireSemicolon;
      }

      @Override
      public void handle($Token t, StringBuilder sb) {
         switch (t.getType()) {
            case 5:
               this.blockLevel++;
               sb.append(t.getText());
               break;
            case 6:
               if (this.blockLevel == 0) {
                  throw new IllegalStateException("Found END in SQL script without BEGIN!");
               }

               this.blockLevel--;
               sb.append(t.getText());
               break;
            case 7:
               if (this.blockLevel == 0) {
                  if (this.requireSemicolon && this.lastToken != null && this.lastToken.getType() == 6) {
                     sb.append(t.getText());
                  }

                  this.addStatement(sb.toString());
                  sb.setLength(0);
               } else {
                  sb.append(t.getText());
               }
               break;
            default:
               sb.append(t.getText());
         }

         this.lastToken = t;
      }

      public List<String> getStatements() {
         return this.statements;
      }

      public List<String> addStatement(String statement) {
         String trimmedStatement = statement.trim();
         if (!trimmedStatement.isEmpty()) {
            this.statements.add(trimmedStatement);
         }

         return this.statements;
      }
   }

   @FunctionalInterface
   public interface TokenHandler {
      void handle($Token t, StringBuilder sb);
   }
}
