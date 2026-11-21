package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.TimeZone;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.XMessage;

public class RowFactory implements ProtocolEntityFactory<Row, XMessage> {
   private ColumnDefinition metadata;
   private TimeZone defaultTimeZone;
   private PropertySet pset;

   public RowFactory(ColumnDefinition metadata, TimeZone defaultTimeZone, PropertySet pset) {
      this.metadata = metadata;
      this.defaultTimeZone = defaultTimeZone;
      this.pset = pset;
   }

   public Row createFromProtocolEntity(ProtocolEntity internalRow) {
      return new RowImpl((org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row)internalRow, this.metadata, this.defaultTimeZone, this.pset);
   }
}
