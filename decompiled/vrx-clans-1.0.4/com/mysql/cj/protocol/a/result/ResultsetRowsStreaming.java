package com.mysql.cj.protocol.a.result;

import com.mysql.cj.Messages;
import com.mysql.cj.Session;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.StreamingNotifiable;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.protocol.a.BinaryRowFactory;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.protocol.a.NativeProtocol;
import com.mysql.cj.protocol.a.TextRowFactory;
import com.mysql.cj.result.Row;
import com.mysql.cj.telemetry.TelemetryAttribute;
import com.mysql.cj.telemetry.TelemetryScope;
import com.mysql.cj.telemetry.TelemetrySpan;
import com.mysql.cj.telemetry.TelemetrySpanName;
import com.mysql.cj.util.Util;

public class ResultsetRowsStreaming<T extends ProtocolEntity> extends AbstractResultsetRows implements ResultsetRows {
   private NativeProtocol protocol;
   private boolean isAfterEnd = false;
   private boolean noMoreRows = false;
   private boolean isBinaryEncoded = false;
   private Row nextRow;
   private boolean streamerClosed = false;
   private ExceptionInterceptor exceptionInterceptor;
   private ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory;
   private NativeMessageBuilder commandBuilder = null;

   public ResultsetRowsStreaming(
      NativeProtocol io, ColumnDefinition columnDefinition, boolean isBinaryEncoded, ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) {
      this.protocol = io;
      this.isBinaryEncoded = isBinaryEncoded;
      this.metadata = columnDefinition;
      this.exceptionInterceptor = this.protocol.getExceptionInterceptor();
      this.resultSetFactory = resultSetFactory;
      this.rowFactory = (ProtocolEntityFactory<ResultsetRow, NativePacketPayload>)(this.isBinaryEncoded
         ? new BinaryRowFactory(this.protocol, this.metadata, Resultset.Concurrency.READ_ONLY, true)
         : new TextRowFactory(this.protocol, this.metadata, Resultset.Concurrency.READ_ONLY, true));
      this.commandBuilder = new NativeMessageBuilder(this.protocol.getServerSession().supportsQueryAttributes());
   }

   @Override
   public void close() {
      Object mutex = this.owner != null && this.owner.getSyncMutex() != null ? this.owner.getSyncMutex() : this;
      boolean hadMore = false;
      int howMuchMore = 0;
      synchronized (mutex) {
         while (this.next() != null) {
            hadMore = true;
            if (++howMuchMore % 100 == 0) {
               Thread.yield();
            }
         }

         if (!this.protocol.getPropertySet().getBooleanProperty(PropertyKey.clobberStreamingResults).getValue()
            && this.protocol.getPropertySet().getIntegerProperty(PropertyKey.netTimeoutForStreamingResults).getValue() > 0) {
            Session session = this.owner.getSession();
            TelemetrySpan span = session.getTelemetryHandler().startSpan(TelemetrySpanName.SET_VARIABLE, "net_write_timeout");

            try (TelemetryScope scope = span.makeCurrent()) {
               span.setAttribute(TelemetryAttribute.DB_NAME, session.getHostInfo().getDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, session.getHostInfo().getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               int oldValue = this.protocol.getServerSession().getServerVariable("net_write_timeout", 60);
               this.protocol.clearInputStream();

               try {
                  this.protocol
                     .sendCommand(
                        this.commandBuilder
                           .buildComQuery(
                              this.protocol.getSharedSendPacket(),
                              session,
                              "SET net_write_timeout=" + oldValue,
                              this.protocol.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue()
                           ),
                        false,
                        0
                     );
               } catch (Exception var32) {
                  throw ExceptionFactory.createException(var32.getMessage(), var32, this.exceptionInterceptor);
               }
            } catch (Throwable var35) {
               span.setError(var35);
               throw var35;
            } finally {
               span.end();
            }
         }

         if (this.protocol.getPropertySet().getBooleanProperty(PropertyKey.useUsageAdvisor).getValue() && hadMore) {
            this.owner
               .getSession()
               .getProfilerEventHandler()
               .processEvent(
                  (byte)0,
                  this.owner.getSession(),
                  this.owner.getOwningQuery(),
                  null,
                  0L,
                  new Throwable(),
                  Messages.getString("RowDataDynamic.1", new String[]{String.valueOf(howMuchMore), this.owner.getPointOfOrigin()})
               );
         }
      }

      this.metadata = null;
      this.owner = null;
   }

   @Override
   public boolean hasNext() {
      boolean hasNext = this.nextRow != null;
      if (!hasNext && !this.streamerClosed) {
         this.protocol.unsetStreamingData(this);
         this.streamerClosed = true;
      }

      return hasNext;
   }

   @Override
   public boolean isAfterLast() {
      return this.isAfterEnd;
   }

   @Override
   public boolean isBeforeFirst() {
      return this.currentPositionInFetchedRows < 0;
   }

   @Override
   public boolean isEmpty() {
      return this.wasEmpty;
   }

   @Override
   public boolean isFirst() {
      return this.currentPositionInFetchedRows == 0;
   }

   @Override
   public boolean isLast() {
      return !this.isBeforeFirst() && !this.isAfterLast() && this.noMoreRows;
   }

   public Row next() {
      try {
         if (!this.noMoreRows) {
            this.nextRow = this.protocol.read(ResultsetRow.class, this.rowFactory);
            if (this.nextRow == null) {
               this.noMoreRows = true;
               this.isAfterEnd = true;
               if (this.currentPositionInFetchedRows == -1) {
                  this.wasEmpty = true;
               }
            }
         } else {
            this.nextRow = null;
            this.isAfterEnd = true;
         }

         if (this.nextRow == null && !this.streamerClosed) {
            if (this.protocol.getServerSession().hasMoreResults()) {
               this.protocol.readNextResultset((T)this.owner, this.owner.getOwningStatementMaxRows(), true, this.isBinaryEncoded, this.resultSetFactory);
            } else {
               this.protocol.unsetStreamingData(this);
               this.streamerClosed = true;
            }
         }

         if (this.nextRow != null && this.currentPositionInFetchedRows != Integer.MAX_VALUE) {
            this.currentPositionInFetchedRows++;
         }

         return this.nextRow;
      } catch (CJException var3) {
         if (var3 instanceof StreamingNotifiable) {
            ((StreamingNotifiable)var3).setWasStreamingResults();
         }

         this.noMoreRows = true;
         throw var3;
      } catch (Exception var4) {
         CJException cjEx = ExceptionFactory.createException(
            Messages.getString("RowDataDynamic.2", new String[]{var4.getClass().getName(), var4.getMessage(), Util.stackTraceToString(var4)}),
            var4,
            this.exceptionInterceptor
         );
         throw cjEx;
      }
   }

   @Override
   public int getPosition() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
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
