package github.nighter.smartspawner.libs.mariadb.message.client;

import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.message.ClientMessage;
import java.io.IOException;
import java.io.InputStream;

public final class QueryPacket implements RedoableClientMessage {
   private final String sql;
   private final InputStream localInfileInputStream;

   public QueryPacket(String sql) {
      this.sql = sql;
      this.localInfileInputStream = null;
   }

   public QueryPacket(String sql, InputStream localInfileInputStream) {
      this.sql = sql;
      this.localInfileInputStream = localInfileInputStream;
   }

   @Override
   public int batchUpdateLength() {
      return 1;
   }

   @Override
   public int encode(Writer writer, Context context) throws IOException {
      writer.initPacket();
      writer.writeByte(3);
      writer.writeString(this.sql);
      writer.flush();
      return 1;
   }

   public boolean isCommit() {
      return "COMMIT".equalsIgnoreCase(this.sql);
   }

   @Override
   public boolean validateLocalFileName(String fileName, Context context) {
      return ClientMessage.validateLocalFileName(this.sql, null, fileName, context);
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return this.localInfileInputStream;
   }

   @Override
   public String description() {
      return this.sql;
   }
}
