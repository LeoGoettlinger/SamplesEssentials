package github.nighter.smartspawner.libs.mariadb.message.server;

import github.nighter.smartspawner.libs.mariadb.BasePreparedStatement;
import github.nighter.smartspawner.libs.mariadb.client.Client;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Completion;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Reader;
import github.nighter.smartspawner.libs.mariadb.export.Prepare;
import github.nighter.smartspawner.libs.mariadb.util.log.Logger;
import github.nighter.smartspawner.libs.mariadb.util.log.Loggers;
import java.io.IOException;
import java.sql.SQLException;

public class PrepareResultPacket implements Completion, Prepare {
   private static final byte[] CONSTANT_PARAMETER_BYTES = new byte[]{3, 100, 101, 102, 0, 0, 0, 1, 63, 0, 0, 12, 63, 0, 0, 0, 0, 0, 6, -128, 0, 0, 0, 0};
   static final ColumnDecoder CONSTANT_PARAMETER = ColumnDecoder.decode(new ReadableByteBuf(CONSTANT_PARAMETER_BYTES, CONSTANT_PARAMETER_BYTES.length));
   private static final Logger logger = Loggers.getLogger(PrepareResultPacket.class);
   private final ColumnDecoder[] parameters;
   protected int statementId;
   private ColumnDecoder[] columns;

   public PrepareResultPacket(ReadableByteBuf buffer, Reader reader, Context context) throws IOException {
      boolean trace = logger.isTraceEnabled();
      buffer.readByte();
      this.statementId = buffer.readInt();
      int numColumns = buffer.readUnsignedShort();
      int numParams = buffer.readUnsignedShort();
      this.parameters = new ColumnDecoder[numParams];
      this.columns = new ColumnDecoder[numColumns];
      if (numParams > 0) {
         for (int i = 0; i < numParams; i++) {
            this.parameters[i] = CONSTANT_PARAMETER;
            reader.readReusablePacket();
         }

         if (!context.isEofDeprecated()) {
            reader.readReusablePacket();
         }
      }

      if (numColumns > 0) {
         for (int i = 0; i < numColumns; i++) {
            this.columns[i] = context.getColumnDecoderFunction().apply(new ReadableByteBuf(reader.readPacket(trace)));
         }

         if (!context.isEofDeprecated()) {
            reader.readReusablePacket();
         }
      }
   }

   @Override
   public void close(Client con) throws SQLException {
      con.closePrepare(this);
   }

   @Override
   public void decrementUse(Client con, BasePreparedStatement preparedStatement) throws SQLException {
      this.close(con);
   }

   @Override
   public int getStatementId() {
      return this.statementId;
   }

   @Override
   public ColumnDecoder[] getParameters() {
      return this.parameters;
   }

   @Override
   public ColumnDecoder[] getColumns() {
      return this.columns;
   }

   @Override
   public void setColumns(ColumnDecoder[] columns) {
      this.columns = columns;
   }
}
