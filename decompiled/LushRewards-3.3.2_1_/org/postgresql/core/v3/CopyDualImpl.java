package org.postgresql.core.v3;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Queue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.copy.CopyDual;
import org.postgresql.util.ByteStreamWriter;
import org.postgresql.util.PSQLException;

public class CopyDualImpl extends CopyOperationImpl implements CopyDual {
   private final Queue<byte[]> received = new ArrayDeque<>();

   @Override
   public void writeToCopy(byte[] data, int off, int siz) throws SQLException {
      this.getQueryExecutor().writeToCopy(this, data, off, siz);
   }

   @Override
   public void writeToCopy(ByteStreamWriter from) throws SQLException {
      this.getQueryExecutor().writeToCopy(this, from);
   }

   @Override
   public void flushCopy() throws SQLException {
      this.getQueryExecutor().flushCopy(this);
   }

   @Override
   public long endCopy() throws SQLException {
      return this.getQueryExecutor().endCopy(this);
   }

   @Nullable
   @Override
   public byte[] readFromCopy() throws SQLException {
      return this.readFromCopy(true);
   }

   @Nullable
   @Override
   public byte[] readFromCopy(boolean block) throws SQLException {
      if (this.received.isEmpty()) {
         this.getQueryExecutor().readFromCopy(this, block);
      }

      return this.received.poll();
   }

   @Override
   public void handleCommandStatus(String status) throws PSQLException {
   }

   @Override
   protected void handleCopydata(byte[] data) {
      this.received.add(data);
   }
}
