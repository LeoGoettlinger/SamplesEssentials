package github.nighter.smartspawner.libs.mariadb.message.client;

import github.nighter.smartspawner.libs.mariadb.BasePreparedStatement;
import github.nighter.smartspawner.libs.mariadb.ServerPreparedStatement;
import github.nighter.smartspawner.libs.mariadb.Statement;
import github.nighter.smartspawner.libs.mariadb.client.Completion;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Reader;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.ClosableLock;
import github.nighter.smartspawner.libs.mariadb.client.util.Parameter;
import github.nighter.smartspawner.libs.mariadb.client.util.Parameters;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import github.nighter.smartspawner.libs.mariadb.export.Prepare;
import github.nighter.smartspawner.libs.mariadb.message.ClientMessage;
import github.nighter.smartspawner.libs.mariadb.message.server.CachedPrepareResultPacket;
import github.nighter.smartspawner.libs.mariadb.message.server.ErrorPacket;
import github.nighter.smartspawner.libs.mariadb.message.server.PrepareResultPacket;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ByteArrayCodec;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.function.Consumer;

public final class PrepareExecutePacket implements RedoableWithPrepareClientMessage {
   private final String sql;
   private final ServerPreparedStatement prep;
   private final InputStream localInfileInputStream;
   private PrepareResultPacket prepareResult;
   private Parameters parameters;

   public PrepareExecutePacket(String sql, Parameters parameters, ServerPreparedStatement prep, InputStream localInfileInputStream) {
      this.sql = sql;
      this.parameters = parameters;
      this.prep = prep;
      this.localInfileInputStream = localInfileInputStream;
      this.prepareResult = null;
   }

   @Override
   public int encode(Writer writer, Context context, Prepare newPrepareResult) throws IOException, SQLException {
      int statementId = -1;
      if (newPrepareResult == null) {
         writer.initPacket();
         writer.writeByte(22);
         writer.writeString(this.sql);
         writer.flushPipeline();
      } else {
         statementId = newPrepareResult.getStatementId();
      }

      int parameterCount = this.parameters.size();

      for (int i = 0; i < parameterCount; i++) {
         Parameter p = this.parameters.get(i);
         if (!p.isNull() && p.canEncodeLongData()) {
            new LongDataPacket(statementId, p, i).encode(writer, context);
         }
      }

      writer.initPacket();
      writer.writeByte(23);
      writer.writeInt(statementId);
      writer.writeByte(0);
      writer.writeInt(1);
      if (parameterCount > 0) {
         int nullCount = (parameterCount + 7) / 8;
         byte[] nullBitsBuffer = new byte[nullCount];
         int initialPos = writer.pos();
         writer.pos(initialPos + nullCount);
         writer.writeByte(1);

         for (int ix = 0; ix < parameterCount; ix++) {
            Parameter p = this.parameters.get(ix);
            writer.writeByte(p.getBinaryEncodeType());
            writer.writeByte(0);
            if (p.isNull()) {
               nullBitsBuffer[ix / 8] = (byte)(nullBitsBuffer[ix / 8] | (byte)(1 << ix % 8));
            }
         }

         writer.writeBytesAtPos(nullBitsBuffer, initialPos);

         for (int ixx = 0; ixx < parameterCount; ixx++) {
            Parameter p = this.parameters.get(ixx);
            if (!p.isNull() && !p.canEncodeLongData()) {
               p.encodeBinary(writer, context);
            }
         }
      }

      writer.flush();
      return newPrepareResult == null ? 2 : 1;
   }

   @Override
   public Completion readPacket(
      Statement stmt,
      int fetchSize,
      long maxRows,
      int resultSetConcurrency,
      int resultSetType,
      boolean closeOnCompletion,
      Reader reader,
      Writer writer,
      Context context,
      ExceptionFactory exceptionFactory,
      ClosableLock lock,
      boolean traceEnable,
      ClientMessage message,
      Consumer<String> redirectFct
   ) throws IOException, SQLException {
      if (this.prepareResult == null) {
         ReadableByteBuf buf = reader.readReusablePacket(traceEnable);
         if (buf.getUnsignedByte() == 255) {
            ErrorPacket errorPacket = new ErrorPacket(buf, context);
            throw exceptionFactory.withSql(this.description()).create(errorPacket.getMessage(), errorPacket.getSqlState(), errorPacket.getErrorCode(), true);
         } else if (context.getConf().useServerPrepStmts() && context.getConf().cachePrepStmts() && this.sql.length() < 8192) {
            PrepareResultPacket prepare = new CachedPrepareResultPacket(buf, reader, context);
            PrepareResultPacket previousCached = (PrepareResultPacket)context.putPrepareCacheCmd(
               this.sql, prepare, stmt instanceof ServerPreparedStatement ? (ServerPreparedStatement)stmt : null
            );
            if (stmt != null) {
               ((BasePreparedStatement)stmt).setPrepareResult(previousCached != null ? previousCached : prepare);
            }

            this.prepareResult = previousCached != null ? previousCached : prepare;
            return this.prepareResult;
         } else {
            PrepareResultPacket prepareResult = new PrepareResultPacket(buf, reader, context);
            if (stmt != null) {
               ((BasePreparedStatement)stmt).setPrepareResult(prepareResult);
            }

            this.prepareResult = prepareResult;
            return prepareResult;
         }
      } else {
         return RedoableWithPrepareClientMessage.super.readPacket(
            stmt,
            fetchSize,
            maxRows,
            resultSetConcurrency,
            resultSetType,
            closeOnCompletion,
            reader,
            writer,
            context,
            exceptionFactory,
            lock,
            traceEnable,
            message,
            redirectFct
         );
      }
   }

   @Override
   public void saveParameters() {
      this.parameters = this.parameters.clone();
   }

   @Override
   public void ensureReplayable(Context context) throws IOException, SQLException {
      int parameterCount = this.parameters.size();

      for (int i = 0; i < parameterCount; i++) {
         Parameter p = this.parameters.get(i);
         if (!p.isNull() && p.canEncodeLongData()) {
            this.parameters.set(i, new github.nighter.smartspawner.libs.mariadb.codec.Parameter<>(ByteArrayCodec.INSTANCE, p.encodeData()));
         }
      }
   }

   @Override
   public boolean canSkipMeta() {
      return true;
   }

   @Override
   public String description() {
      return "PREPARE + EXECUTE " + this.sql;
   }

   @Override
   public int batchUpdateLength() {
      return 1;
   }

   @Override
   public String getCommand() {
      return this.sql;
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return this.localInfileInputStream;
   }

   public ServerPreparedStatement prep() {
      return this.prep;
   }

   @Override
   public boolean binaryProtocol() {
      return true;
   }

   @Override
   public boolean validateLocalFileName(String fileName, Context context) {
      return ClientMessage.validateLocalFileName(this.sql, this.parameters, fileName, context);
   }

   @Override
   public void setPrepareResult(PrepareResultPacket prepareResult) {
      this.prepareResult = prepareResult;
   }
}
