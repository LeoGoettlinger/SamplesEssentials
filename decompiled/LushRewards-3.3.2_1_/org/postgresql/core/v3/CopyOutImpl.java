package org.postgresql.core.v3;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.copy.CopyOut;

public class CopyOutImpl extends CopyOperationImpl implements CopyOut {
   @Nullable
   private byte[] currentDataRow;

   @Nullable
   @Override
   public byte[] readFromCopy() throws SQLException {
      return this.readFromCopy(true);
   }

   @Nullable
   @Override
   public byte[] readFromCopy(boolean block) throws SQLException {
      this.currentDataRow = null;
      this.getQueryExecutor().readFromCopy(this, block);
      return this.currentDataRow;
   }

   @Override
   protected void handleCopydata(byte[] data) {
      this.currentDataRow = data;
   }
}
