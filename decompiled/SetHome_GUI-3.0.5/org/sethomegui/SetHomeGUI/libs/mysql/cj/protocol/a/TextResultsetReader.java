package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import java.io.IOException;
import java.util.ArrayList;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityReader;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRow;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRows;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.OkPacket;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.ResultsetRowsStreaming;

public class TextResultsetReader implements ProtocolEntityReader<Resultset, NativePacketPayload> {
   protected NativeProtocol protocol;

   public TextResultsetReader(NativeProtocol prot) {
      this.protocol = prot;
   }

   public Resultset read(
      int maxRows,
      boolean streamResults,
      NativePacketPayload resultPacket,
      ColumnDefinition metadata,
      ProtocolEntityFactory<Resultset, NativePacketPayload> resultSetFactory
   ) throws IOException {
      Resultset rs = null;
      long columnCount = resultPacket.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      if (columnCount > 0L) {
         ColumnDefinition cdef = this.protocol.read(ColumnDefinition.class, new ColumnDefinitionFactory(columnCount, metadata));
         if (!this.protocol.getServerSession().isEOFDeprecated()) {
            this.protocol.skipPacket();
         }

         ResultsetRows rows = null;
         if (streamResults) {
            rows = new ResultsetRowsStreaming<>(this.protocol, cdef, false, resultSetFactory);
            this.protocol.setStreamingData(rows);
         } else {
            TextRowFactory trf = new TextRowFactory(this.protocol, cdef, resultSetFactory.getResultSetConcurrency(), false);
            ArrayList<ResultsetRow> rowList = new ArrayList<>();

            for (ResultsetRow row = this.protocol.read(ResultsetRow.class, trf); row != null; row = this.protocol.read(ResultsetRow.class, trf)) {
               if (maxRows == -1 || rowList.size() < maxRows) {
                  rowList.add(row);
               }
            }

            rows = new ResultsetRowsStatic(rowList, cdef);
         }

         rs = resultSetFactory.createFromProtocolEntity(rows);
      } else {
         if (columnCount == -1L) {
            String charEncoding = this.protocol.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue();
            String fileName = resultPacket.readString(
               NativeConstants.StringSelfDataType.STRING_TERM,
               this.protocol.getServerSession().getCharsetSettings().doesPlatformDbCharsetMatches() ? charEncoding : null
            );
            resultPacket = this.protocol.sendFileToServer(fileName);
         }

         OkPacket ok = this.protocol.readServerStatusForResultSets(resultPacket, false);
         rs = resultSetFactory.createFromProtocolEntity(ok);
      }

      return rs;
   }
}
