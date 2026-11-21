package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import org.lushplugins.lushrewards.libraries.mysql.cj.conf.RuntimeProperty;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Resultset;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ResultsetRow;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ValueDecoder;

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
