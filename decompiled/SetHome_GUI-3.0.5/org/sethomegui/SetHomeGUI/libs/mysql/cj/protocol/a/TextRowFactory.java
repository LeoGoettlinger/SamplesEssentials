package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRow;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.ByteArrayRow;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.TextBufferRow;

public class TextRowFactory extends AbstractRowFactory implements ProtocolEntityFactory<ResultsetRow, NativePacketPayload> {
   public TextRowFactory(
      NativeProtocol protocol, ColumnDefinition colDefinition, Resultset.Concurrency resultSetConcurrency, boolean canReuseRowPacketForBufferRow
   ) {
      this.columnDefinition = colDefinition;
      this.resultSetConcurrency = resultSetConcurrency;
      this.canReuseRowPacketForBufferRow = canReuseRowPacketForBufferRow;
      this.useBufferRowSizeThreshold = protocol.getPropertySet().getMemorySizeProperty(PropertyKey.largeRowSizeThreshold);
      this.exceptionInterceptor = protocol.getExceptionInterceptor();
      this.valueDecoder = new MysqlTextValueDecoder();
   }

   public ResultsetRow createFromMessage(NativePacketPayload rowPacket) {
      boolean useBufferRow = this.canReuseRowPacketForBufferRow
         || this.columnDefinition.hasLargeFields()
         || rowPacket.getPayloadLength() >= this.useBufferRowSizeThreshold.getValue();
      if (this.resultSetConcurrency != Resultset.Concurrency.UPDATABLE && useBufferRow) {
         return new TextBufferRow(rowPacket, this.columnDefinition, this.exceptionInterceptor, this.valueDecoder);
      } else {
         byte[][] rowBytes = new byte[this.columnDefinition.getFields().length][];

         for (int i = 0; i < this.columnDefinition.getFields().length; i++) {
            rowBytes[i] = rowPacket.readBytes(NativeConstants.StringSelfDataType.STRING_LENENC);
         }

         return new ByteArrayRow(rowBytes, this.exceptionInterceptor);
      }
   }

   @Override
   public boolean canReuseRowPacketForBufferRow() {
      return this.canReuseRowPacketForBufferRow;
   }
}
