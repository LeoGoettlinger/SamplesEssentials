package org.jdbi.v3.core.statement;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Batch extends BaseStatement<Batch> {
   private static final Logger LOG = LoggerFactory.getLogger(Batch.class);
   private final List<String> parts = new ArrayList<>();

   public Batch(Handle handle) {
      super(handle);
   }

   public Batch add(String sql) {
      this.parts.add(sql);
      return this;
   }

   public int[] execute() {
      if (this.parts.isEmpty()) {
         return new int[0];
      } else {
         int[] var15;
         try {
            SqlStatements stmtConfig = this.getConfig(SqlStatements.class);

            Statement stmt;
            try {
               stmt = this.createStatement();
               this.getContext().addCleanable(() -> this.cleanupStatement(stmt));
               stmtConfig.customize(stmt);
            } catch (SQLException var12) {
               throw new UnableToCreateStatementException(var12, this.getContext());
            }

            LOG.trace("Execute batch [");

            try {
               for (String part : this.parts) {
                  String sql = stmtConfig.preparedRender(part, this.getContext());
                  LOG.trace(" {}", sql);
                  stmt.addBatch(sql);
               }
            } catch (SQLException var13) {
               throw new UnableToExecuteStatementException("Unable to configure JDBC statement", var13, this.getContext());
            }

            try {
               var15 = SqlLoggerUtil.wrap(stmt::executeBatch, this.getContext(), stmtConfig.getSqlLogger());
            } catch (SQLException var11) {
               throw stmtConfig.handleException(mungeBatchException(var11), this.getContext());
            }
         } finally {
            this.close();
         }

         return var15;
      }
   }

   Statement createStatement() throws SQLException {
      return this.getHandle().getStatementBuilder().create(this.getHandle().getConnection(), this.getContext());
   }

   void cleanupStatement(final Statement statement) throws SQLException {
      this.getHandle().getStatementBuilder().close(this.getHandle().getConnection(), "<batch>", statement);
   }

   static SQLException mungeBatchException(SQLException e) {
      for (SQLException next = e.getNextException(); next != null; next = next.getNextException()) {
         e.addSuppressed(next);
      }

      return e;
   }
}
