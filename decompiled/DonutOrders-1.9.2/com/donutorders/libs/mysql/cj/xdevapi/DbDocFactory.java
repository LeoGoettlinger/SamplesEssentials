package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntity;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;
import com.donutorders.libs.mysql.cj.protocol.x.XMessage;

public class DbDocFactory implements ProtocolEntityFactory<DbDoc, XMessage> {
   private PropertySet pset;

   public DbDocFactory(PropertySet pset) {
      this.pset = pset;
   }

   public DbDoc createFromProtocolEntity(ProtocolEntity internalRow) {
      return ((com.donutorders.libs.mysql.cj.result.Row)internalRow).getValue(0, new DbDocValueFactory(this.pset));
   }
}
