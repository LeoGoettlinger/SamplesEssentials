package com.mysql.cj.protocol.a.result;

import com.mysql.cj.Messages;
import com.mysql.cj.Session;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.protocol.a.BinaryRowFactory;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativeProtocol;
import com.mysql.cj.result.Row;
import com.mysql.cj.telemetry.TelemetryAttribute;
import com.mysql.cj.telemetry.TelemetryScope;
import com.mysql.cj.telemetry.TelemetrySpan;
import com.mysql.cj.telemetry.TelemetrySpanName;
import java.util.ArrayList;
import java.util.List;

public class ResultsetRowsCursor extends AbstractResultsetRows implements ResultsetRows {
   private List<Row> fetchedRows;
   private int currentPositionInEntireResult = -1;
   private boolean lastRowFetched = false;
   private NativeProtocol protocol;
   private boolean firstFetchCompleted = false;
   protected NativeMessageBuilder commandBuilder = null;

   public ResultsetRowsCursor(NativeProtocol ioChannel, ColumnDefinition columnDefinition) {
      this.currentPositionInEntireResult = -1;
      this.metadata = columnDefinition;
      this.protocol = ioChannel;
      this.rowFactory = new BinaryRowFactory(this.protocol, this.metadata, Resultset.Concurrency.READ_ONLY, false);
      this.commandBuilder = new NativeMessageBuilder(this.protocol.getServerSession().supportsQueryAttributes());
   }

   @Override
   public boolean isAfterLast() {
      return this.lastRowFetched && this.currentPositionInFetchedRows > this.fetchedRows.size();
   }

   @Override
   public boolean isBeforeFirst() {
      return this.currentPositionInEntireResult < 0;
   }

   @Override
   public int getPosition() {
      return this.currentPositionInEntireResult + 1;
   }

   @Override
   public boolean isEmpty() {
      return this.isBeforeFirst() && this.isAfterLast();
   }

   @Override
   public boolean isFirst() {
      return this.currentPositionInEntireResult == 0;
   }

   @Override
   public boolean isLast() {
      return this.lastRowFetched && this.currentPositionInFetchedRows == this.fetchedRows.size() - 1;
   }

   @Override
   public void close() {
      this.metadata = null;
      this.owner = null;
   }

   @Override
   public boolean hasNext() {
      if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
         return false;
      } else {
         if (this.owner != null) {
            int maxRows = this.owner.getOwningStatementMaxRows();
            if (maxRows != -1 && this.currentPositionInEntireResult + 1 > maxRows) {
               return false;
            }
         }

         if (this.currentPositionInEntireResult != -1) {
            if (this.currentPositionInFetchedRows < this.fetchedRows.size() - 1) {
               return true;
            } else if (this.currentPositionInFetchedRows == this.fetchedRows.size() && this.lastRowFetched) {
               return false;
            } else {
               this.fetchMoreRows();
               return this.fetchedRows.size() > 0;
            }
         } else {
            this.fetchMoreRows();
            return this.fetchedRows.size() > 0;
         }
      }
   }

   public Row next() {
      if (this.fetchedRows == null && this.currentPositionInEntireResult != -1) {
         throw ExceptionFactory.createException(
            Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), this.protocol.getExceptionInterceptor()
         );
      } else if (!this.hasNext()) {
         return null;
      } else {
         this.currentPositionInEntireResult++;
         this.currentPositionInFetchedRows++;
         if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
            return null;
         } else {
            if (this.fetchedRows == null || this.currentPositionInFetchedRows > this.fetchedRows.size() - 1) {
               this.fetchMoreRows();
               this.currentPositionInFetchedRows = 0;
            }

            Row row = this.fetchedRows.get(this.currentPositionInFetchedRows);
            row.setMetadata(this.metadata);
            return row;
         }
      }
   }

   private void fetchMoreRows() {
      if (this.lastRowFetched) {
         this.fetchedRows = new ArrayList<>(0);
      } else {
         synchronized (this.owner.getSyncMutex()) {
            Session session = this.owner.getSession();
            TelemetrySpan span = session.getTelemetryHandler().startSpan(TelemetrySpanName.STMT_FETCH_PREPARED);

            try (TelemetryScope scope = span.makeCurrent()) {
               span.setAttribute(TelemetryAttribute.DB_NAME, session.getHostInfo().getDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, session.getHostInfo().getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());

               try {
                  boolean oldFirstFetchCompleted = this.firstFetchCompleted;
                  if (!this.firstFetchCompleted) {
                     this.firstFetchCompleted = true;
                  }

                  int numRowsToFetch = this.owner.getOwnerFetchSize();
                  if (numRowsToFetch == 0) {
                     numRowsToFetch = this.owner.getOwningStatementFetchSize();
                  }

                  if (numRowsToFetch == Integer.MIN_VALUE) {
                     numRowsToFetch = 1;
                  }

                  if (this.fetchedRows == null) {
                     this.fetchedRows = new ArrayList<>(numRowsToFetch);
                  } else {
                     this.fetchedRows.clear();
                  }

                  this.protocol
                     .sendCommand(
                        this.commandBuilder.buildComStmtFetch(this.protocol.getSharedSendPacket(), this.owner.getOwningStatementServerId(), numRowsToFetch),
                        true,
                        0
                     );
                  Row row = null;

                  while ((row = this.protocol.read(ResultsetRow.class, this.rowFactory)) != null) {
                     this.fetchedRows.add(row);
                  }

                  this.currentPositionInFetchedRows = -1;
                  if (this.protocol.getServerSession().isLastRowSent()) {
                     this.lastRowFetched = true;
                     if (!oldFirstFetchCompleted && this.fetchedRows.size() == 0) {
                        this.wasEmpty = true;
                     }
                  }
               } catch (Exception var30) {
                  throw ExceptionFactory.createException(var30.getMessage(), var30);
               }
            } catch (Throwable var33) {
               span.setError(var33);
               throw var33;
            } finally {
               span.end();
            }
         }
      }
   }

   @Override
   public void addRow(Row row) {
   }

   @Override
   public void afterLast() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void beforeFirst() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void beforeLast() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void moveRowRelative(int rows) {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void setCurrentRow(int rowNumber) {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }
}
