package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Session;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.StreamingNotifiable;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRow;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRows;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.BinaryRowFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativeMessageBuilder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativePacketPayload;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativeProtocol;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.TextRowFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpanName;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.Util;

public class ResultsetRowsStreaming<T extends ProtocolEntity> extends AbstractResultsetRows implements ResultsetRows {
   private NativeProtocol protocol;
   private NativeMessageBuilder commandBuilder = null;
   private ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory;
   private final Lock lock = new ReentrantLock();
   private Row nextRow;
   private boolean isAfterEnd = false;
   private boolean noMoreRows = false;
   private boolean isBinaryEncoded = false;
   private boolean streamerClosed = false;
   private ExceptionInterceptor exceptionInterceptor;

   public ResultsetRowsStreaming(
      NativeProtocol protocol, ColumnDefinition columnDefinition, boolean isBinaryEncoded, ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) {
      this.protocol = protocol;
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
      boolean hadMore = false;
      int howMuchMore = 0;
      Lock localLock = this.owner != null && this.owner.getLock() != null ? this.owner.getLock() : this.lock;
      localLock.lock();

      try {
         while (this.next() != null) {
            hadMore = true;
            if (++howMuchMore % 100 == 0) {
               Thread.yield();
            }
         }

         if (this.protocol.getPropertySet().getIntegerProperty(PropertyKey.netTimeoutForStreamingResults).getValue() > 0) {
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
               } catch (Exception var39) {
                  throw ExceptionFactory.createException(var39.getMessage(), var39, this.exceptionInterceptor);
               }
            } catch (Throwable var42) {
               span.setError(var42);
               throw var42;
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
      } finally {
         localLock.unlock();
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
