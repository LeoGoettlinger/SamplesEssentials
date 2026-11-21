package org.postgresql.core.v3;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.copy.CopyOperation;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public abstract class CopyOperationImpl implements CopyOperation {
   @Nullable
   QueryExecutorImpl queryExecutor;
   int rowFormat;
   @Nullable
   int[] fieldFormats;
   long handledRowCount = -1L;

   void init(QueryExecutorImpl q, int fmt, int[] fmts) {
      this.queryExecutor = q;
      this.rowFormat = fmt;
      this.fieldFormats = fmts;
   }

   protected QueryExecutorImpl getQueryExecutor() {
      return Nullness.castNonNull(this.queryExecutor);
   }

   @Override
   public void cancelCopy() throws SQLException {
      Nullness.castNonNull(this.queryExecutor).cancelCopy(this);
   }

   @Override
   public int getFieldCount() {
      return Nullness.castNonNull(this.fieldFormats).length;
   }

   @Override
   public int getFieldFormat(int field) {
      return Nullness.castNonNull(this.fieldFormats)[field];
   }

   @Override
   public int getFormat() {
      return this.rowFormat;
   }

   @Override
   public boolean isActive() {
      return Nullness.castNonNull(this.queryExecutor).hasLockOn(this);
   }

   public void handleCommandStatus(String status) throws PSQLException {
      if (status.startsWith("COPY")) {
         int i = status.lastIndexOf(32);
         this.handledRowCount = i > 3 ? Long.parseLong(status.substring(i + 1)) : -1L;
      } else {
         throw new PSQLException(GT.tr("CommandComplete expected COPY but got: " + status), PSQLState.COMMUNICATION_ERROR);
      }
   }

   protected abstract void handleCopydata(byte[] var1) throws PSQLException;

   @Override
   public long getHandledRowCount() {
      return this.handledRowCount;
   }
}
