package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.result;

import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.NativePacketPayload;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.result.AbstractResultsetRow;

public abstract class AbstractBufferRow extends AbstractResultsetRow {
   protected NativePacketPayload rowFromServer;
   protected int homePosition = 0;
   protected int lastRequestedIndex = -1;
   protected int lastRequestedPos;

   protected AbstractBufferRow(ExceptionInterceptor exceptionInterceptor) {
      super(exceptionInterceptor);
   }

   abstract int findAndSeekToOffset(int var1);
}
