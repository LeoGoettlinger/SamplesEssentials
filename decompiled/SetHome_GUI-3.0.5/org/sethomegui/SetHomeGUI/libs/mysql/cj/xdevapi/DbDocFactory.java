package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.XMessage;

public class DbDocFactory implements ProtocolEntityFactory<DbDoc, XMessage> {
   private PropertySet pset;

   public DbDocFactory(PropertySet pset) {
      this.pset = pset;
   }

   public DbDoc createFromProtocolEntity(ProtocolEntity internalRow) {
      return ((org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row)internalRow).getValue(0, new DbDocValueFactory(this.pset));
   }
}
