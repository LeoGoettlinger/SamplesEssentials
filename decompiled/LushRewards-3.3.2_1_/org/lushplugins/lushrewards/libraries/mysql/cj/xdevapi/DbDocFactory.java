package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XMessage;

public class DbDocFactory implements ProtocolEntityFactory<DbDoc, XMessage> {
   private PropertySet pset;

   public DbDocFactory(PropertySet pset) {
      this.pset = pset;
   }

   public DbDoc createFromProtocolEntity(ProtocolEntity internalRow) {
      return ((org.lushplugins.lushrewards.libraries.mysql.cj.result.Row)internalRow).getValue(0, new DbDocValueFactory(this.pset));
   }
}
