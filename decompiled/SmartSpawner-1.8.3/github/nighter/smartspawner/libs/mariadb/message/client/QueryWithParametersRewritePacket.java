package github.nighter.smartspawner.libs.mariadb.message.client;

import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.Parameter;
import github.nighter.smartspawner.libs.mariadb.client.util.Parameters;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ByteArrayCodec;
import github.nighter.smartspawner.libs.mariadb.util.ClientParser;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class QueryWithParametersRewritePacket implements RedoableClientMessage {
   private final String preSqlCmd;
   private final ClientParser parser;
   private List<Parameters> parametersList;

   public QueryWithParametersRewritePacket(String preSqlCmd, ClientParser parser, List<Parameters> batchParameterList) {
      this.preSqlCmd = preSqlCmd;
      this.parametersList = batchParameterList;
      this.parser = parser;
   }

   @Override
   public void saveParameters() {
      List<Parameters> savedList = new ArrayList<>(this.parametersList.size());

      for (Parameters parameterList : this.parametersList) {
         savedList.add(parameterList.clone());
      }

      this.parametersList = savedList;
   }

   @Override
   public void ensureReplayable(Context context) throws IOException, SQLException {
      for (int j = 0; j < this.parametersList.size(); j++) {
         Parameters parameters = this.parametersList.get(j);
         int parameterCount = parameters.size();

         for (int i = 0; i < parameterCount; i++) {
            Parameter p = parameters.get(i);
            if (!p.isNull() && p.canEncodeLongData()) {
               parameters.set(i, new github.nighter.smartspawner.libs.mariadb.codec.Parameter<>(ByteArrayCodec.INSTANCE, p.encodeData()));
            }
         }
      }
   }

   @Override
   public int encode(Writer writer, Context context) throws IOException, SQLException {
      Iterator<Parameters> paramIterator = this.parametersList.iterator();
      Parameters parameters = paramIterator.next();
      int rewritePacketNo = 0;
      int tailStart = this.parser.getParamPositions().get(this.parser.getParamCount() - 1) + 1;
      int endingPartLen = this.parser.getQuery().length - tailStart;

      label73:
      while (true) {
         rewritePacketNo++;
         writer.initPacket();
         writer.writeByte(3);
         if (this.preSqlCmd != null) {
            writer.writeAscii(this.preSqlCmd);
         }

         int pos = 0;
         if (this.parser.getParamCount() > parameters.size()) {
            throw context.getExceptionFactory().create("wrong number of parameters", "Y0000");
         }

         for (int i = 0; i < this.parser.getParamCount(); i++) {
            int paramPos = this.parser.getParamPositions().get(i);
            writer.writeBytes(this.parser.getQuery(), pos, paramPos - pos);
            pos = paramPos + 1;
            parameters.get(i).encodeText(writer, context);
         }

         if (!paramIterator.hasNext()) {
            break;
         }

         parameters = paramIterator.next();
         if (writer.throwMaxAllowedLengthOr16M(writer.pos() + endingPartLen)) {
            writer.writeBytes(this.parser.getQuery(), tailStart, endingPartLen);
            writer.flush();
         } else {
            while (true) {
               int parameterLength = 0;
               boolean knownParameterSize = true;
               if (this.parser.getParamCount() > parameters.size()) {
                  throw context.getExceptionFactory().create("wrong number of parameters", "Y0000");
               }

               for (int i = 0; i < this.parser.getParamCount(); i++) {
                  int paramSize = parameters.get(i).getApproximateTextProtocolLength();
                  if (paramSize == -1) {
                     knownParameterSize = false;
                     break;
                  }

                  if (i > 0) {
                     parameterLength += this.parser.getParamPositions().get(i) - (this.parser.getParamPositions().get(i - 1) + 1);
                  }

                  parameterLength += paramSize;
               }

               if (!knownParameterSize || writer.throwMaxAllowedLengthOr16M(writer.pos() + parameterLength)) {
                  writer.writeBytes(this.parser.getQuery(), tailStart, endingPartLen);
                  writer.flush();
                  break;
               }

               writer.writeBytes(this.parser.getQuery(), pos, this.parser.getValuesBracketPositions().get(1) + 1 - pos);
               writer.writeByte(44);
               pos = this.parser.getValuesBracketPositions().get(0);

               for (int i = 0; i < this.parser.getParamPositions().size(); i++) {
                  int paramPos = this.parser.getParamPositions().get(i);
                  writer.writeBytes(this.parser.getQuery(), pos, paramPos - pos);
                  pos = paramPos + 1;
                  parameters.get(i).encodeText(writer, context);
               }

               if (!paramIterator.hasNext()) {
                  break label73;
               }

               parameters = paramIterator.next();
            }
         }
      }

      writer.writeBytes(this.parser.getQuery(), tailStart, endingPartLen);
      writer.flush();
      return rewritePacketNo;
   }

   @Override
   public boolean binaryProtocol() {
      return false;
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return null;
   }

   @Override
   public String description() {
      return "REWRITE: " + this.preSqlCmd + this.parser.getSql();
   }

   @Override
   public int batchUpdateLength() {
      return this.parametersList.size();
   }

   @Override
   public boolean validateLocalFileName(String fileName, Context context) {
      return false;
   }
}
