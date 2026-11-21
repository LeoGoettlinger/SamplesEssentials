package com.donutorders.libs.mysql.cj.protocol.a;

import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.exceptions.WrongArgumentException;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntity;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;
import com.donutorders.libs.mysql.cj.protocol.Resultset;
import com.donutorders.libs.mysql.cj.protocol.ResultsetRows;
import com.donutorders.libs.mysql.cj.protocol.a.result.NativeResultset;
import com.donutorders.libs.mysql.cj.protocol.a.result.OkPacket;

public class ResultsetFactory implements ProtocolEntityFactory<Resultset, NativePacketPayload> {
   private Resultset.Type type = Resultset.Type.FORWARD_ONLY;
   private Resultset.Concurrency concurrency = Resultset.Concurrency.READ_ONLY;

   public ResultsetFactory(Resultset.Type type, Resultset.Concurrency concurrency) {
      this.type = type;
      this.concurrency = concurrency;
   }

   @Override
   public Resultset.Type getResultSetType() {
      return this.type;
   }

   @Override
   public Resultset.Concurrency getResultSetConcurrency() {
      return this.concurrency;
   }

   public Resultset createFromProtocolEntity(ProtocolEntity protocolEntity) {
      if (protocolEntity instanceof OkPacket) {
         return new NativeResultset((OkPacket)protocolEntity);
      } else if (protocolEntity instanceof ResultsetRows) {
         return new NativeResultset((ResultsetRows)protocolEntity);
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Unknown ProtocolEntity class " + protocolEntity);
      }
   }
}
