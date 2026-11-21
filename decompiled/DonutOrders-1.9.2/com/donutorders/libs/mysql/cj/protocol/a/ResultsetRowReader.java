package com.donutorders.libs.mysql.cj.protocol.a;

import com.donutorders.libs.mysql.cj.conf.PropertyKey;
import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.conf.RuntimeProperty;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityReader;
import com.donutorders.libs.mysql.cj.protocol.ResultsetRow;
import java.io.IOException;
import java.util.Optional;

public class ResultsetRowReader implements ProtocolEntityReader<ResultsetRow, NativePacketPayload> {
   protected NativeProtocol protocol;
   protected PropertySet propertySet;
   protected RuntimeProperty<Integer> useBufferRowSizeThreshold;

   public ResultsetRowReader(NativeProtocol prot) {
      this.protocol = prot;
      this.propertySet = this.protocol.getPropertySet();
      this.useBufferRowSizeThreshold = this.propertySet.getMemorySizeProperty(PropertyKey.largeRowSizeThreshold);
   }

   public ResultsetRow read(ProtocolEntityFactory<ResultsetRow, NativePacketPayload> sf) throws IOException {
      AbstractRowFactory rf = (AbstractRowFactory)sf;
      NativePacketPayload rowPacket = null;
      NativePacketHeader hdr = this.protocol.getPacketReader().readHeader();
      rowPacket = this.protocol
         .getPacketReader()
         .readMessage(rf.canReuseRowPacketForBufferRow() ? Optional.ofNullable(this.protocol.getReusablePacket()) : Optional.empty(), hdr);
      this.protocol.checkErrorMessage(rowPacket);
      rowPacket.setPosition(rowPacket.getPosition() - 1);
      if ((this.protocol.getServerSession().isEOFDeprecated() || !rowPacket.isEOFPacket())
         && (!this.protocol.getServerSession().isEOFDeprecated() || !rowPacket.isResultSetOKPacket())) {
         return sf.createFromMessage(rowPacket);
      } else {
         this.protocol.readServerStatusForResultSets(rowPacket, true);
         return null;
      }
   }
}
