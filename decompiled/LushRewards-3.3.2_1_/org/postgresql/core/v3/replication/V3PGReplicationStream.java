package org.postgresql.core.v3.replication;

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.copy.CopyDual;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.ReplicationType;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class V3PGReplicationStream implements PGReplicationStream {
   private static final Logger LOGGER = Logger.getLogger(V3PGReplicationStream.class.getName());
   public static final long POSTGRES_EPOCH_2000_01_01 = 946684800000L;
   private static final long NANOS_PER_MILLISECOND = 1000000L;
   private final CopyDual copyDual;
   private final long updateInterval;
   private final ReplicationType replicationType;
   private long lastStatusUpdate;
   private boolean closeFlag;
   private LogSequenceNumber lastServerLSN = LogSequenceNumber.INVALID_LSN;
   private volatile LogSequenceNumber lastReceiveLSN = LogSequenceNumber.INVALID_LSN;
   private volatile LogSequenceNumber lastAppliedLSN = LogSequenceNumber.INVALID_LSN;
   private volatile LogSequenceNumber lastFlushedLSN = LogSequenceNumber.INVALID_LSN;
   private volatile LogSequenceNumber startOfLastMessageLSN = LogSequenceNumber.INVALID_LSN;
   private volatile LogSequenceNumber explicitlyFlushedLSN = LogSequenceNumber.INVALID_LSN;

   public V3PGReplicationStream(CopyDual copyDual, LogSequenceNumber startLSN, long updateIntervalMs, ReplicationType replicationType) {
      this.copyDual = copyDual;
      this.updateInterval = updateIntervalMs * 1000000L;
      this.lastStatusUpdate = System.nanoTime() - updateIntervalMs * 1000000L;
      this.lastReceiveLSN = startLSN;
      this.replicationType = replicationType;
   }

   @Nullable
   @Override
   public ByteBuffer read() throws SQLException {
      this.checkClose();
      ByteBuffer payload = null;

      while (payload == null && this.copyDual.isActive()) {
         payload = this.readInternal(true);
      }

      return payload;
   }

   @Nullable
   @Override
   public ByteBuffer readPending() throws SQLException {
      this.checkClose();
      return this.readInternal(false);
   }

   @Override
   public LogSequenceNumber getLastReceiveLSN() {
      return this.lastReceiveLSN;
   }

   @Override
   public LogSequenceNumber getLastFlushedLSN() {
      return this.lastFlushedLSN;
   }

   @Override
   public LogSequenceNumber getLastAppliedLSN() {
      return this.lastAppliedLSN;
   }

   @Override
   public void setFlushedLSN(LogSequenceNumber flushed) {
      this.lastFlushedLSN = flushed;
   }

   @Override
   public void setAppliedLSN(LogSequenceNumber applied) {
      this.lastAppliedLSN = applied;
   }

   @Override
   public void forceUpdateStatus() throws SQLException {
      this.checkClose();
      this.updateStatusInternal(this.lastReceiveLSN, this.lastFlushedLSN, this.lastAppliedLSN, true);
   }

   @Override
   public boolean isClosed() {
      return this.closeFlag || !this.copyDual.isActive();
   }

   @Nullable
   private ByteBuffer readInternal(boolean block) throws SQLException {
      boolean updateStatusRequired = false;

      while (this.copyDual.isActive()) {
         ByteBuffer buffer = this.receiveNextData(block);
         if (updateStatusRequired || this.isTimeUpdate()) {
            this.timeUpdateStatus();
         }

         if (buffer == null) {
            return null;
         }

         int code = buffer.get();
         switch (code) {
            case 107:
               updateStatusRequired = this.processKeepAliveMessage(buffer);
               updateStatusRequired |= this.updateInterval == 0L;
               break;
            case 119:
               return this.processXLogData(buffer);
            default:
               throw new PSQLException(GT.tr("Unexpected packet type during replication: {0}", Integer.toString(code)), PSQLState.PROTOCOL_VIOLATION);
         }
      }

      return null;
   }

   @Nullable
   private ByteBuffer receiveNextData(boolean block) throws SQLException {
      try {
         byte[] message = this.copyDual.readFromCopy(block);
         return message != null ? ByteBuffer.wrap(message) : null;
      } catch (PSQLException var3) {
         if (var3.getCause() instanceof SocketTimeoutException) {
            return null;
         } else {
            throw var3;
         }
      }
   }

   private boolean isTimeUpdate() {
      if (this.updateInterval == 0L) {
         return false;
      } else {
         long diff = System.nanoTime() - this.lastStatusUpdate;
         return diff >= this.updateInterval;
      }
   }

   private void timeUpdateStatus() throws SQLException {
      this.updateStatusInternal(this.lastReceiveLSN, this.lastFlushedLSN, this.lastAppliedLSN, false);
   }

   private void updateStatusInternal(LogSequenceNumber received, LogSequenceNumber flushed, LogSequenceNumber applied, boolean replyRequired) throws SQLException {
      byte[] reply = this.prepareUpdateStatus(received, flushed, applied, replyRequired);
      this.copyDual.writeToCopy(reply, 0, reply.length);
      this.copyDual.flushCopy();
      this.explicitlyFlushedLSN = flushed;
      this.lastStatusUpdate = System.nanoTime();
   }

   private byte[] prepareUpdateStatus(LogSequenceNumber received, LogSequenceNumber flushed, LogSequenceNumber applied, boolean replyRequired) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(34);
      long now = System.nanoTime() / 1000000L;
      long systemClock = TimeUnit.MICROSECONDS.convert(now - 946684800000L, TimeUnit.MICROSECONDS);
      if (LOGGER.isLoggable(Level.FINEST)) {
         LOGGER.log(
            Level.FINEST,
            " FE=> StandbyStatusUpdate(received: {0}, flushed: {1}, applied: {2}, clock: {3})",
            new Object[]{received.asString(), flushed.asString(), applied.asString(), new Date(now)}
         );
      }

      byteBuffer.put((byte)114);
      byteBuffer.putLong(received.asLong());
      byteBuffer.putLong(flushed.asLong());
      byteBuffer.putLong(applied.asLong());
      byteBuffer.putLong(systemClock);
      if (replyRequired) {
         byteBuffer.put((byte)1);
      } else {
         byteBuffer.put((byte)(received == LogSequenceNumber.INVALID_LSN ? 1 : 0));
      }

      this.lastStatusUpdate = now;
      return byteBuffer.array();
   }

   private boolean processKeepAliveMessage(ByteBuffer buffer) {
      this.lastServerLSN = LogSequenceNumber.valueOf(buffer.getLong());
      if (this.lastServerLSN.asLong() > this.lastReceiveLSN.asLong()) {
         this.lastReceiveLSN = this.lastServerLSN;
      }

      if (this.explicitlyFlushedLSN.asLong() >= this.startOfLastMessageLSN.asLong()
         && this.lastServerLSN.asLong() > this.explicitlyFlushedLSN.asLong()
         && this.lastServerLSN.asLong() > this.lastFlushedLSN.asLong()) {
         this.lastFlushedLSN = this.lastServerLSN;
      }

      long lastServerClock = buffer.getLong();
      boolean replyRequired = buffer.get() != 0;
      if (LOGGER.isLoggable(Level.FINEST)) {
         Date clockTime = new Date(TimeUnit.MILLISECONDS.convert(lastServerClock, TimeUnit.MICROSECONDS) + 946684800000L);
         LOGGER.log(
            Level.FINEST,
            "  <=BE Keepalive(lastServerWal: {0}, clock: {1} needReply: {2})",
            new Object[]{this.lastServerLSN.asString(), clockTime, replyRequired}
         );
      }

      return replyRequired;
   }

   private ByteBuffer processXLogData(ByteBuffer buffer) {
      long startLsn = buffer.getLong();
      this.startOfLastMessageLSN = LogSequenceNumber.valueOf(startLsn);
      this.lastServerLSN = LogSequenceNumber.valueOf(buffer.getLong());
      long systemClock = buffer.getLong();
      if (this.replicationType == ReplicationType.LOGICAL) {
         this.lastReceiveLSN = LogSequenceNumber.valueOf(startLsn);
      } else if (this.replicationType == ReplicationType.PHYSICAL) {
         int payloadSize = buffer.limit() - buffer.position();
         this.lastReceiveLSN = LogSequenceNumber.valueOf(startLsn + payloadSize);
      }

      if (LOGGER.isLoggable(Level.FINEST)) {
         LOGGER.log(
            Level.FINEST,
            "  <=BE XLogData(currWal: {0}, lastServerWal: {1}, clock: {2})",
            new Object[]{this.lastReceiveLSN.asString(), this.lastServerLSN.asString(), systemClock}
         );
      }

      return buffer.slice();
   }

   private void checkClose() throws PSQLException {
      if (this.isClosed()) {
         throw new PSQLException(GT.tr("This replication stream has been closed."), PSQLState.CONNECTION_DOES_NOT_EXIST);
      }
   }

   @Override
   public void close() throws SQLException {
      if (!this.isClosed()) {
         LOGGER.log(Level.FINEST, " FE=> StopReplication");
         this.copyDual.endCopy();
         this.closeFlag = true;
      }
   }
}
