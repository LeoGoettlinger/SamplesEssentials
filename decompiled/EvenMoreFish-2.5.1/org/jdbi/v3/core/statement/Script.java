package org.jdbi.v3.core.statement;

import java.util.List;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$CharStreams;
import org.jdbi.v3.core.internal.SqlScriptParser;

public class Script extends SqlStatement<Script> {
   private final boolean requireSemicolon;

   public Script(Handle handle, CharSequence sql) {
      super(handle, sql);
      this.requireSemicolon = handle.getConfig(SqlStatements.class).isScriptStatementsNeedSemicolon();
   }

   public Script(Handle handle, String sql) {
      super(handle, sql);
      this.requireSemicolon = handle.getConfig(SqlStatements.class).isScriptStatementsNeedSemicolon();
   }

   public int[] execute() {
      List<String> statements = this.getStatements();

      int[] var3;
      try (Batch b = this.getHandle().createBatch()) {
         statements.forEach(b::add);
         var3 = b.execute();
      }

      return var3;
   }

   public void executeAsSeparateStatements() {
      for (String s : this.getStatements()) {
         this.getHandle().execute(s);
      }
   }

   public List<String> getStatements() {
      return this.splitToStatements(this.getConfig(SqlStatements.class).preparedRender(this.getSql(), this.getContext()));
   }

   private List<String> splitToStatements(String script) {
      SqlScriptParser.ScriptTokenHandler scriptTokenHandler = new SqlScriptParser.ScriptTokenHandler(this.requireSemicolon);
      String lastStatement = new SqlScriptParser(scriptTokenHandler).parse($CharStreams.fromString(script));
      return scriptTokenHandler.addStatement(lastStatement);
   }
}
