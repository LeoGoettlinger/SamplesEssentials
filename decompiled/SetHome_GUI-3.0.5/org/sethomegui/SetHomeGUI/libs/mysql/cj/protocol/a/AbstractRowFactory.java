package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.RuntimeProperty;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRow;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ValueDecoder;

public abstract class AbstractRowFactory implements ProtocolEntityFactory<ResultsetRow, NativePacketPayload> {
   protected ColumnDefinition columnDefinition;
   protected Resultset.Concurrency resultSetConcurrency;
   protected boolean canReuseRowPacketForBufferRow;
   protected RuntimeProperty<Integer> useBufferRowSizeThreshold;
   protected ExceptionInterceptor exceptionInterceptor;
   protected ValueDecoder valueDecoder;

   public boolean canReuseRowPacketForBufferRow() {
      return this.canReuseRowPacketForBufferRow;
   }
}
